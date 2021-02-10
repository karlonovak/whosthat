package hr.kn.whosthat.camera.detection;

import reactor.core.publisher.Mono;

public interface PeopleDetector {

    Mono<PeopleDetectionResult> detectPeople(byte[] photo);
}
