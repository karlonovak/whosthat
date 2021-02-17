package hr.kn.whosthat;

import hr.kn.whosthat.camera.CameraCommunicator;
import hr.kn.whosthat.camera.detection.PeopleDetectionResult;
import hr.kn.whosthat.camera.detection.PeopleDetector;
import hr.kn.whosthat.notification.TelegramService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DetectionScheduler {

    private final CameraCommunicator cameraCommunicator;
    private final TelegramService telegramService;
    private final PeopleDetector peopleDetector;

//    private Long lastNotificationTime = 0L;

    public DetectionScheduler(CameraCommunicator cameraCommunicator,
                              TelegramService telegramService,
                              PeopleDetector peopleDetector) {
        this.cameraCommunicator = cameraCommunicator;
        this.telegramService = telegramService;
        this.peopleDetector = peopleDetector;
    }

    @Scheduled(fixedDelay = 2000)
    public void startDetector() {
        cameraCommunicator
                .acquireCameraPhoto()
                .doOnError(Throwable::printStackTrace)
                .subscribe(photo -> peopleDetector
                        .detectPeople(photo)
                        .filter(PeopleDetectionResult::arePeopleDetected)
                        .map(PeopleDetectionResult::getImage)
                        .subscribe(image -> telegramService.sendPhoto(image, "Somebody's at the door!")));
    }

//    public void startHikvisionMotionDetector() {
//        cameraCommunicator
//                .acquireCameraMotions()
//                .doOnError(Throwable::printStackTrace)
//                .filter(motion -> motion.contains("<eventType>VMD</eventType>"))
//                .delayElements(Duration.ofSeconds(2))
//                .filter(this::minutePassedSinceLastNotification)
//                .subscribe(motionDetected -> {
//                    lastNotificationTime = System.currentTimeMillis();
//                    cameraCommunicator.acquireCameraPhoto()
//                            .subscribe(photo -> telegramService.sendPhoto(photo, "Somebody's at the door!"));
//                });
//    }
//
//    private boolean minutePassedSinceLastNotification(String motion) {
//        return System.currentTimeMillis() - lastNotificationTime > 60_000;
//    }

}
