package technikal.task.fishmarket.services.impl;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.multipart.MultipartFile;
import technikal.task.fishmarket.exception.FailedToSaveImageException;
import technikal.task.fishmarket.model.Fish;
import technikal.task.fishmarket.dto.FishDto;
import technikal.task.fishmarket.repository.FishRepository;
import technikal.task.fishmarket.services.FishService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class FishServiceImpl implements FishService {
    private static final Logger logger = LoggerFactory.getLogger(FishServiceImpl.class);
    private final FishRepository fishRepository;

    @Autowired
    public FishServiceImpl(FishRepository fishRepository) {
        this.fishRepository = fishRepository;
    }

    public List<Fish> getFishListSortedById() {
        logger.info("Fetching fish list sorted by ID (DESC)");
        return fishRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    @Override
    public FishDto getFishDto() {
        logger.debug("Returning new FishDto");
        return new FishDto();
    }

    public void deleteFishById(int id) {
        Fish fish = findFishById(id);

        logger.info("Deleting fish with ID {}", id);
        fishRepository.delete(fish);
    }

    public Fish saveFish(FishDto fishDto, BindingResult result) {
        List<String> storedFilenames = new ArrayList<>();
        Date catchDate = new Date();

        if (!isFishDtoImagesValid(fishDto.getImageFiles(), result)
                || !saveImages(fishDto, catchDate, storedFilenames)) {
            logger.error("Fish saving failed");
            return null;
        }

        Fish fish = new Fish();
        fish.setCatchDate(catchDate);
        fish.setName(fishDto.getName());
        fish.setPrice(fishDto.getPrice());
        fish.setImageFileNames(storedFilenames);

        Fish savedFish = fishRepository.save(fish);
        logger.info("Fish saved successfully with ID {}", savedFish.getId());
        return savedFish;
    }

    private boolean isFishDtoImagesValid (List<MultipartFile> images, BindingResult result) {
        logger.debug("Validating image files");
        if (images == null || images.stream().allMatch(MultipartFile::isEmpty)) {
            logger.warn("No images provided for fish");
            result.addError(new FieldError("fishDto", "imageFiles", "Потрібне фото рибки"));
            return false;
        }

        return true;
    }

    private boolean saveImages(FishDto fishDto, Date catchDate, List<String>  storedFilenames) {
        try {
            String uploadDir = "public/images/";
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                logger.debug("Created upload directory: {}", uploadPath.toAbsolutePath());
            }

            for (MultipartFile image : fishDto.getImageFiles()) {
                if (!image.isEmpty()) {
                    String storageFileName = catchDate.getTime() + "_" + image.getOriginalFilename();
                    try (InputStream inputStream = image.getInputStream()) {
                        Files.copy(inputStream, uploadPath.resolve(storageFileName), StandardCopyOption.REPLACE_EXISTING);
                        storedFilenames.add(storageFileName);
                        logger.info("Saved image file: {}", storageFileName);
                    }
                }
            }
            return true;
        } catch (IOException e) {
            throw new FailedToSaveImageException(e.getMessage());
        }
    }

    private Fish findFishById(Integer id) {
        logger.info("Attempting to find fish with ID: {}", id);

        return fishRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Fish not found with id: " + id));
    }
}
