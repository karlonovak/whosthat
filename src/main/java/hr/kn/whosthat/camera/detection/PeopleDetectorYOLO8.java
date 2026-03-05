package hr.kn.whosthat.camera.detection;

import ai.djl.inference.Predictor;
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
import java.nio.file.Paths;

@Service
@Primary
public class PeopleDetectorYOLO8 implements PeopleDetector {

    private Predictor<Image, DetectedObjects> predictor;

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
                                        .optThreshold(0.85f)
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
                    .resize(640, 640, false);

            DetectedObjects detections = predictor.predict(img);
            System.out.println("Total items detected: " + detections.items().size());
            for (var obj : detections.items()) {
                System.out.printf("%s: %.2f%n", obj.getClassName(), obj.getProbability());
            }

            boolean personDetected = detections.items()
                    .stream()
                    .anyMatch(o -> "person".equalsIgnoreCase(o.getClassName()));

            if (personDetected) {
                return PeopleDetectionResult.detected(photo, 1.0f);
            }

            return PeopleDetectionResult.notDetected();

        } catch (Exception e) {
            e.printStackTrace();
            return PeopleDetectionResult.notDetected();
        }
    }
}