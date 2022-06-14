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
package io.gravitee.gateway.services.sync.cache.task;

import static io.gravitee.repository.management.model.Subscription.Status.*;
import static org.mockito.Mockito.*;

import io.gravitee.gateway.services.sync.cache.ApiKeysCache;
import io.gravitee.repository.exceptions.TechnicalException;
import io.gravitee.repository.management.api.ApiKeyRepository;
import io.gravitee.repository.management.api.SubscriptionRepository;
import io.gravitee.repository.management.api.search.ApiKeyCriteria;
import io.gravitee.repository.management.model.ApiKey;
import io.gravitee.repository.management.model.Subscription;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApiKeyRefresherTest {

    @InjectMocks
    private FullApiKeyRefresher apiKeyRefresher;

    @Mock
    private ApiKeyRepository apiKeyRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private ApiKeysCache cache;

    @Before
    public void setup() {
        apiKeyRefresher.setApiKeyRepository(apiKeyRepository);
        apiKeyRefresher.setSubscriptionRepository(subscriptionRepository);
        apiKeyRefresher.setCache(cache);
    }

    @Test
    public void doRefresh_should_cache_subscriptions() throws TechnicalException {
        ApiKeyCriteria apiKeyCriteria = mock(ApiKeyCriteria.class);

        List<ApiKey> apiKeysList = List.of(
            buildTestApiKey("key-1", List.of("sub-1", "sub-2", "sub-3", "sub-6")),
            buildTestApiKey("key-2", List.of("sub-1", "sub-4"), true, false),
            buildTestApiKey("key-3", List.of()),
            buildTestApiKey("key-4", List.of("sub-1", "sub-4", "sub-5")),
            buildTestApiKey("key-5", List.of("sub-1", "sub-4"), false, true),
            buildTestApiKey("key-6", List.of("sub-unknown")),
            buildTestApiKey("key-7", List.of("sub-6"))
        );

        when(apiKeyRepository.findByCriteria(apiKeyCriteria)).thenReturn(apiKeysList);

        when(subscriptionRepository.findByIdIn(Set.of("sub-1", "sub-2", "sub-3", "sub-4", "sub-5", "sub-unknown", "sub-6")))
            .thenReturn(
                List.of(
                    buildTestSubscription("sub-1", CLOSED, LocalDate.now().minusDays(1)),
                    buildTestSubscription("sub-2", ACCEPTED, LocalDate.now().minusDays(1)),
                    buildTestSubscription("sub-3", REJECTED, LocalDate.now().minusDays(1)),
                    buildTestSubscription("sub-4", ACCEPTED, LocalDate.now().minusDays(1)),
                    buildTestSubscription("sub-5", ACCEPTED, LocalDate.now().minusDays(1)),
                    buildTestSubscription("sub-6", ACCEPTED, LocalDate.now().plusDays(1))
                )
            );

        apiKeyRefresher.doRefresh(apiKeyCriteria);

        // those API keys have been put in cache because they are active and their subscription is active
        verify(cache, times(1)).put(argThat(apiKey -> apiKey.getId().equals("key-1") && apiKey.getSubscription().equals("sub-2")));
        verify(cache, times(1)).put(argThat(apiKey -> apiKey.getId().equals("key-4") && apiKey.getSubscription().equals("sub-4")));
        verify(cache, times(1)).put(argThat(apiKey -> apiKey.getId().equals("key-4") && apiKey.getSubscription().equals("sub-5")));

        // those API keys have been removed from cache because they are inactive, or their subscription is not active
        verify(cache, times(1)).remove(argThat(apiKey -> apiKey.getId().equals("key-1") && apiKey.getSubscription().equals("sub-1")));
        verify(cache, times(1)).remove(argThat(apiKey -> apiKey.getId().equals("key-1") && apiKey.getSubscription().equals("sub-3")));
        verify(cache, times(1)).remove(argThat(apiKey -> apiKey.getId().equals("key-2") && apiKey.getSubscription().equals("sub-1")));
        verify(cache, times(1)).remove(argThat(apiKey -> apiKey.getId().equals("key-2") && apiKey.getSubscription().equals("sub-4")));
        verify(cache, times(1)).remove(argThat(apiKey -> apiKey.getId().equals("key-4") && apiKey.getSubscription().equals("sub-1")));
        verify(cache, times(1)).remove(argThat(apiKey -> apiKey.getId().equals("key-5") && apiKey.getSubscription().equals("sub-1")));
        verify(cache, times(1)).remove(argThat(apiKey -> apiKey.getId().equals("key-5") && apiKey.getSubscription().equals("sub-4")));

        // those API keys have been removed from cache because their subscription is active but not yet started
        verify(cache, times(1)).remove(argThat(apiKey -> apiKey.getId().equals("key-1") && apiKey.getSubscription().equals("sub-6")));
        verify(cache, times(1)).remove(argThat(apiKey -> apiKey.getId().equals("key-7") && apiKey.getSubscription().equals("sub-6")));

        verifyNoMoreInteractions(cache);
    }

    private ApiKey buildTestApiKey(String id, List<String> subscriptions) {
        return buildTestApiKey(id, subscriptions, false, false);
    }

    private ApiKey buildTestApiKey(String id, List<String> subscriptions, boolean revoked, boolean paused) {
        ApiKey apiKey = new ApiKey();
        apiKey.setId(id);
        apiKey.setSubscriptions(subscriptions);
        apiKey.setRevoked(revoked);
        apiKey.setPaused(paused);
        return apiKey;
    }

    private Subscription buildTestSubscription(String id, Subscription.Status status, LocalDate startDate) {
        Subscription subscription = new Subscription();
        subscription.setId(id);
        subscription.setStatus(status);
        subscription.setStartingAt(Date.from(startDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        return subscription;
    }
}
