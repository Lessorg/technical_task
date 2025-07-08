package technikal.task.fishmarket.services;

import org.springframework.web.multipart.MultipartFile;
import technikal.task.fishmarket.model.Image;

import java.util.Date;
import java.util.List;

public interface ImageService {
    List<Image> storeImages(List<MultipartFile> multipartFiles, Date catchDate);
}
