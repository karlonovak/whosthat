package hr.kn.whosthat;

import hr.kn.whosthat.camera.CameraCommunicator;
import hr.kn.whosthat.camera.detection.PeopleDetector;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import origami.Origami;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootApplication
@EnableScheduling
public class WhosThatApplication {

    @Autowired
    private PeopleDetector peopleDetector;

    @Autowired
    private CameraCommunicator cameraCommunicator;

    @Scheduled(fixedDelay = 1000)
    public void contextStartedListener() {
        cameraCommunicator
                .acquireCameraPhoto()
                .doOnError(e -> System.out.println(e))
                .subscribe(photo -> {
                    try {
                        Files.write(Paths.get("/home/knovak/Pictures/opencv/kamera.jpg"), photo);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    peopleDetector
                            .detectPeople("/home/knovak/Pictures/opencv/kamera.jpg")
                            .subscribe(peopleDetectionResult -> {
                                if (peopleDetectionResult.arePeopleDetected()) {
                                    Imgcodecs.imwrite("/home/knovak/Pictures/opencv/"
                                            + System.currentTimeMillis() +".jpg", peopleDetectionResult.getImage());
                                }
                            });
                });
    }

    public static void main(String[] args) {
        Origami.init();
        SpringApplication.run(WhosThatApplication.class);
    }

}
