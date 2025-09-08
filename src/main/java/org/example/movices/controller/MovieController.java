package org.example.movices.controller;


import lombok.RequiredArgsConstructor;
import org.example.movices.dto.request.MovieRequest;
import org.example.movices.dto.response.MovieResponse;
import org.example.movices.exception.ResourceNotFoundException;
import org.example.movices.service.MovieService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MovieResponse> createMovie(
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "director", required = false) String director,
            @RequestParam(value = "releaseDate", required = false) String releaseDate,
            @RequestParam(value = "rating", required = false) String rating,
            @RequestParam(value = "genre", required = false) String genre,
            @RequestParam("video") MultipartFile video,
            @RequestParam(value = "photo", required = false) MultipartFile photo) {

        // Create MovieRequest manually
        MovieRequest movieRequest = new MovieRequest();
        movieRequest.setTitle(title);
        movieRequest.setDescription(description);
        movieRequest.setDirector(director);
        movieRequest.setReleaseDate(releaseDate);
        movieRequest.setRating(rating);
        movieRequest.setGenre(genre);

        MovieResponse movieResponse = movieService.createMovieWithFile(movieRequest, video, photo);
        return new ResponseEntity<>(movieResponse, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<MovieResponse> getMovieById(@PathVariable Long id) {
        MovieResponse movie = movieService.getMovieById(id);
        return ResponseEntity.ok(movie);
    }

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<MovieResponse>> getAllMovies() {
        List<MovieResponse> movies = movieService.getAllMovies();
        return ResponseEntity.ok(movies);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieResponse> updateMovie(@PathVariable Long id, @RequestBody MovieRequest movieRequest) {
        MovieResponse updatedMovie = movieService.updateMovie(id, movieRequest);
        return ResponseEntity.ok(updatedMovie);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Resource> downloadMovie(@PathVariable Long id) {
        Resource resource = movieService.downloadMovie(id);

        String contentType = "application/octet-stream";
        try {
            contentType = Files.probeContentType(Path.of(resource.getURI()));
            if (contentType == null) {
                contentType = "application/octet-stream";

            }
        } catch (IOException e) {
            // ignore
        }
        String fileName = resource.getFilename();
        if (fileName == null) {
            fileName = "Movie " + id;
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
