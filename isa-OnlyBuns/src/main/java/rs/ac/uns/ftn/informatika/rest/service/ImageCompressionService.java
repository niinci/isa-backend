package rs.ac.uns.ftn.informatika.rest.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class ImageCompressionService {

    private static final String IMAGE_DIRECTORY = "uploads/images";

    @Scheduled(cron = "0 0 0 * * ?")
    public void compressOldImages() {
        File folder = new File(IMAGE_DIRECTORY);
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && isOlderThanOneMonth(file)) {
                    try {
                        compressImage(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private boolean isOlderThanOneMonth(File file) {
        LocalDate lastModifiedDate = LocalDate.ofEpochDay(file.lastModified() / 86400000);
        return ChronoUnit.MONTHS.between(lastModifiedDate, LocalDate.now()) >= 1;
    }

    private void compressImage(File file) throws IOException {
        BufferedImage originalImage = ImageIO.read(file);

        BufferedImage compressedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = compressedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, null);
        g.dispose();

        File compressedFile = new File(file.getParent(), "compressed_" + file.getName());
        ImageIO.write(compressedImage, "jpg", compressedFile);
    }
}
