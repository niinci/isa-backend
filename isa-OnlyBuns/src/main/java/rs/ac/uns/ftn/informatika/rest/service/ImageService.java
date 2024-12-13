package rs.ac.uns.ftn.informatika.rest.service;

import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

@Service
public class ImageService {

    private static final String IMAGE_DIRECTORY = "src/main/resources/static/images/posts";

    public String saveImage(String base64Image) throws IOException {
        if (base64Image != null && !base64Image.isEmpty()) {
            try {
                byte[] imageData = Base64.getDecoder().decode(base64Image.split(",")[1]);

                String fileName = UUID.randomUUID().toString() + ".jpg";
                Path imagePath = Paths.get(IMAGE_DIRECTORY);
                Files.createDirectories(imagePath);

                Path filePath = imagePath.resolve(fileName);
                Files.write(filePath, imageData);

                return "images/posts/" + fileName;
            } catch (IOException e) {
                throw new IOException("Error saving image", e);
            }
        }
        return null;
    }
}
