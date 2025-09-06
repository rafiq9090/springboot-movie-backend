package org.example.movices.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.movices.dto.request.MovieRequest;
import org.example.movices.dto.response.MovieResponse;
import org.example.movices.exception.DuplicateResourceException;
import org.example.movices.exception.ResourceNotFoundException;
import org.example.movices.exception.UnauthorizedException;
import org.example.movices.model.entity.Movie;
import org.example.movices.repository.MovieRepository;
import org.example.movices.service.MovieService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final ModelMapper modelMapper;

    @Value("${video.storage.path}")
    private String videoStoragePath;

    @Override
    public MovieResponse createMovie(MovieRequest movieRequest) {
        try {
            Movie movie = modelMapper.map(movieRequest, Movie.class);
            movie = movieRepository.save(movie);
            return modelMapper.map(movie, MovieResponse.class);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException("Movie with title '" + movieRequest.getTitle() + "' already exists!");
        }
    }

    @Override
    public MovieResponse createMovieWithFile(MovieRequest movieRequest, MultipartFile file) {
        try {
            // Ensure upload file exits
            Path uploadDir = Paths.get("upload/movies/");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            // Save the locally
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadDir.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Map Request + Entity
            Movie movie = modelMapper.map(movieRequest, Movie.class);
            movie.setVideoUrl(filePath.toString()); // save file path DB

            Movie saved = movieRepository.save(movie);
            return modelMapper.map(saved, MovieResponse.class);

        }catch (Exception ioException){
            throw new RuntimeException("Movie upload failed",ioException);
        }
    }


    @Override
    public MovieResponse getMovieById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
        return modelMapper.map(movie, MovieResponse.class);
    }

    @Override
    public List<MovieResponse> getAllMovies() {
        List<Movie> movies = movieRepository.findAll();
        return movies.stream()
                .map(movie -> modelMapper.map(movie, MovieResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public MovieResponse updateMovie(Long id, MovieRequest movieRequest) {
        Movie existingMovie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));

        modelMapper.map(movieRequest, existingMovie);

        existingMovie = movieRepository.save(existingMovie);
        return modelMapper.map(existingMovie, MovieResponse.class);
    }

    @Override
    public void deleteMovie(Long id) {
        if (!movieRepository.existsById(id)) {
            throw new ResourceNotFoundException("Movie not found with id: " + id);
        }
        movieRepository.deleteById(id);
    }

    @Override
    public Resource downloadMovie(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));

        String videoFileName = movie.getVideoUrl();

        try {
            Path videoPath = Paths.get(videoStoragePath).resolve(videoFileName).normalize();
            Resource resource = new UrlResource(videoPath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read the video file!");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }
}
