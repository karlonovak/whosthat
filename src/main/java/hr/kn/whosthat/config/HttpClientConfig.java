package hr.kn.whosthat.config;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.eclipse.jetty.client.HttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.HttpComponentsClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class HttpClientConfig {

    @Bean
    public WebClient cameraHttpClient(@Value("${camera.username}") String camUser,
                                      @Value("${camera.password}") String camPassword) {
        var httpClient = createHttpClientForCamera(camUser, camPassword);
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

    private HttpClient createJettyClient(String camUser, String camPassword) throws Exception {
        var httpClient = new HttpClient();
        var authStore = httpClient.getAuthenticationStore();
        httpClient.start();
        return httpClient;
    }

    private CloseableHttpAsyncClient createHttpClientForCamera(String camUser, String camPassword) {
        CredentialsProvider provider = createCredentialsProvider(camUser, camPassword);
        HttpAsyncClientBuilder clientBuilder = HttpAsyncClients.custom();
        return clientBuilder.setDefaultCredentialsProvider(provider).build();
    }

    private CredentialsProvider createCredentialsProvider(String camUser, String camPassword) {
        BasicCredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(camUser, camPassword.toCharArray());
        provider.setCredentials(new AuthScope("192.168.6.20", 65002), credentials);
        return provider;
    }

}
