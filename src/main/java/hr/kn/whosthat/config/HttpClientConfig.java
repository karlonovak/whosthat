package hr.kn.whosthat.config;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.util.TimeValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.HttpComponentsClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.MalformedURLException;
import java.net.URL;

@Configuration
public class HttpClientConfig {

    @Bean
    public WebClient cameraHttpClient(@Value("${camera.username}") String camUser,
                                      @Value("${camera.password}") String camPassword,
                                      @Value("${camera.address}") String camAddress) throws MalformedURLException {
        var httpClient = createHttpClientForCamera(camUser, camPassword, camAddress);
        var clientConnector = new HttpComponentsClientHttpConnector(httpClient);

        return WebClient.builder()
                .clientConnector(clientConnector)
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer
                                .defaultCodecs()
                                .maxInMemorySize(16 * 1024 * 1024))
                        .build())
                .build();
    }

    @Bean
    public WebClient motionHttpClient(@Value("${camera.username}") String camUser,
                                      @Value("${camera.password}") String camPassword,
                                      @Value("${camera.address}") String camAddress) throws MalformedURLException {
        var httpClient = createHttpClientForCamera(camUser, camPassword, camAddress);
        var clientConnector = new HttpComponentsClientHttpConnector(httpClient);

        return WebClient.builder()
            .clientConnector(clientConnector)
            .exchangeStrategies(ExchangeStrategies.builder()
                .codecs(configurer -> configurer
                    .defaultCodecs()
                    .maxInMemorySize(16 * 1024 * 1024))
                .build())
            .build();
    }

    private CloseableHttpAsyncClient createHttpClientForCamera(String camUser, String camPassword, String camAddress) throws MalformedURLException {
        var provider = createCredentialsProvider(camUser, camPassword, camAddress);

        var connectionManager = PoolingAsyncClientConnectionManagerBuilder.create()
                .setConnPoolPolicy(PoolReusePolicy.LIFO)
                .setMaxConnTotal(100)
                .setMaxConnPerRoute(100)
                .setConnectionTimeToLive(TimeValue.ofDays(10_000L))
                .build();

        return HttpAsyncClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultCredentialsProvider(provider)
                .build();
    }

    private CredentialsProvider createCredentialsProvider(String camUser, String camPassword, String camAddress) throws MalformedURLException {
        var url = new URL(camAddress);
        var provider = new BasicCredentialsProvider();
        var credentials = new UsernamePasswordCredentials(camUser, camPassword.toCharArray());
        provider.setCredentials(new AuthScope(url.getHost(), url.getPort()), credentials);
        return provider;
    }

}
