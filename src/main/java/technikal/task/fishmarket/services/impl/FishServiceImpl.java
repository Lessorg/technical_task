package technikal.task.fishmarket.services.impl;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.multipart.MultipartFile;
import technikal.task.fishmarket.model.Fish;
import technikal.task.fishmarket.dto.FishDto;
import technikal.task.fishmarket.model.Image;
import technikal.task.fishmarket.repository.FishRepository;
import technikal.task.fishmarket.services.FishService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import technikal.task.fishmarket.services.ImageService;

import java.util.Date;
import java.util.List;

@Service
public class FishServiceImpl implements FishService {
    private static final Logger logger = LoggerFactory.getLogger(FishServiceImpl.class);
    private final FishRepository fishRepository;
    private final ImageService imageService;

    @Autowired
    public FishServiceImpl(FishRepository fishRepository, ImageService imageService) {
        this.fishRepository = fishRepository;
        this.imageService = imageService;
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
        if (!isFishDtoImagesValid(fishDto.getImageFiles(), result)) {
            logger.error("Fish saving failed due to image validation");
            return null;
        }

        Date catchDate = new Date();
        List<Image> images = imageService.storeImages(fishDto.getImageFiles(), catchDate);

        Fish fish = new Fish();
        fish.setCatchDate(catchDate);
        fish.setName(fishDto.getName());
        fish.setPrice(fishDto.getPrice());
        fish.setImages(images);

        for (Image image : images) {
            image.setFish(fish);
        }

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

    private Fish findFishById(Integer id) {
        logger.info("Attempting to find fish with ID: {}", id);

        return fishRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Fish not found with id: " + id));
    }
}
