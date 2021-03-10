package hr.kn.whosthat.camera.detection;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PeopleDetectorMotion implements PeopleDetector {

    private final Logger logger = LoggerFactory.getLogger(PeopleDetectorMotion.class);

    private Mat frame = new Mat();
    private Mat lastFrame = new Mat();
    private Mat gray = new Mat();
    private Mat frameDelta = new Mat();
    private Mat thresh = new Mat();

    private Integer curr = 0;

    @Override
    public PeopleDetectionResult detectPeople(byte[] photo) {
        frame = Imgcodecs.imdecode(new MatOfByte(photo), Imgcodecs.IMREAD_COLOR);
        var roi = new Rect(1000, 15, 920, 1500);
        var cropped = new Mat(frame, roi);

        if (curr == 0) {
            Imgproc.cvtColor(cropped, lastFrame, Imgproc.COLOR_BGR2GRAY);
            Imgproc.GaussianBlur(lastFrame, lastFrame, new Size(21, 21), 0);
            curr++;
            return PeopleDetectionResult.notDetected();
        }

        Imgproc.cvtColor(cropped, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(gray, gray, new Size(21, 21), 0);

        //compute difference between first frame and current frame
        Core.absdiff(lastFrame, gray, frameDelta);
        Imgproc.threshold(frameDelta, thresh, 25, 255, Imgproc.THRESH_BINARY);

        List<MatOfPoint> cnts = new ArrayList<>();
        Imgproc.dilate(thresh, thresh, new Mat(), new Point(-1, -1), 2);
        Imgproc.findContours(thresh, cnts, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        for (MatOfPoint cnt : cnts) {
            var contourArea = Imgproc.contourArea(cnt);
            logger.debug("Motion detected with contourArea {}", contourArea);
            if (contourArea < 100_000) {
                continue;
            }

            curr++;
            Imgproc.cvtColor(cropped, lastFrame, Imgproc.COLOR_BGR2GRAY);
            Imgproc.GaussianBlur(lastFrame, lastFrame, new Size(21, 21), 0);
            return PeopleDetectionResult.detected(photo, 0.0f);
        }

        return PeopleDetectionResult.notDetected();
    }

}
