package hr.kn.whosthat;

import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.HOGDescriptor;
import org.springframework.stereotype.Service;

@Service
public class PeopleDetector {

    private final HOGDescriptor hog = new HOGDescriptor();

    public PeopleDetector() {
        hog.setSVMDetector(HOGDescriptor.getDefaultPeopleDetector());
    }

    public PeopleDetectionResult detectPeople(String imagePath) {
        var locations = new MatOfRect();
        var weights = new MatOfDouble();
        var img = Imgcodecs.imread(imagePath);

        hog.detectMultiScale(img, locations, weights);

        if (locations.rows() > 0) {
            var locationsArray = locations.toArray();
            for (Rect rect : locationsArray) {
                Imgproc.rectangle(img, rect.tl(), rect.br(), new Scalar(0, 255, 0, 255), 3);
            }
            return PeopleDetectionResult.detected(img);
        } else {
            return PeopleDetectionResult.notDetected();
        }
    }

}
