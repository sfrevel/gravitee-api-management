/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.gateway.handlers.api.manager.impl;

import io.gravitee.common.event.EventManager;
import io.gravitee.common.util.DataEncryptor;
import io.gravitee.gateway.env.GatewayConfiguration;
import io.gravitee.gateway.handlers.api.definition.Api;
import io.gravitee.gateway.handlers.api.manager.ApiManager;
import io.gravitee.gateway.handlers.api.manager.Deployer;
import io.gravitee.gateway.handlers.api.manager.deployer.ApiDeployer;
import io.gravitee.gateway.reactor.ReactableApi;
import io.gravitee.gateway.reactor.ReactorEvent;
import io.gravitee.node.api.cache.Cache;
import io.gravitee.node.api.cache.CacheListener;
import io.gravitee.node.api.cache.CacheManager;
import io.gravitee.node.api.cache.EntryEvent;
import io.gravitee.node.api.cache.EntryEventType;
import io.gravitee.node.api.cluster.ClusterManager;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class ApiManagerImpl implements ApiManager, InitializingBean, CacheListener<String, ReactableApi<?>> {

    private static final int PARALLELISM = Runtime.getRuntime().availableProcessors() * 2;
    private final Logger logger = LoggerFactory.getLogger(ApiManagerImpl.class);

    @Autowired
    private EventManager eventManager;

    @Autowired
    private GatewayConfiguration gatewayConfiguration;

    @Autowired
    private ClusterManager clusterManager;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private DataEncryptor dataEncryptor;

    private Cache<String, ReactableApi<?>> apis;

    private Map<Class<? extends ReactableApi<?>>, ? extends Deployer<?>> deployers;

    @Override
    public void afterPropertiesSet() {
        if (cacheManager != null) {
            apis = cacheManager.getOrCreateCache("apis");
            apis.addCacheListener(this);
        }

        deployers =
            Map.of(
                Api.class,
                new ApiDeployer(gatewayConfiguration, dataEncryptor),
                io.gravitee.gateway.jupiter.handlers.api.v4.Api.class,
                new io.gravitee.gateway.jupiter.handlers.api.v4.deployer.ApiDeployer(gatewayConfiguration, dataEncryptor)
            );
    }

    @Override
    public void onEvent(EntryEvent<String, ReactableApi<?>> event) {
        // Replication is only done for secondary nodes
        if (!clusterManager.isMasterNode()) {
            if (event.getEventType() == EntryEventType.ADDED) {
                register(event.getValue());
            } else if (event.getEventType() == EntryEventType.UPDATED) {
                register(event.getValue());
            } else if (
                event.getEventType() == EntryEventType.REMOVED ||
                event.getEventType() == EntryEventType.EVICTED ||
                event.getEventType() == EntryEventType.EXPIRED
            ) {
                unregister(event.getKey());
            }
        }
    }

    private boolean register(ReactableApi<?> api, boolean force) {
        // Get deployed API
        ReactableApi<?> deployedApi = get(api.getId());

        // Does the API have a matching sharding tags ?
        if (gatewayConfiguration.hasMatchingTags(api.getTags())) {
            boolean apiToDeploy = deployedApi == null || force;
            boolean apiToUpdate = !apiToDeploy && deployedApi.getDeployedAt().before(api.getDeployedAt());

            // if API will be deployed or updated
            if (apiToDeploy || apiToUpdate) {
                Deployer deployer = deployers.get(api.getClass());
                deployer.initialize(api);
            }

            // API is not yet deployed, so let's do it
            if (apiToDeploy) {
                deploy(api);
                return true;
            }
            // API has to be updated, so update it
            else if (apiToUpdate) {
                update(api);
                return true;
            }
        } else {
            logger.debug("The API {} has been ignored because not in configured tags {}", api.getName(), api.getTags());

            // Check that the API was not previously deployed with other tags
            // In that case, we must undeploy it
            if (deployedApi != null) {
                undeploy(api.getId());
            }
        }

        return false;
    }

    @Override
    public boolean register(ReactableApi api) {
        return register(api, false);
    }

    @Override
    public void unregister(String apiId) {
        undeploy(apiId);
    }

    @Override
    public void refresh() {
        if (apis != null && !apis.isEmpty()) {
            final long begin = System.currentTimeMillis();

            logger.info("Starting apis refresh. {} apis to be refreshed.", apis.size());

            // Create an executor to parallelize a refresh for all the apis.
            final ExecutorService refreshAllExecutor = createExecutor(Math.min(PARALLELISM, apis.size()));

            final List<Callable<Boolean>> toInvoke = apis
                .values()
                .stream()
                .map(api -> ((Callable<Boolean>) () -> register(api, true)))
                .collect(Collectors.toList());

            try {
                refreshAllExecutor.invokeAll(toInvoke);
                refreshAllExecutor.shutdown();
                while (!refreshAllExecutor.awaitTermination(100, TimeUnit.MILLISECONDS));
            } catch (InterruptedException e) {
                logger.error("Unable to refresh apis", e);
                Thread.currentThread().interrupt();
            } finally {
                refreshAllExecutor.shutdown();
            }

            logger.info("Apis refresh done in {}ms", (System.currentTimeMillis() - begin));
        }
    }

    private void deploy(ReactableApi api) {
        MDC.put("api", api.getId());
        logger.debug("Deployment of {}", api);

        if (api.isEnabled()) {
            Deployer deployer = deployers.get(api.getClass());
            List<String> plans = deployer.getPlans(api);

            // Deploy the API only if there is at least one plan
            if (!plans.isEmpty()) {
                logger.debug("Deploying {} plan(s) for {}:", plans.size(), api);
                for (String plan : plans) {
                    logger.debug("\t- {}", plan);
                }

                apis.put(api.getId(), api);
                eventManager.publishEvent(ReactorEvent.DEPLOY, api);
                logger.info("{} has been deployed", api);
            } else {
                logger.warn("There is no published plan associated to this API, skipping deployment...");
            }
        } else {
            logger.debug("{} is not enabled. Skip deployment.", api);
        }

        MDC.remove("api");
    }

    private ExecutorService createExecutor(int threadCount) {
        return Executors.newFixedThreadPool(
            threadCount,
            new ThreadFactory() {
                private int counter = 0;

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "gio.api-manager-" + counter++);
                }
            }
        );
    }

    private void update(ReactableApi<?> api) {
        MDC.put("api", api.getId());
        logger.debug("Updating {}", api);

        Deployer deployer = deployers.get(api.getClass());
        List<String> plans = deployer.getPlans(api);

        if (!plans.isEmpty()) {
            logger.debug("Deploying {} plan(s) for {}:", plans.size(), api);
            for (String plan : plans) {
                logger.info("\t- {}", plan);
            }

            apis.put(api.getId(), api);
            eventManager.publishEvent(ReactorEvent.UPDATE, api);
            logger.info("{} has been updated", api);
        } else {
            logger.warn("There is no published plan associated to this API, undeploy it...");
            undeploy(api.getId());
        }

        MDC.remove("api");
    }

    private void undeploy(String apiId) {
        ReactableApi<?> currentApi = apis.evict(apiId);
        if (currentApi != null) {
            MDC.put("api", apiId);
            logger.debug("Undeployment of {}", currentApi);

            eventManager.publishEvent(ReactorEvent.UNDEPLOY, currentApi);
            logger.info("{} has been undeployed", currentApi);
            MDC.remove("api");
        }
    }

    @Override
    public Collection<ReactableApi<?>> apis() {
        return apis.values();
    }

    @Override
    public ReactableApi<?> get(String name) {
        return apis.get(name);
    }

    public void setEventManager(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    public void setApis(Cache<String, ReactableApi<?>> apis) {
        this.apis = apis;
    }
}
