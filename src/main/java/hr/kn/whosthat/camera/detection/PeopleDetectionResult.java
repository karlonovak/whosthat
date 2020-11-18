package hr.kn.whosthat.camera.detection;

public class PeopleDetectionResult {

    private boolean peopleDetected;
    private byte[] image;

    private PeopleDetectionResult() {
    }

    public static PeopleDetectionResult detected(byte[] photo) {
        var result = new PeopleDetectionResult();
        result.peopleDetected = true;
        result.image = photo;
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
}
