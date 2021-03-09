package hr.kn.whosthat.camera.detection;

import org.opencv.core.*;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Primary
@Service
public class PeopleDetectorYOLO implements PeopleDetector {

    private final Logger logger = LoggerFactory.getLogger(PeopleDetectorYOLO.class);

    private final Net net;
    private final List<String> outBlobNames;
    private final String workDir;

    public PeopleDetectorYOLO(@Value("${yolo.weights}") String yoloWeights,
                              @Value("${yolo.config}") String yoloConfig,
                              @Value("${app.work.dir}") String workDir) {
        this.net = Dnn.readNetFromDarknet(yoloConfig, yoloWeights);
        this.outBlobNames = getOutputNames(net);
        this.workDir = workDir;
    }

    private static List<String> getOutputNames(Net net) {
        List<Integer> outLayers = net.getUnconnectedOutLayers().toList();
        List<String> layersNames = net.getLayerNames();

        List<String> names = new ArrayList<>();
        outLayers.forEach((item) -> names.add(layersNames.get(item - 1))); // unfold and create R-CNN layers from the loaded YOLO model
        return names;
    }

    @Override
    public PeopleDetectionResult detectPeople(byte[] photo) {
        var frame = Imgcodecs.imdecode(new MatOfByte(photo), Imgcodecs.IMREAD_COLOR);
        var blob = Dnn.blobFromImage(frame, 0.00392, new Size(608, 608), new Scalar(0), true, false);

        List<Mat> result = new ArrayList<>();
        net.setInput(blob);
        net.forward(result, outBlobNames);

        float confThreshold = 0.85f;
        List<Integer> classIds = new ArrayList<>();
        List<Float> confidences = new ArrayList<>();
        List<Rect> rectangles = new ArrayList<>();
        for (Mat level : result) {
            // each row is a candidate detection, the 1st 4 numbers are
            // [center_x, center_y, width, height], followed by (N-4) class probabilities
            for (int j = 0; j < level.rows(); ++j) {
                Mat row = level.row(j);
                Mat scores = row.colRange(5, level.cols());
                Core.MinMaxLocResult mm = Core.minMaxLoc(scores);
                float confidence = (float) mm.maxVal;
                Point classIdPoint = mm.maxLoc;
                if (confidence > confThreshold) {
                    int centerX = (int) (row.get(0, 0)[0] * frame.cols()); //scaling for drawing the bounding boxes
                    int centerY = (int) (row.get(0, 1)[0] * frame.rows());
                    int width = (int) (row.get(0, 2)[0] * frame.cols());
                    int height = (int) (row.get(0, 3)[0] * frame.rows());
                    int left = centerX - width / 2;
                    int top = centerY - height / 2;

                    classIds.add((int) classIdPoint.x);
                    confidences.add(confidence);
                    rectangles.add(new Rect(left, top, width, height));
                }
            }
        }

        if (classIds.contains(YOLOObjectClass.PERSON.classId)) {
            logger.info("Person detected!");
            float nmsThresh = 0.85f;
            MatOfFloat confs = new MatOfFloat(Converters.vector_float_to_Mat(confidences));
            Rect[] boxesArray = rectangles.toArray(new Rect[0]);
            MatOfRect boxes = new MatOfRect(boxesArray);
            MatOfInt indices = new MatOfInt();
            Dnn.NMSBoxes(boxes, confs, confThreshold, nmsThresh, indices); // We draw the bounding boxes for objects here

            int[] ind = indices.toArray();
            for (int i = 0; i < ind.length; ++i) {
                logger.debug("Confidence is " + confidences.get(i) + " and class is " + classIds.get(i));
                int idx = ind[i];
                Rect box = boxesArray[idx];
                Imgproc.rectangle(frame, box.tl(), box.br(), new Scalar(0, 0, 255), 2);

            }
            Imgcodecs.imwrite(workDir + "/detect_" + System.currentTimeMillis() + ".jpg", frame);
            return PeopleDetectionResult.detected(photo, Collections.max(confidences));
        }

        return PeopleDetectionResult.notDetected();
    }

    enum YOLOObjectClass {
        PERSON(0);

        private final Integer classId;

        YOLOObjectClass(Integer classId) {
            this.classId = classId;
        }
    }

}
