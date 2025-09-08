package org.example.movices.repository;

import org.example.movices.model.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    boolean existsByTitle(String title);
    boolean existsByTitleIgnoreCase(String title);
    Movie findByTitle(String title);
}
