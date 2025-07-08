package technikal.task.fishmarket.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

    @Entity
    @Table(name = "fish_images", uniqueConstraints = @UniqueConstraint(columnNames = "fileName"))
    public class Image {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false, unique = true)
        private String fileName;

        @ManyToOne
        @JoinColumn(name = "fish_id", nullable = false)
        private Fish fish;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }

        public Fish getFish() { return fish; }
        public void setFish(Fish fish) { this.fish = fish; }
    }
