package hr.kn.whosthat.camera.detection;

public class PeopleDetectionResult {

    private boolean peopleDetected;
    private byte[] image;
    private float confidence;

    private PeopleDetectionResult() {
    }

    public static PeopleDetectionResult detected(byte[] image, float confidence) {
        var result = new PeopleDetectionResult();
        result.peopleDetected = true;
        result.image = image;
        result.confidence = confidence;
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

    public byte[] getImage() {
        return image;
    }

    public float getConfidence() {
        return confidence;
    }
}
