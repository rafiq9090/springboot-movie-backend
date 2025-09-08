package org.example.movices.service.impl;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.InvalidFileNameException;
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
import java.nio.file.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final ModelMapper modelMapper;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;


    @Override
    public MovieResponse createMovieWithFile(MovieRequest movieRequest, MultipartFile video, MultipartFile photo) {
        try {

            if(movieRequest == null){
                throw new ResourceNotFoundException("Movie request cannot be null");
            }
            if(movieRequest.getTitle() == null ||  movieRequest.getTitle().trim().isEmpty()){
                throw new ResourceNotFoundException("Movie title required");
            }
            if (movieRepository.existsByTitle(movieRequest.getTitle())) {
                throw new DuplicateResourceException("Movie with title '" + movieRequest.getTitle() + "' already exists");            }

            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Convert Request to Entity
            Movie movie = modelMapper.map(movieRequest, Movie.class);

            // Handle thumbnail image upload (optional)
            if (photo != null && !photo.isEmpty()) {
               validImageFile(photo);
                String thumbnailFileName = generateFileName(photo.getOriginalFilename());
                Path thumbnailPath = uploadPath.resolve(thumbnailFileName);
                Files.copy(photo.getInputStream(), thumbnailPath);
                movie.setThumbnailImage(thumbnailFileName);
            }

            // Handle video file upload (required)
            if (video != null && !video.isEmpty()) {
                validVideoFile(video);
                String videoFileName = generateFileName(video.getOriginalFilename());
                Path videoPath = uploadPath.resolve(videoFileName);
                Files.copy(video.getInputStream(), videoPath);
                movie.setVideo(videoFileName);
            } else {
                throw new RuntimeException("Video file is required");
            }

            // Save to database
            Movie savedMovie = movieRepository.save(movie);

            // Convert Entity to Response and return
            return modelMapper.map(savedMovie, MovieResponse.class);

        } catch (IOException e) {
            throw new ResourceNotFoundException("Failed to store files: " + e.getMessage(), e);
        }catch (DataIntegrityViolationException e) {
            throw e;
        }catch (Exception e) {
            throw new RuntimeException("Failed to store files: " + e.getMessage(), e);
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
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
        if (!movieRepository.existsById(id)) {
            throw new ResourceNotFoundException("Movie not found with id: " + id);
        }
        try {
            if (movie.getThumbnailImage() != null && !movie.getThumbnailImage().isEmpty()) {
                Files.deleteIfExists(Paths.get(uploadDir,movie.getThumbnailImage()));
            }
            if (movie.getVideo() != null && !movie.getVideo().isEmpty()) {
                Files.deleteIfExists(Paths.get(uploadDir,movie.getVideo()));
            }
        }catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + e.getMessage(), e);
        }

        movieRepository.deleteById(id);
    }

    @Override
    public Resource downloadMovie(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
        if (movie.getVideo() != null && !movie.getVideo().isEmpty()) {
            throw new ResourceNotFoundException("No video file associated with this movie");
        }

        try {
            Path filePath = Paths.get(uploadDir).resolve(movie.getVideo()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                throw new ResourceNotFoundException("Video file not found on server");
            }
            if (!resource.isReadable()) {
                throw new RuntimeException("Video file is not readable (permission issue)");
            }

            return resource;

        } catch (MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage(), e);
        }
    }

    private void  validImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResourceNotFoundException("Empty image file");
        }
        String contentType = file.getContentType();
        if (contentType == null || !isValidImageType(contentType)) {
            throw new ResourceNotFoundException("Invalid image file type");
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new ResourceNotFoundException("Image file size must be less than 5MB");
        }
    }

    private boolean isValidImageType(String contentType) {
        return contentType.equals("image/jpeg") ||
                contentType.equals("image/jpg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/gif") ||
                contentType.equals("image/webp");
    }

    private void validVideoFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResourceNotFoundException("Empty video file");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("video/")) {
            throw new ResourceNotFoundException("Invalid video file type");
        }
        if (file.getSize() > 2000 * 1024 * 1024) {
            throw new ResourceNotFoundException("Video file size must be less than 2000MB");
        }
    }

   private String generateFileName(String originalFilename) {
        String uuid = UUID.randomUUID().toString();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")){
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return uuid + extension;
   }
}
