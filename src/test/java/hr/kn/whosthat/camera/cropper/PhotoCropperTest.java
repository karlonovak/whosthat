package hr.kn.whosthat.camera.cropper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import origami.Origami;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PhotoCropperTest {

    private final PhotoCropper photoCropper = new PhotoCropper();

    @BeforeAll
    static void initOpenCV() {
        Origami.init();
    }

    @Test
    void cropRectangleFromPhoto() throws IOException {
        var photo = Files.readAllBytes(Paths.get("/home/knovak/Downloads/photo.jpeg"));
        var cropped = photoCropper.removePartOfImage(photo, new Point(2560, 500), new Point(1900, 0));
    }

}
