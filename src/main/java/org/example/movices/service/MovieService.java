package org.example.movices.service;
import org.example.movices.dto.request.MovieRequest;
import org.example.movices.dto.response.MovieResponse;

import org.springframework.core.io.Resource;

import java.util.List;

public interface MovieService {
    MovieResponse createMovie(MovieRequest movieRequest);
    MovieResponse getMovieById(Long id);
    List<MovieResponse> getAllMovies();
    MovieResponse updateMovie(Long id, MovieRequest movieRequest);
    void deleteMovie(Long id);
    Resource downloadMovie(Long id);
}

