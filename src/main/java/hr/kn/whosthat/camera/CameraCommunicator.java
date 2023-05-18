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
    private final String motionAddress;
    private final WebClient cameraHttpClient;

    public CameraCommunicator(WebClient cameraHttpClient,
                              @Value("${camera.address}") String cameraAddress,
                              @Value("${MOTION_ADDRESS}") String motionAddress) {
        this.cameraAddress = cameraAddress;
        this.motionAddress = motionAddress;
        this.cameraHttpClient = cameraHttpClient;
    }

    public byte[] acquireCameraPhoto() {
        return cameraHttpClient.get()
            .uri(cameraAddress)
            .retrieve()
            .bodyToMono(byte[].class)
            .block();
    }

    public Flux<String> acquireCameraMotions() {
        return cameraHttpClient.get()
            .uri(motionAddress)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .retrieve()
            .bodyToFlux(String.class);
    }

}
