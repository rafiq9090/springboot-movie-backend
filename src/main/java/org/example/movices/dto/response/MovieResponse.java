package org.example.movices.dto.response;

import lombok.Data;

@Data
public class MovieResponse {
    private Long id;
    private String title;
    private String description;
    private String director;
    private String releaseDate;
    private String rating;
    private String genre;
    private String thumbnailImage;
    private String videoUrl;
}