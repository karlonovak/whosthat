package hr.kn.whosthat.camera;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CameraCommunicator {

    private final String cameraAddress;
    private final WebClient cameraHttpClient;
    private static final String CAMERA_MOTION = "http://192.168.6.20:65002/ISAPI/Event/notification/alertStream";

    public CameraCommunicator(WebClient cameraHttpClient, @Value("${camera.address}") String cameraAddress) {
        this.cameraAddress = cameraAddress;
        this.cameraHttpClient = cameraHttpClient;
    }

    public Mono<byte[]> acquireCameraPhoto() {
        return cameraHttpClient.get()
                .uri(cameraAddress)
                .retrieve()
                .bodyToMono(byte[].class);
    }

    public Flux<String> acquireCameraMotions() {
        return cameraHttpClient.get()
                .uri(CAMERA_MOTION)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(String.class);
    }

}
