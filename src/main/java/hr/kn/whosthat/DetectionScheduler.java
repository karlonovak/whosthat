package hr.kn.whosthat;

import hr.kn.whosthat.camera.CameraCommunicator;
import hr.kn.whosthat.camera.cropper.PhotoCropper;
import hr.kn.whosthat.camera.detection.PeopleDetector;
import hr.kn.whosthat.notification.TelegramService;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

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
                .subscribe(photo -> {
                    var rect1 = new Rect(new Point(2688, 480), new Point(1980, 0));
                    var rect2 = new Rect(new Point(2688, 1520), new Point(2400, 0));
                    var cropped = photoCropper.removePartsOfImage(photo, List.of(rect1, rect2));

                    var detection = peopleDetector.detectPeople(cropped);
                    if (detection.arePeopleDetected()) {
                        var message = String.format("A person! %.2f%% sure.", detection.getConfidence() * 100);
                        telegramService.sendPhoto(photo, message);
                    }
                });
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
