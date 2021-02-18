package hr.kn.whosthat;

import hr.kn.whosthat.camera.CameraCommunicator;
import hr.kn.whosthat.camera.cropper.PhotoCropper;
import hr.kn.whosthat.camera.detection.PeopleDetectionResult;
import hr.kn.whosthat.camera.detection.PeopleDetector;
import hr.kn.whosthat.notification.TelegramService;
import org.opencv.core.Point;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DetectionScheduler {

    private final CameraCommunicator cameraCommunicator;
    private final TelegramService telegramService;
    private final PeopleDetector peopleDetector;
    private final PhotoCropper photoCropper;

    public DetectionScheduler(CameraCommunicator cameraCommunicator,
                              TelegramService telegramService,
                              PeopleDetector peopleDetector,
                              PhotoCropper photoCropper) {
        this.cameraCommunicator = cameraCommunicator;
        this.telegramService = telegramService;
        this.peopleDetector = peopleDetector;
        this.photoCropper = photoCropper;
    }

    @Scheduled(fixedDelay = 2000)
    public void startDetector() {
        cameraCommunicator
                .acquireCameraPhoto()
                .map(photo -> photoCropper.removePartOfImage(photo, new Point(2560, 500), new Point(1900, 0)))
                .doOnError(Throwable::printStackTrace)
                .subscribe(photo -> peopleDetector
                        .detectPeople(photo)
                        .filter(PeopleDetectionResult::arePeopleDetected)
                        .map(PeopleDetectionResult::getImage)
                        .subscribe(image -> telegramService.sendPhoto(image, "Somebody's at the door!")));
    }

//    private Long lastNotificationTime = 0L;

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
