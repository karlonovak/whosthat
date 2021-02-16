package hr.kn.whosthat;

import hr.kn.whosthat.camera.CameraCommunicator;
import hr.kn.whosthat.camera.detection.PeopleDetectionResult;
import hr.kn.whosthat.camera.detection.PeopleDetector;
import hr.kn.whosthat.notification.TelegramService;
import org.opencv.core.*;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class DetectionScheduler {

    private final Logger logger = LoggerFactory.getLogger(DetectionScheduler.class);

    private final CameraCommunicator cameraCommunicator;
    private final TelegramService telegramService;
    private final PeopleDetector peopleDetector;

    private Long lastNotificationTime = 0L;

    public DetectionScheduler(CameraCommunicator cameraCommunicator,
                              TelegramService telegramService,
                              PeopleDetector peopleDetector) throws IOException {
        this.cameraCommunicator = cameraCommunicator;
        this.telegramService = telegramService;
        this.peopleDetector = peopleDetector;
        startDarknetDetector();
    }

    public void startHikvisionMotionDetector() {
        cameraCommunicator
                .acquireCameraMotions()
                .doOnError(Throwable::printStackTrace)
                .filter(motion -> motion.contains("<eventType>VMD</eventType>"))
                .delayElements(Duration.ofSeconds(2))
                .filter(this::minutePassedSinceLastNotification)
                .subscribe(motionDetected -> {
                    lastNotificationTime = System.currentTimeMillis();
                    cameraCommunicator.acquireCameraPhoto()
                            .subscribe(photo -> telegramService.sendPhoto(photo, "Somebody's at the door!"));
                });
    }

    private static List<String> getOutputNames(Net net) {
        List<String> names = new ArrayList<>();

        List<Integer> outLayers = net.getUnconnectedOutLayers().toList();
        List<String> layersNames = net.getLayerNames();

        outLayers.forEach((item) -> names.add(layersNames.get(item - 1)));//unfold and create R-CNN layers from the loaded YOLO model//
        return names;
    }

    private boolean minutePassedSinceLastNotification(String motion) {
        return System.currentTimeMillis() - lastNotificationTime > 60_000;
    }

    //    @Scheduled(fixedDelay = 1000)
    public void startDarknetDetector() throws IOException {
        String modelWeights = "/home/knovak/Workspaces/darknet/yolov3.weights"; //Download and load only wights for YOLO , this is obtained from official YOLO site//
        String modelConfiguration = "/home/knovak/Workspaces/darknet/cfg/yolov3.cfg";//Download and load cfg file for YOLO , can be obtained from official site//
        Net net = Dnn.readNetFromDarknet(modelConfiguration, modelWeights);
        List<Mat> result = new ArrayList<>();
        List<String> outBlobNames = getOutputNames(net);

        var photo = Files.readAllBytes(Paths.get("/home/knovak/Downloads/kamera.jpg"));
        Mat frame = Imgcodecs.imdecode(new MatOfByte(photo), Imgcodecs.IMREAD_COLOR);
        Mat blob = Dnn.blobFromImage(frame, 0.00392, new Size(416, 416), new Scalar(0), true, false);

        System.out.println(blob.size());

        net.setInput(blob);
        net.forward(result, outBlobNames);

        outBlobNames.forEach(System.out::println);
        result.forEach(System.out::println);

        float confThreshold = 0.6f;
        List<Integer> clsIds = new ArrayList<>();
        List<Float> confs = new ArrayList<>();
        List<Rect> rects = new ArrayList<>();
        for (int i = 0; i < result.size(); ++i) {
            // each row is a candidate detection, the 1st 4 numbers are
            // [center_x, center_y, width, height], followed by (N-4) class probabilities
            Mat level = result.get(i);
            for (int j = 0; j < level.rows(); ++j) {
                Mat row = level.row(j);
                Mat scores = row.colRange(5, level.cols());
                Core.MinMaxLocResult mm = Core.minMaxLoc(scores);
                float confidence = (float) mm.maxVal;
                Point classIdPoint = mm.maxLoc;
                if (confidence > confThreshold) {
                    int centerX = (int) (row.get(0, 0)[0] * frame.cols()); //scaling for drawing the bounding boxes//
                    int centerY = (int) (row.get(0, 1)[0] * frame.rows());
                    int width = (int) (row.get(0, 2)[0] * frame.cols());
                    int height = (int) (row.get(0, 3)[0] * frame.rows());
                    int left = centerX - width / 2;
                    int top = centerY - height / 2;

                    clsIds.add((int) classIdPoint.x);
                    confs.add((float) confidence);
                    rects.add(new Rect(left, top, width, height));
                }
            }
        }


        if (rects.size() > 0) {
            float nmsThresh = 0.5f;
            MatOfFloat confidences = new MatOfFloat(Converters.vector_float_to_Mat(confs));
            Rect[] boxesArray = rects.toArray(new Rect[0]);
            MatOfRect boxes = new MatOfRect(boxesArray);
            MatOfInt indices = new MatOfInt();
            Dnn.NMSBoxes(boxes, confidences, confThreshold, nmsThresh, indices); //We draw the bounding boxes for objects here//

            int[] ind = indices.toArray();
            int j = 0;
            for (int i = 0; i < ind.length; ++i) {
                int idx = ind[i];
                Rect box = boxesArray[idx];
                Imgproc.rectangle(frame, box.tl(), box.br(), new Scalar(0, 0, 255), 2);
                Imgcodecs.imwrite("/home/knovak/Downloads/detect.jpg", frame);
                //i=j;

                System.out.println(idx);
            }
        }

//        cameraCommunicator
//                .acquireCameraPhoto()
//                .doOnError(Throwable::printStackTrace)
//                .subscribe(photo -> {
//                    Mat frame = Imgcodecs.imdecode(new MatOfByte(photo), Imgcodecs.IMREAD_COLOR);
//                    Size sz = new Size(1520,2688);
//                    Mat blob = Dnn.blobFromImage(frame, 0.00392, sz, new Scalar(0), true, false);
//                    net.setInput(blob);
//                    net.forward(result, outBlobNames);
//
//                    outBlobNames.forEach(System.out::println);
//                    result.forEach(System.out::println);
//                });
    }

    @Scheduled(fixedDelay = 1000)
    public void startImageMotionDetector() {
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
