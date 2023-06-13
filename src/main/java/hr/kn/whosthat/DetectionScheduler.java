package hr.kn.whosthat;

import hr.kn.whosthat.camera.CameraCommunicator;
import hr.kn.whosthat.camera.cropper.PhotoCropper;
import hr.kn.whosthat.camera.detection.PeopleDetector;
import hr.kn.whosthat.notification.TelegramService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class DetectionScheduler {

    private final CameraCommunicator cameraCommunicator;
    private final TelegramService telegramService;
    private final PeopleDetector peopleDetector;
    private final PhotoCropper photoCropper;

    private final Logger logger = LoggerFactory.getLogger(DetectionScheduler.class);

    private final AtomicLong motionThresh = new AtomicLong(0L);

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
            startMotionObserver(camFrequency);
        } else {
            logger.info("Starting observer in scheduled mode...");
            new ScheduledThreadPoolExecutor(1).scheduleAtFixedRate(
                this::executeDetection, 0, camFrequency, TimeUnit.SECONDS);
        }
    }

    private void startMotionObserver(Integer camFrequency) {
        logger.info("Starting observer in motion mode...");
        cameraCommunicator
            .acquireCameraMotions()
            .subscribeOn(Schedulers.fromExecutor(Executors.newSingleThreadExecutor()))
            .doOnError(Throwable::printStackTrace)
            .doOnCancel(() -> logger.info("Motion stream canceled..."))
            .doOnComplete(() -> logger.info("Motion stream completed..."))
            .doOnTerminate(() -> {
                logger.info("Motion stream terminated...");
                startMotionObserver(camFrequency);
            })
            .filter(line -> line.contains("VMD"))
            .map(m -> System.currentTimeMillis())
            .subscribeOn(Schedulers.fromExecutor(Executors.newSingleThreadExecutor()))
            .subscribe(timestamp -> {
                if (timestamp - motionThresh.get() > (camFrequency * 1000)) {
                    if (executeDetection()) { // detection delays next detection for 60 seconds
                        motionThresh.set(timestamp + 60_000);
                    } else {
                        motionThresh.set(timestamp);
                    }
                }
            });
    }

    public boolean executeDetection() {
        try {
            var start = System.currentTimeMillis();
            byte[] camSnap = cameraCommunicator.acquireCameraPhoto();
            logger.info("Image size in bytes is {}", camSnap.length);
            logger.info("Cropping in progress..");
            var croppedSnap = photoCropper.removePartsOfImage(camSnap);
            logger.info("Cropping done");
            var detection = peopleDetector.detectPeople(croppedSnap);
            logger.info("Processing took {}ms", System.currentTimeMillis() - start);

            System.gc();

            if (detection.arePeopleDetected()) {
                var message = String.format("A person! %.2f%% sure.", detection.getConfidence() * 100);
                telegramService.sendPhoto(camSnap, message);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
