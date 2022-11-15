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
package io.gravitee.gateway.services.sync.kubernetes;

import static io.gravitee.repository.management.model.Event.EventProperties.API_ID;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.gravitee.common.service.AbstractService;
import io.gravitee.definition.jackson.datatype.GraviteeMapper;
import io.gravitee.definition.model.Api;
import io.gravitee.gateway.services.sync.synchronizer.ApiSynchronizer;
import io.gravitee.kubernetes.client.KubernetesClient;
import io.gravitee.kubernetes.client.api.LabelSelector;
import io.gravitee.kubernetes.client.api.WatchQuery;
import io.gravitee.kubernetes.client.model.v1.ConfigMap;
import io.gravitee.kubernetes.client.model.v1.Event;
import io.gravitee.repository.management.model.EventType;
import io.gravitee.repository.management.model.LifecycleState;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public class KubernetesSyncService extends AbstractService<KubernetesSyncService> {

    protected static final String LABEL_MANAGED_BY = "managed-by";
    protected static final String LABEL_GIO_TYPE = "gio-type";
    protected static final String GRAVITEE_IO = "gravitee.io";
    protected static final String APIDEFINITIONS_TYPE = "apidefinitions.gravitee.io";
    protected static final String DATA_ENVIRONMENT_ID = "environmentId";
    protected static final String DATA_DEFINITION = "definition";
    private static final int RETRY_DELAY_MILLIS = 10000;
    private final Logger logger = LoggerFactory.getLogger(KubernetesSyncService.class);
    private final KubernetesClient client;
    private final ApiSynchronizer apiSynchronizer;
    private final ObjectMapper mapper;
    private Disposable disposable;

    public KubernetesSyncService(KubernetesClient client, ApiSynchronizer apiSynchronizer) {
        this.client = client;
        this.apiSynchronizer = apiSynchronizer;
        this.mapper = new GraviteeMapper();
    }

    @Override
    protected void doStart() throws Exception {
        startWatch();
    }

    private void startWatch() {
        logger.info("Kubernetes synchronization started at {}", Instant.now().toString());
        this.disposable =
            watch()
                .flatMapCompletable(this::handleConfigMapEvent)
                .doOnError(throwable -> logger.error("An error occurred during configmaps refresh. Restarting watch.", throwable))
                .retry()
                .subscribe();
    }

    private Flowable<Event<ConfigMap>> watch() {
        return client
            .watch(
                WatchQuery
                    .configMaps()
                    .labelSelector(LabelSelector.equals(LABEL_MANAGED_BY, GRAVITEE_IO))
                    .labelSelector(LabelSelector.equals(LABEL_GIO_TYPE, APIDEFINITIONS_TYPE))
                    .build()
            )
            .observeOn(Schedulers.computation())
            .repeat()
            .retryWhen(errors -> errors.delay(RETRY_DELAY_MILLIS, TimeUnit.MILLISECONDS));
    }

    private Completable handleConfigMapEvent(Event<ConfigMap> kubEvent) {
        ConfigMap configMap = kubEvent.getObject();

        logger.info(
            "New event {} for service {} namespace {}",
            kubEvent.getType(),
            configMap.getMetadata().getName(),
            configMap.getMetadata().getNamespace()
        );
        String definition = configMap.getData().get(DATA_DEFINITION);

        Api apiDefinition = null;

        if (definition != null) {
            try {
                // Need to deserialize api definition in order to recreate a regular Event which can be handled by the ApiSynchronizer.
                apiDefinition = mapper.readValue(definition, Api.class);

                final io.gravitee.repository.management.model.Event event = new io.gravitee.repository.management.model.Event();
                event.setProperties(Collections.singletonMap(API_ID.getValue(), apiDefinition.getId()));
                event.setCreatedAt(new Date());

                final io.gravitee.repository.management.model.Api api = new io.gravitee.repository.management.model.Api();
                api.setEnvironmentId(configMap.getData().get(DATA_ENVIRONMENT_ID));
                api.setDefinition(definition);
                api.setId(apiDefinition.getId());

                switch (kubEvent.getType()) {
                    case "ADDED":
                    case "MODIFIED":
                        event.setType(EventType.PUBLISH_API);
                        api.setLifecycleState(LifecycleState.STARTED);
                        break;
                    case "DELETED":
                        event.setType(EventType.UNPUBLISH_API);
                        api.setLifecycleState(LifecycleState.STOPPED);
                }

                event.setPayload(mapper.writeValueAsString(api));
                apiSynchronizer
                    .processApiEvents(Flowable.just(event))
                    .subscribe(
                        s -> logger.info("Event {} processed for API {}", kubEvent.getType(), api.getId()),
                        t ->
                            logger.error(
                                String.format("An error occurred while processing event %s for API %s", kubEvent.getType(), api.getId()),
                                t
                            )
                    );
            } catch (Exception ex) {
                logger.error(
                    "Unexpected error while trying to register service {}",
                    (apiDefinition != null) ? apiDefinition.getId() : "unknown",
                    ex
                );
            }
        }

        return Completable.complete();
    }

    @Override
    protected void doStop() throws Exception {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}
