package hr.kn.whosthat;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.HOGDescriptor;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import origami.Origami;

@SpringBootApplication
public class WhosThatApplication {

    public static void main(String[] args) {
        Origami.init();
        Mat mat = Mat.eye(3, 3, CvType.CV_8UC1);
        System.out.println("mat = " + mat.dump());
        HOGDescriptor hog = new HOGDescriptor();
        hog.setSVMDetector(HOGDescriptor.getDefaultPeopleDetector());

        MatOfRect locations = new MatOfRect();
        MatOfDouble weights = new MatOfDouble();
        Mat img = Imgcodecs.imread("/home/knovak/Pictures/pedestrians.jpg");
        hog.detectMultiScale(img, locations, weights);

        if (locations != null && locations.rows() > 0) {
            System.out.println("Yey!");
            Rect[] locationsArray = locations.toArray();
            for (int i = 0; i < locationsArray.length; i++) {
                System.out.println("Ped " + i);
                Imgproc.rectangle(img, locationsArray[i].tl(), locationsArray[i].br(), new Scalar(0, 255, 0, 255), 3);
            }
        }
//        SpringApplication.run(WhosThatApplication.class, args);
    }

}
