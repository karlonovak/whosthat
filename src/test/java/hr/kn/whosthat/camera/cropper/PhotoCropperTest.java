package hr.kn.whosthat.camera.cropper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import origami.Origami;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class PhotoCropperTest {

    private final PhotoCropper photoCropper = new PhotoCropper();

    @BeforeAll
    static void initOpenCV() {
        Origami.init();
    }

    @Test
    void cropRectangleFromPhoto() throws IOException {
        var photo = Files.readAllBytes(Paths.get("/home/knovak/Downloads/photo.jpeg"));
        var rect1 = new Rect(new Point(2688, 480), new Point(1980, 0));
        var rect2 = new Rect(new Point(2688, 1520), new Point(2400, 0));
        var cropped = photoCropper.removePartsOfImage(photo, List.of(rect1, rect2));
        Files.write(Paths.get("/home/knovak/Downloads/photo2.jpeg"), cropped);
    }

}
