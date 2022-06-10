package io.gravitee.definition.model;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.gravitee.definition.model.endpoint.EndpointStatusListener;
import org.junit.Test;

public class EndpointTest {

    private String expectedJsonString = String.join(
        ",",
        "{\"name\":\"my-name\"",
        "\"target\":\"my-target\"",
        "\"weight\":1",
        "\"backup\":false",
        "\"tenants\":null",
        "\"type\":\"my-type\"",
        "\"inherit\":null",
        "\"healthcheck\":null}"
    );

    @Test
    public void shouldSerializeEndpoint() throws JsonProcessingException {
        Endpoint endpoint = new Endpoint("my-type", "my-name", "my-target");

        String serializedEndpoint = new ObjectMapper().writeValueAsString(endpoint);

        assertEquals(expectedJsonString, serializedEndpoint);
    }

    @Test
    public void shouldSerializeEndpoint_withUnserializableStatusListener() throws JsonProcessingException {
        /* This simulates an EndpointStatusListener that jackson will fail to serialize
         * For serialization, jackson will call the getter that throws an exception
         */
        var unserializableStatusListener = new EndpointStatusListener() {
            public void onStatusChanged(Endpoint.Status s) {}

            public String getTestData() throws Exception {
                throw new Exception("Serialization fails");
            }
        };

        Endpoint endpoint = new Endpoint("my-type", "my-name", "my-target");
        endpoint.addEndpointAvailabilityListener(unserializableStatusListener);

        String serializedEndpoint = new ObjectMapper().writeValueAsString(endpoint);

        assertEquals(expectedJsonString, serializedEndpoint);
    }
}
