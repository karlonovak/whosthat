package hr.kn.whosthat.camera;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CameraCommunicator {

    private final String CAMERA_IMAGE = "http://192.168.6.20:65002/ISAPI/Streaming/channels/101/picture";

    private final String CAMERA_MOTION = "http://192.168.6.20:65002/ISAPI/Event/notification/alertStream";

    private final WebClient cameraHttpClient;

    public CameraCommunicator(WebClient cameraHttpClient) {
        this.cameraHttpClient = cameraHttpClient;
    }

    public Mono<byte[]> acquireCameraPhoto() {
        return cameraHttpClient.get()
                .uri(CAMERA_IMAGE)
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
