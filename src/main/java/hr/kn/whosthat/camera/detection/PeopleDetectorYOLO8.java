package hr.kn.whosthat.camera.detection;

import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.translator.YoloV8Translator;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Primary
public class PeopleDetectorYOLO8 implements PeopleDetector {

    private final Predictor<Image, DetectedObjects> predictor;

    public PeopleDetectorYOLO8() throws Exception {
        Criteria<Image, DetectedObjects> criteria =
                Criteria.builder()
                        .setTypes(Image.class, DetectedObjects.class)
                        .optModelPath(Paths.get(
                                getClass().getClassLoader()
                                        .getResource("yolov8s.torchscript")
                                        .toURI()))
                        .optEngine("PyTorch")
                        .optTranslator(
                                YoloV8Translator.builder()
                                        .optThreshold(0.70f)
                                        .build())
                        .optProgress(new ProgressBar())
                        .build();

        ZooModel<Image, DetectedObjects> model = criteria.loadModel();
        predictor = model.newPredictor();
    }

    @Override
    public PeopleDetectionResult detectPeople(byte[] photo) {

        try {
            Image img = ImageFactory.getInstance()
                    .fromInputStream(new ByteArrayInputStream(photo))
                    .resize(1280, 1280, false);

            img.save(new FileOutputStream("/Users/knovak/Downloads/test.jpg"), "jpg");

            DetectedObjects detections = predictor.predict(img);
            for (var obj : detections.items()) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM.dd.yyyy HH:mm:ss"));
                String line = "[" + timestamp + "] " + obj.getClassName() + " " + obj.getProbability() + System.lineSeparator();
                Files.writeString(
                        Paths.get("/Users/knovak/Downloads/detected.txt"),
                        line,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND
                );
            }

            var detectedPeople = detections.items()
                    .stream()
                    .filter(o -> "person".equalsIgnoreCase(o.getClassName()))
                    .toList();

            if (!detectedPeople.isEmpty()) {
                var maxConfidence = detectedPeople
                        .stream()
                        .map(Classifications.Classification::getProbability)
                        .max(Double::compare)
                        .orElse(0.0);

                return PeopleDetectionResult.detected(photo, maxConfidence.floatValue());
            }

            return PeopleDetectionResult.notDetected();

        } catch (Exception e) {
            e.printStackTrace();
            return PeopleDetectionResult.notDetected();
        }
    }
}