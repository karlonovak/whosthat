package hr.kn.whosthat.camera.detection;

public interface PeopleDetector {

    PeopleDetectionResult detectPeople(byte[] photo);

}
