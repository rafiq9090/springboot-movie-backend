package org.example.movices.service;
import org.example.movices.dto.request.MovieRequest;
import org.example.movices.dto.response.MovieResponse;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MovieService {
    MovieResponse createMovie(MovieRequest movieRequest);
    MovieResponse createMovieWithFile(MovieRequest movieRequest, MultipartFile file);
    MovieResponse getMovieById(Long id);
    List<MovieResponse> getAllMovies();
    MovieResponse updateMovie(Long id, MovieRequest movieRequest);
    void deleteMovie(Long id);
    Resource downloadMovie(Long id);
}

