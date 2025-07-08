package technikal.task.fishmarket.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;
import technikal.task.fishmarket.models.Fish;
import technikal.task.fishmarket.models.FishDto;
import technikal.task.fishmarket.services.FishService;

@Controller
@RequestMapping("/fish")
public class FishController {
	private final FishService fishService;

	@Autowired
    public FishController(FishService fishService) {
        this.fishService = fishService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	@GetMapping({"", "/"})
	public String showFishList(Model model) {
		List<Fish> fishlist = fishService.getFishListSortedById();
		model.addAttribute("fishlist", fishlist);
		return "index";
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/create")
	public String showCreatePage(Model model) {
		FishDto fishDto = fishService.getFishDto();
		model.addAttribute("fishDto", fishDto);
		return "createFish";
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/delete")
	public String deleteFish(@RequestParam int id) {
		fishService.deleteFishById(id);
		return "redirect:/fish";
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/create")
	public String addFish(@Valid @ModelAttribute FishDto fishDto, BindingResult result) {
		fishService.saveFish(fishDto, result);
		if (result.hasErrors()) {
			return "createFish";
		}

		return "redirect:/fish";
	}

}
