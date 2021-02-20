package hr.kn.whosthat;

import hr.kn.whosthat.camera.CameraCommunicator;
import hr.kn.whosthat.camera.cropper.PhotoCropper;
import hr.kn.whosthat.camera.detection.PeopleDetector;
import hr.kn.whosthat.notification.TelegramService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class DetectionScheduler {

    private final CameraCommunicator cameraCommunicator;
    private final TelegramService telegramService;
    private final PeopleDetector peopleDetector;
    private final PhotoCropper photoCropper;

    public DetectionScheduler(CameraCommunicator cameraCommunicator,
                              TelegramService telegramService,
                              PeopleDetector peopleDetector,
                              PhotoCropper photoCropper,
                              @Value("${camera.frequency}") Integer camFrequency) {
        this.cameraCommunicator = cameraCommunicator;
        this.telegramService = telegramService;
        this.peopleDetector = peopleDetector;
        this.photoCropper = photoCropper;
        startDetector(camFrequency);
    }

    public void startDetector(Integer camFrequency) {
        new ScheduledThreadPoolExecutor(1).scheduleAtFixedRate(
                () -> cameraCommunicator
                        .acquireCameraPhoto()
                        .subscribe(camSnap -> {
                            var croppedSnap = photoCropper.removePartsOfImage(camSnap);
                            var detection = peopleDetector.detectPeople(croppedSnap);
                            if (detection.arePeopleDetected()) {
                                var message = String.format("A person! %.2f%% sure.", detection.getConfidence() * 100);
                                telegramService.sendPhoto(camSnap, message);
                            }
                        }),
                0, camFrequency, TimeUnit.SECONDS);
    }

}
