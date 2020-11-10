package hr.kn.whosthat;

import org.opencv.core.Mat;

public class PeopleDetectionResult {

    private boolean peopleDetected;
    private Mat image;

    private PeopleDetectionResult() {
    }

    public static PeopleDetectionResult detected(Mat detectedImage) {
        var result = new PeopleDetectionResult();
        result.peopleDetected = true;
        result.image = detectedImage;
        return result;
    }

    public static PeopleDetectionResult notDetected() {
        var result = new PeopleDetectionResult();
        result.peopleDetected = false;
        return result;
    }

    public boolean arePeopleDetected() {
        return peopleDetected;
    }

    public Mat getImage() {
        return image;
    }
}
