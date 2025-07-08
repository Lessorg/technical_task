package technikal.task.fishmarket.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import technikal.task.fishmarket.exception.FailedToSaveImageException;
import technikal.task.fishmarket.model.Image;
import technikal.task.fishmarket.services.ImageService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ImageServiceImpl implements ImageService {
    private static final Logger logger = LoggerFactory.getLogger(ImageServiceImpl.class);
    private static final String UPLOAD_DIR = "public/images/";

    @Override
    public List<Image> storeImages(List<MultipartFile> multipartFiles, Date catchDate) {
        List<Image> imageEntities = new ArrayList<>();
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                logger.debug("Created upload directory: {}", uploadPath.toAbsolutePath());
            }

            for (MultipartFile file : multipartFiles) {
                if (!file.isEmpty()) {
                    String fileName = catchDate.getTime() + "_" + file.getOriginalFilename();
                    try (InputStream inputStream = file.getInputStream()) {
                        Files.copy(inputStream, uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                        Image image = new Image();
                        image.setFileName(fileName);
                        imageEntities.add(image);
                        logger.info("Saved image: {}", fileName);
                    }
                }
            }

            return imageEntities;
        } catch (IOException e) {
            throw new FailedToSaveImageException("Failed to save image: " + e.getMessage());
        }
    }
}
