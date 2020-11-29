package hr.kn.whosthat.config;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.util.DigestAuthentication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.JettyClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;

@Configuration
public class HttpClientConfig {

    @Bean
    public WebClient cameraHttpClient(@Value("${camera.username}") String camUser,
                               @Value("${camera.password}") String camPassword) throws Exception {
        var httpClient = createJettyClient(camUser, camPassword);
        var clientConnector = new JettyClientHttpConnector(httpClient);
        return WebClient.builder().clientConnector(clientConnector).build();
    }

    private HttpClient createJettyClient(String camUser, String camPassword) throws Exception {
        var httpClient = new HttpClient();
        var authStore = httpClient.getAuthenticationStore();
        authStore.addAuthentication(new DigestAuthentication(
                new URI("http://192.168.1.4"), "b2a34f4333de921404444389", camUser, camPassword));
        httpClient.start();
        return httpClient;
    }

}
