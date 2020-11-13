package hr.kn.whosthat.camera.detection;

import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
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

    public Mono<PeopleDetectionResult> detectPeople(String imagePath) {
        return Mono.fromCallable(() -> {
            var locations = new MatOfRect();
            var weights = new MatOfDouble();
            var img = Imgcodecs.imread(imagePath);

//            var roi = new Rect(207, 15, 728, 926);
            var cropped = img;
//            var cropped = new Mat(img, roi);
//            Imgcodecs.imwrite("/home/knovak/Pictures/opencv/cropped.jpg", cropped);

            hog.detectMultiScale(cropped, locations, weights);
            if (locations.rows() > 0 && preciseWeightFound(weights)) {
                var locationsArray = locations.toArray();
                for (Rect rect : locationsArray) {
                    Imgproc.rectangle(cropped, rect.tl(), rect.br(), new Scalar(0, 255, 0, 255), 3);
                }
                return PeopleDetectionResult.detected(cropped);
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
