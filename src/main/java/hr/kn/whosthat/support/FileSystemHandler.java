package hr.kn.whosthat.support;

import org.opencv.core.Mat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class FileSystemHandler {

    @Value("${app.workdir}")
    private String workDir;

    public String saveDetectedSnapToDisk(Mat mat) {
        try {
            int length = (int) (mat.total() * mat.elemSize());
            byte[] buffer = new byte[length];
            mat.get(0, 0, buffer);

            var path = workDir + "snap_detect_" + System.currentTimeMillis() + ".jpg";
            Files.write(Paths.get(workDir + "snap.jpg"), buffer);
            return path;
        } catch (IOException e) {
            throw new RuntimeException("Exception while writing camera snap to disk: " + e.getMessage());
        }

    }

    public String saveSnapToDisk(byte[] file) {
        try {
            var path = workDir + "snap.jpg";
            Files.write(Paths.get(workDir + "snap.jpg"), file);
            return path;
        } catch (IOException e) {
            throw new RuntimeException("Exception while writing camera snap to disk: " + e.getMessage());
        }
    }

}
