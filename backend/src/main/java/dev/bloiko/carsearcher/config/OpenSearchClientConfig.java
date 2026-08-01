package dev.bloiko.carsearcher.config;

import org.apache.hc.core5.http.HttpHost;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires up the {@link OpenSearchClient} used to talk to the local OpenSearch
 * cluster started via {@code docker-compose.yml}. No auth: the compose file
 * runs with {@code plugins.security.disabled=true}, so a plain HTTP transport
 * is enough for local dev.
 */
@Configuration
public class OpenSearchClientConfig {

    private final String host;
    private final int port;
    private final String scheme;

    public OpenSearchClientConfig(
            @Value("${opensearch.host}") String host,
            @Value("${opensearch.port}") int port,
            @Value("${opensearch.scheme}") String scheme) {
        this.host = host;
        this.port = port;
        this.scheme = scheme;
    }

    @Bean
    public OpenSearchTransport openSearchTransport() {
        HttpHost httpHost = new HttpHost(scheme, host, port);
        return ApacheHttpClient5TransportBuilder.builder(httpHost).build();
    }

    @Bean
    public OpenSearchClient openSearchClient(OpenSearchTransport openSearchTransport) {
        return new OpenSearchClient(openSearchTransport);
    }
}
