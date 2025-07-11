package rs.ac.uns.ftn.informatika.rest.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

//kompresija:
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.imageio.IIOImage;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

import rs.ac.uns.ftn.informatika.rest.domain.Post;
import rs.ac.uns.ftn.informatika.rest.repository.PostRepository;

@Service
public class ImageCompressionService {
    private static final String IMAGE_DIRECTORY_PATH_STRING = "src/main/resources/static/images/posts";
    private final Path imageDirectoryPath = Paths.get(IMAGE_DIRECTORY_PATH_STRING);

    // private static final long ONE_MINUTE_IN_MILLIS = TimeUnit.MINUTES.toMillis(1); // za testiranje
    private static final long ONE_MONTH_IN_MILLIS = TimeUnit.DAYS.toMillis(30);

    @Autowired
    private PostRepository postRepository;

    //@Scheduled(cron = "*/10 * * * * *") // za testiranje svakih 10 sekundi
    @Scheduled(cron = "0 0 0 * * ?") // svaki dan u ponoc
    public void compressOldImages() {
        System.out.println("Image compression task triggered at: " + LocalDate.now());
        if (!Files.exists(imageDirectoryPath) || !Files.isDirectory(imageDirectoryPath)) {
            System.err.println("Error: The image directory does not exist or is invalid: " + IMAGE_DIRECTORY_PATH_STRING);
            return;
        }

        File folder = imageDirectoryPath.toFile();
        File[] files = folder.listFiles();

        if (files != null) {
            int compressedCount = 0;
            for (File file : files) {
                if (file.isFile() && !file.getName().startsWith("compressed_") &&
                        (file.getName().toLowerCase().endsWith(".jpg") || file.getName().toLowerCase().endsWith(".jpeg")) &&
                        isOlderThanOneMonth(file)) {
                    try {
                        System.out.println("  - Old uncompressed image found: " + file.getName());
                        compressAndRenameImage(file);
                        compressedCount++;
                    } catch (IOException e) {
                        System.err.println("  - Error compressing the image " + file.getName() + ": " + e.getMessage());
                    }
                }
            }
            System.out.println("Total compressed:: " + compressedCount + " images.");
        } else {
            System.out.println("  - There are no images in the directory to check.");
        }
    }

    private boolean isOlderThanOneMonth(File file) {
        long currentTimeMillis = System.currentTimeMillis();
        long fileLastModifiedMillis = file.lastModified();
        return (currentTimeMillis - fileLastModifiedMillis) >= ONE_MONTH_IN_MILLIS;
    }

    private void compressAndRenameImage(File originalFile) throws IOException {
        String originalFileName = originalFile.getName();
        String compressedFileName = "compressed_" + originalFileName;
        File tempCompressedFile = new File(originalFile.getParent(), compressedFileName + ".tmp");

        BufferedImage originalImage = ImageIO.read(originalFile);
        if (originalImage == null) {
            System.err.println("    - Unable to load image for compression: " + originalFileName);
            return;
        }

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) {
            System.err.println("    - No ImageWriter available for JPEG format.");
            return;
        }

        ImageWriter writer = writers.next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(0.6f);

        try (FileImageOutputStream output = new FileImageOutputStream(tempCompressedFile)) {
            writer.setOutput(output);
            writer.write(null, new IIOImage(originalImage, null, null), param);
        } finally {
            writer.dispose();
        }

        if (!tempCompressedFile.exists() || tempCompressedFile.length() == 0) {
            System.err.println("    - Failed to create compressed temporary file: " + tempCompressedFile.getName());
            return;
        }

        if (!originalFile.delete()) {
            System.err.println("    - Failed to delete original file: " + originalFileName);
            tempCompressedFile.delete();
            return;
        }

        File finalCompressedFile = new File(originalFile.getParent(), compressedFileName);
        if (!tempCompressedFile.renameTo(finalCompressedFile)) {
            System.err.println("    - Failed to rename temporary compressed file to final name: " + finalCompressedFile.getName());
            tempCompressedFile.delete();
            return;
        }

        System.out.println("    - Successfully compressed and renamed image: " + originalFileName + " -> " + finalCompressedFile.getName());

        Post postToUpdate = postRepository.findByImageUrl("images/posts/" + originalFileName);
        if (postToUpdate != null) {
             postToUpdate.setImageUrl("images/posts/" + compressedFileName);
             postRepository.save(postToUpdate);
             System.out.println("    - Updated database imageUrl for post: " + originalFileName + " to " + compressedFileName);
         } else {
             System.err.println("    - WARNING: Could not find post in database for image: " + originalFileName);
         }
    }
}