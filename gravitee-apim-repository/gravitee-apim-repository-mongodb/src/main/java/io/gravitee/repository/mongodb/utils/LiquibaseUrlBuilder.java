package io.gravitee.repository.mongodb.utils;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

@Slf4j
public class LiquibaseUrlBuilder {

    private static final String MONGODB_PREFIX = "mongodb://";
    private static final String MONGODB_SRV_PREFIX = "mongodb+srv://";

    private boolean isSrvProtocol;
    private String host;
    private int port = -1;

    private String databaseName;

    private final Map<String, String> options = new HashMap<>();

    public LiquibaseUrlBuilder withSrvProtocol() {
        this.isSrvProtocol = true;
        return this;
    }

    public LiquibaseUrlBuilder withHost(String host) {
        this.host = host;
        return this;
    }

    public LiquibaseUrlBuilder withHosts(List<String> hosts) {
        this.host = String.join(",", hosts);
        return this;
    }

    public LiquibaseUrlBuilder withPort(int port) {
        this.port = port;
        return this;
    }

    public LiquibaseUrlBuilder withDatabaseName(String databaseName) {
        this.databaseName = databaseName;
        return this;
    }

    public LiquibaseUrlBuilder withReplicaSet(String replicaSet) {
        if (null != replicaSet && !StringUtils.isBlank(replicaSet)) {
            options.put("replicaSet", replicaSet);
        }
        return this;
    }

    public LiquibaseUrlBuilder withOption(String key, String value) {
        options.put(key, value);
        return this;
    }

    public String build() {
        String query = options.entrySet().stream().map(option -> option.getKey() + '=' + option.getValue()).collect(joining("&"));
        String scheme = isSrvProtocol ? MONGODB_SRV_PREFIX : MONGODB_PREFIX;

        StringBuilder url = new StringBuilder();
        url.append(scheme).append(host);

        if (port != -1) {
            url.append(":").append(port);
        }

        url.append("/").append(databaseName);

        if (query.length() > 0) {
            url.append("?").append(query);
        }

        return url.toString();
    }

    public static String buildFromClient(Environment environment, MongoClientSettings clientSettings) {
        LiquibaseUrlBuilder builder = new LiquibaseUrlBuilder().withDatabaseName(environment.getProperty("management.mongodb.dbname", "gravitee"));

        builder.withHosts(clientSettings.getClusterSettings().getHosts().stream().map(ServerAddress::toString).collect(Collectors.toList()));

        if (null != clientSettings.getCredential() && StringUtils.isNotEmpty(clientSettings.getCredential().getSource())) {
            builder.withOption("authSource", clientSettings.getCredential().getSource());
        }

        return builder.build();
    }

    public static String buildFromUri(String uri) {
        var connectionString = new ConnectionString(uri);

        LiquibaseUrlBuilder builder = new LiquibaseUrlBuilder()
                .withHosts(connectionString.getHosts())
                .withDatabaseName(connectionString.getDatabase())
                .withReplicaSet(connectionString.getRequiredReplicaSetName());

        if (connectionString.isSrvProtocol()) {
            builder.withSrvProtocol();
        }

        if (null != connectionString.getCredential() && StringUtils.isNotEmpty(connectionString.getCredential().getSource())) {
            builder.withOption("authSource", connectionString.getCredential().getSource());
        }

        return builder.build();
    }
}
