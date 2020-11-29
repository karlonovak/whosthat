package hr.kn.whosthat.camera;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class CameraCommunicator {

    private final String CAMERA_URL = "http://192.168.1.4/ISAPI/Streaming/channels/0201/picture";

    private final WebClient cameraHttpClient;

    public CameraCommunicator(WebClient cameraHttpClient) {
        this.cameraHttpClient = cameraHttpClient;
    }

    public Mono<byte[]> acquireCameraPhoto() {
        return cameraHttpClient.get().uri(CAMERA_URL).retrieve().bodyToMono(byte[].class);
    }

}
