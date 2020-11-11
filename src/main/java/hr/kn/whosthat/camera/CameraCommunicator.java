package hr.kn.whosthat.camera;

import io.netty.handler.codec.http.HttpMethod;
import me.vzhilin.auth.DigestAuthenticator;
import me.vzhilin.auth.parser.ChallengeResponseParser;
import me.vzhilin.auth.parser.DigestAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.text.ParseException;

@Service
public class CameraCommunicator {

    private final String camUser;
    private final String camPassword;

    private final WebClient webClient;

    public CameraCommunicator(WebClient.Builder builder,
                              @Value("${camera.username}") String camUser,
                              @Value("${camera.password}") String camPassword) {
        this.camUser = camUser;
        this.camPassword = camPassword;
        this.webClient = builder.baseUrl("http://192.168.1.4/ISAPI/Streaming/channels").build();
    }

    public Mono<byte[]> acquireCameraPhoto() {
        var request = webClient.get().uri("/0201/picture");
        return request
                .retrieve()
                .bodyToMono(byte[].class)
                .onErrorResume(WebClientResponseException.Unauthorized.class, error -> {
                    var receivedAuthenticateHeader = error.getHeaders()
                            .getFirst(HttpHeaders.WWW_AUTHENTICATE)
                            .replace("\"FALSE\"", "FALSE");
                    var authenticator = new DigestAuthenticator(camUser, camPassword);
                    try {
                        var crp = new ChallengeResponseParser(receivedAuthenticateHeader).parseChallenge();
                        crp.addAlgorithm(DigestAlgorithm.MD5);
                        authenticator.onResponseReceived(crp, error.getRawStatusCode());
                    } catch (ParseException e) {
                        return Mono.error(e);
                    }

                    var authorizationHeader = authenticator.autorizationHeader(HttpMethod.GET.name(), "/0201/picture");
                    return request
                            .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                            .accept(MediaType.IMAGE_JPEG)
                            .retrieve()
                            .bodyToMono(byte[].class);
                });
    }

}
