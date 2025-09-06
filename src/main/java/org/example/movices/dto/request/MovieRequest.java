package org.example.movices.dto.request;

import lombok.Data;

@Data
public class MovieRequest {
    private String title;
    private String description;
    private String director;
    private String releaseDate;
    private String rating;
    private String genre;
    private String thumbnailImage;
}