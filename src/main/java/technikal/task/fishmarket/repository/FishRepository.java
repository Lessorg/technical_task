package technikal.task.fishmarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import technikal.task.fishmarket.model.Fish;

public interface FishRepository extends JpaRepository<Fish, Integer> {

}
