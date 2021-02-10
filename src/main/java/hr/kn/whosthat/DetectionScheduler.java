package hr.kn.whosthat;

import hr.kn.whosthat.camera.CameraCommunicator;
import hr.kn.whosthat.camera.detection.PeopleDetectionResult;
import hr.kn.whosthat.camera.detection.PeopleDetector;
import hr.kn.whosthat.notification.TelegramService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DetectionScheduler {

    private final PeopleDetector peopleDetector;

    private final CameraCommunicator cameraCommunicator;

    private final TelegramService telegramService;

    public DetectionScheduler(PeopleDetector peopleDetector,
                              CameraCommunicator cameraCommunicator,
                              TelegramService telegramService) {
        this.peopleDetector = peopleDetector;
        this.cameraCommunicator = cameraCommunicator;
        this.telegramService = telegramService;
    }

    @Scheduled(fixedDelay = 3000)
    public void contextStartedListener() {
        cameraCommunicator
                .acquireCameraPhoto()
                .doOnError(Throwable::printStackTrace)
                .subscribe(photo -> peopleDetector
                        .detectPeople(photo)
                        .filter(PeopleDetectionResult::arePeopleDetected)
                        .map(PeopleDetectionResult::getImage)
                        .subscribe(image -> telegramService.sendPhoto(image, "Somebody's at the door!")));
    }

}
