package hr.kn.whosthat.camera.cropper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PhotoCropper {

    private final List<Rectangle> cropRectangles;

    public PhotoCropper(@Value("${camera.crop.rectangles}") String cropRectangles) {
        this.cropRectangles = generateCropRectangles(cropRectangles);
    }

    private List<Rectangle> generateCropRectangles(String cropRectangles) {
        var rects = new ArrayList<Rectangle>();
        if (cropRectangles != null && !cropRectangles.isEmpty()) {
            var rectangleCoordinates = cropRectangles.split("_");
            for (String rectangleCoordinate : rectangleCoordinates) {
                var coords = rectangleCoordinate.split(",");
                var first = coords[0].split("x");
                var second = coords[1].split("x");

                int x1 = Integer.parseInt(first[0]);
                int y1 = Integer.parseInt(first[1]);
                int x2 = Integer.parseInt(second[0]);
                int y2 = Integer.parseInt(second[1]);

                rects.add(new Rectangle(x1, y1, x2 - x1, y2 - y1));
            }
        }
        return rects;
    }

    public byte[] removePartsOfImage(byte[] imageBytes) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
        Graphics2D g = image.createGraphics();
        g.setColor(Color.BLACK);

        for (Rectangle rect : cropRectangles) {
            g.fillRect(rect.x, rect.y, rect.width, rect.height);
        }

        g.dispose();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        return baos.toByteArray();
    }
}