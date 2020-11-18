package hr.kn.whosthat.camera.detection;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.HOGDescriptor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@Service
public class PeopleDetector {

    private final HOGDescriptor hog = new HOGDescriptor();

    public PeopleDetector() {
        hog.setSVMDetector(HOGDescriptor.getDefaultPeopleDetector());
    }

    public Mono<PeopleDetectionResult> detectPeople(byte[] photo) {
        return Mono.fromCallable(() -> {
            var mat = Imgcodecs.imdecode(new MatOfByte(photo), Imgcodecs.IMREAD_COLOR);
            var locations = new MatOfRect();
            var weights = new MatOfDouble();

            hog.detectMultiScale(mat, locations, weights);

            if (locations.rows() > 0 && preciseWeightFound(weights)) {
                var locationsArray = locations.toArray();
                for (Rect rect : locationsArray) {
                    Imgproc.rectangle(mat, rect.tl(), rect.br(), new Scalar(0, 255, 0, 255), 3);
                }
                return PeopleDetectionResult.detected(photo);
            } else {
                return PeopleDetectionResult.notDetected();
            }
        });
    }

    private boolean preciseWeightFound(MatOfDouble weights) {
        return Arrays.stream(weights.toArray())
                .filter(weight -> weight > 0.8)
                .findAny()
                .isPresent();
    }

}
