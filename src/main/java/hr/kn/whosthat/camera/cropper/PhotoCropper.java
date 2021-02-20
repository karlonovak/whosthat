package hr.kn.whosthat.camera.cropper;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PhotoCropper {

    private final List<Rect> cropRectangles;

    public PhotoCropper(@Value("${camera.crop.rectangles}") String cropRectangles) {
        this.cropRectangles = generateCropRectangles(cropRectangles);
    }

    private List<Rect> generateCropRectangles(String cropRectangles) {
        var rects = new ArrayList<Rect>();
        if (cropRectangles != null) {
            var rectangleCoordinates = cropRectangles.split("_");
            for (String rectangleCoordinate : rectangleCoordinates) {
                var firstCoordinate = rectangleCoordinate.split(",")[0];
                var secondCoordinate = rectangleCoordinate.split(",")[1];

                var firstX = firstCoordinate.split("x")[0];
                var firstY = firstCoordinate.split("x")[1];
                var secondX = secondCoordinate.split("x")[0];
                var secondY = secondCoordinate.split("x")[1];

                rects.add(new Rect(
                        new Point(Integer.parseInt(firstX), Integer.parseInt(firstY)),
                        new Point(Integer.parseInt(secondX), Integer.parseInt(secondY)))
                );
            }
        }
        return rects;
    }

    public byte[] removePartsOfImage(byte[] image) {
        var mat = Imgcodecs.imdecode(new MatOfByte(image), Imgcodecs.IMREAD_COLOR);
        for (Rect rectangle : cropRectangles) {
            Imgproc.rectangle(mat, rectangle.br(), rectangle.tl(), new Scalar(0, 0, 0), -1);
        }
        return matToByte(mat);
    }

    private byte[] matToByte(Mat mat) {
        int length = (int) (mat.total() * mat.elemSize());
        byte[] buffer = new byte[length];
        mat.get(0, 0, buffer);
        MatOfByte mem = new MatOfByte();
        Imgcodecs.imencode(".jpg", mat, mem);
        return mem.toArray();
    }
}
