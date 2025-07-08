package technikal.task.fishmarket.services;

import org.springframework.validation.BindingResult;
import technikal.task.fishmarket.model.Fish;
import technikal.task.fishmarket.dto.FishDto;

import java.util.List;

public interface FishService {
    List<Fish> getFishListSortedById();

    FishDto getFishDto();

    void deleteFishById(int id);

    Fish saveFish(FishDto fishDto, BindingResult result);
}
