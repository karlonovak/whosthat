package hr.kn.whosthat.camera.cropper;

import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PhotoCropper {

    public byte[] removePartsOfImage(byte[] image, List<Rect> rectangles) {
        var mat = Imgcodecs.imdecode(new MatOfByte(image), Imgcodecs.IMREAD_COLOR);
        for(Rect rect : rectangles) {
            Imgproc.rectangle(mat, rect.br(), rect.tl(), new Scalar(0, 0, 0), -1);
        }
        int length = (int) (mat.total() * mat.elemSize());
        byte[] buffer = new byte[length];
        mat.get(0, 0, buffer);
        MatOfByte mem = new MatOfByte();
        Imgcodecs.imencode(".jpg", mat, mem);
        return mem.toArray();
    }

}
