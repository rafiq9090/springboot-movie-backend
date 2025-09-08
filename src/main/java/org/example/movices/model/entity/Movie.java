package org.example.movices.model.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "movies")
@Data
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 200, nullable = false, unique = true)
    private String title;

    @Column(length = 500)
    private String description;
    private String director;
    private String releaseDate;
    private String rating;
    private String genre;

    private String thumbnailImage;

    private String video;
}
