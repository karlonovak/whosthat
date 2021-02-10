package hr.kn.whosthat.camera.detection;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Primary
@Service
public class PeopleDetectorMotion implements PeopleDetector {

    Mat frame = new Mat();
    Mat lastFrame = new Mat();
    Mat gray = new Mat();
    Mat frameDelta = new Mat();
    Mat thresh = new Mat();

    private Integer curr = 0;

    @Override
    public Mono<PeopleDetectionResult> detectPeople(byte[] photo) {
        return Mono.fromCallable(() -> {
            frame = Imgcodecs.imdecode(new MatOfByte(photo), Imgcodecs.IMREAD_COLOR);
            var roi = new Rect(440, 336, 1400, 1100);
            var cropped = new Mat(frame, roi);
            Imgcodecs.imwrite("/home/knovak/Pictures/opencv/last.jpg", cropped);

            if(curr == 0) {
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

            for(int i=0; i < cnts.size(); i++) {
                if(Imgproc.contourArea(cnts.get(i)) < 500) {
                    continue;
                }

                Imgcodecs.imwrite("/home/knovak/Pictures/opencv/detect" + curr + ".jpg", cropped);
                curr++;
                System.out.println("Motion detected!!!");
                break;
            }

            Imgproc.cvtColor(cropped, lastFrame, Imgproc.COLOR_BGR2GRAY);
            Imgproc.GaussianBlur(lastFrame, lastFrame, new Size(21, 21), 0);

            return PeopleDetectionResult.notDetected();
//            return PeopleDetectionResult.detected(buffer);
        });
    }

}
