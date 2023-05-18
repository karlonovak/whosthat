package hr.kn.whosthat;

import hr.kn.whosthat.camera.CameraCommunicator;
import hr.kn.whosthat.camera.cropper.PhotoCropper;
import hr.kn.whosthat.camera.detection.PeopleDetector;
import hr.kn.whosthat.notification.TelegramService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Component
public class DetectionScheduler {

    private final CameraCommunicator cameraCommunicator;
    private final TelegramService telegramService;
    private final PeopleDetector peopleDetector;
    private final PhotoCropper photoCropper;

    private final Logger logger = LoggerFactory.getLogger(DetectionScheduler.class);

    private Long motionThresh = 0L;

    public DetectionScheduler(CameraCommunicator cameraCommunicator,
                              TelegramService telegramService,
                              PeopleDetector peopleDetector,
                              PhotoCropper photoCropper,
                              @Value("${camera.frequency}") Integer camFrequency,
                              @Value("${TRIGGER_TYPE}") String triggerType) {
        this.cameraCommunicator = cameraCommunicator;
        this.telegramService = telegramService;
        this.peopleDetector = peopleDetector;
        this.photoCropper = photoCropper;

        if ("motion".equals(triggerType)) {
            startMotionObserver();
        } else {
            logger.info("Starting observer in scheduled mode...");
            new ScheduledThreadPoolExecutor(1).scheduleAtFixedRate(
                this::processSnap, 0, camFrequency, TimeUnit.SECONDS);
        }
    }

    private void startMotionObserver() {
        logger.info("Starting observer in motion mode...");
        cameraCommunicator
            .acquireCameraMotions()
            .doOnError(Throwable::printStackTrace)
//            .doOnCancel(() -> {
//                System.out.println("canceled");
//            })
//            .doOnComplete(() -> {
//                System.out.println("complete");
//            })
            .doOnTerminate(() -> {
                logger.info("Motion stream terminated...");
                startMotionObserver();
            })
            .filter(line -> line.contains("VMD"))
            .subscribe(motionEvent -> {
                var now = System.currentTimeMillis();
                if (now - motionThresh > 3000) {
                    processSnap();
                }
                motionThresh = now;
            });
//            .subscribe(logger::info);
    }

    public void processSnap() {
        try {
            var start = System.currentTimeMillis();
            byte[] camSnap = cameraCommunicator.acquireCameraPhoto();
            logger.info("Image size in bytes is {}", camSnap.length);
            logger.info("Cropping in progress..");
            var croppedSnap = photoCropper.removePartsOfImage(camSnap);
            logger.info("Cropping done");
            var detection = peopleDetector.detectPeople(croppedSnap);
            logger.info("Processing took {}ms", System.currentTimeMillis() - start);
            if (detection.arePeopleDetected()) {
                var message = String.format("A person! %.2f%% sure.", detection.getConfidence() * 100);
                telegramService.sendPhoto(camSnap, message);
            }
            System.gc();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
