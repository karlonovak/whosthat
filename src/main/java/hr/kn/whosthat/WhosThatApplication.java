package hr.kn.whosthat;

import hr.kn.whosthat.camera.CameraCommunicator;
import hr.kn.whosthat.camera.detection.PeopleDetector;
import hr.kn.whosthat.notification.TelegramService;
import hr.kn.whosthat.support.FileSystemHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import origami.Origami;

@SpringBootApplication
@EnableScheduling
public class WhosThatApplication {

    @Autowired
    private PeopleDetector peopleDetector;

    @Autowired
    private CameraCommunicator cameraCommunicator;

    @Autowired
    private FileSystemHandler fileSystemHandler;

    @Autowired
    private TelegramService telegramService;

    private long lastNotificationTime = System.currentTimeMillis();

    @Scheduled(fixedDelay = 1000)
    public void contextStartedListener() {
        cameraCommunicator
                .acquireCameraPhoto()
                .map(fileSystemHandler::saveSnapToDisk)
                .doOnError(System.out::println)
                .subscribe(photoPath -> peopleDetector
                        .detectPeople(photoPath)
                        .filter(peopleDetectionResult -> peopleDetectionResult.arePeopleDetected() && minutePassedSinceLastNotification())
                        .map(peopleDetectionResult -> fileSystemHandler.saveDetectedSnapToDisk(peopleDetectionResult.getImage()))
                        .subscribe(detectedPhotoPath -> {
                            telegramService.sendPhoto(detectedPhotoPath, "Somebody's at the door!");
                            lastNotificationTime = System.currentTimeMillis();
                        }));
    }

    private boolean minutePassedSinceLastNotification() {
        return System.currentTimeMillis() - lastNotificationTime > 60_000;
    }

    public static void main(String[] args) {
        Origami.init();
        SpringApplication.run(WhosThatApplication.class);
    }

}
