package hr.kn.whosthat.camera.cropper;

import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;

@Service
public class PhotoCropper {

    public byte[] removePartOfImage(byte[] image, Point pt1, Point pt2) {
        var mat = Imgcodecs.imdecode(new MatOfByte(image), Imgcodecs.IMREAD_COLOR);
        Imgproc.rectangle(mat, pt1, pt2, new Scalar(0, 0, 0), -1);
//        Imgcodecs.imwrite("/home/knovak/Downloads/photo2.jpeg", mat);
        int length = (int) (mat.total() * mat.elemSize());
        byte[] buffer = new byte[length];
        mat.get(0, 0, buffer);
        return buffer;
    }

}
