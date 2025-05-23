package technikal.task.fishmarket.controllers;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import technikal.task.fishmarket.models.Fish;
import technikal.task.fishmarket.models.FishDto;
import technikal.task.fishmarket.services.FishRepository;

@Controller
@RequestMapping("/fish")
public class FishController {
	
	@Autowired
	private FishRepository repo;

	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	@GetMapping({"", "/"})
	public String showFishList(Model model) {
		List<Fish> fishlist = repo.findAll(Sort.by(Sort.Direction.DESC, "id"));
		model.addAttribute("fishlist", fishlist);
		return "index";
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/create")
	public String showCreatePage(Model model) {
		FishDto fishDto = new FishDto();
		model.addAttribute("fishDto", fishDto);
		return "createFish";
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/delete")
	public String deleteFish(@RequestParam int id) {
		try {
			Optional<Fish> optionalFish = repo.findById(id);
			if (optionalFish.isPresent()) {
				Fish fish = optionalFish.get();

				List<String> imageFileNames = fish.getImageFileNames();
				if (imageFileNames != null) {
					for (String fileName : imageFileNames) {
						Path imagePath = Paths.get("public/images/" + fileName);
						Files.deleteIfExists(imagePath);
					}
				}

				repo.delete(fish);
			}
		} catch (Exception ex) {
			System.out.println("Exception: " + ex.getMessage());
		}
		return "redirect:/fish";
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/create")
	public String addFish(@Valid @ModelAttribute FishDto fishDto, BindingResult result) {

		List<MultipartFile> images = fishDto.getImageFiles();
		if (images == null || images.stream().allMatch(MultipartFile::isEmpty)) {
			result.addError(new FieldError("fishDto", "imageFiles", "Потрібне фото рибки"));
		}

		if(result.hasErrors()) {
			return "createFish";
		}

		List<String> storedFilenames = new ArrayList<>();
		Date catchDate = new Date();

		
		try {
			String uploadDir = "public/images/";
			Path uploadPath = Paths.get(uploadDir);
			
			if(!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
			}

            for (MultipartFile image : images) {
				if (!image.isEmpty()) {
					String storageFileName = catchDate.getTime() + "_" + image.getOriginalFilename();
					try (InputStream inputStream = image.getInputStream()) {
						Files.copy(inputStream, uploadPath.resolve(storageFileName), StandardCopyOption.REPLACE_EXISTING);
					}
					storedFilenames.add(storageFileName);
				}
			}
			
		}catch(Exception ex) {
			System.out.println("Exception: " + ex.getMessage());
		}

		Fish fish = new Fish();
		fish.setCatchDate(catchDate);
		fish.setName(fishDto.getName());
		fish.setPrice(fishDto.getPrice());
		fish.setImageFileNames(storedFilenames);
		
		repo.save(fish);
		
		return "redirect:/fish";
	}

}
