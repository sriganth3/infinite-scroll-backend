package com.infinite.scroll.service;

import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.infinite.scroll.model.GridFsImage;
import com.infinite.scroll.repository.ImageRepository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ImageService {

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${unsplash.api.url}")
    private String unsplashApiUrl;

    @Value("${unsplash.api.key}")
    private String unsplashApiKey;

    /**
     * Fetch random images from Unsplash and upload them to GridFS.
     *
     * @param imagesCount The number of images to fetch from Unsplash.
     * @return A list of ObjectIds representing the uploaded images.
     * @throws IOException If an error occurs during image download.
     */
    public List<ObjectId> fetchAndUploadFromUnsplash(int imagesCount) throws IOException {
        // Build the API URL with the given count
        String apiUrl = String.format("%s/photos/random/?client_id=%s&count=%d", unsplashApiUrl, unsplashApiKey, imagesCount);

        // Make the API request to Unsplash
        List<Map<String, Object>> response = restTemplate.getForObject(apiUrl, List.class);

        if (response == null) {
            throw new IOException("Failed to fetch images from Unsplash.");
        }

        // Iterate over the response and upload each image to GridFS
        List<ObjectId> uploadedImageIds = new ArrayList<>();
        for (Map<String, Object> imageData : response) {
            // Extract the image details
            String imageId = (String) imageData.get("id");
            String altDescription = (String) imageData.get("alt_description");
            String description = (String) imageData.get("description");
            Map<String, String> urls = (Map<String, String>) imageData.get("urls");
            String imageUrl = urls.get("regular");

            // Check if the image already exists in the repository by ID
            if (imageRepository.existsByImageId(imageId)) {
                System.out.println("Image with ID " + imageId + " already exists. Skipping download.");
                continue;
            }

            // Download the image bytes
            byte[] imageBytes = IOUtils.toByteArray(new URL(imageUrl).openStream());

            // Store the image in GridFS and get the ObjectId
            ObjectId gridFSId = imageRepository.storeImage(new ByteArrayInputStream(imageBytes), UUID.randomUUID().toString() + ".jpg", "image/jpeg");
            ObjectId imageIdInDb = imageRepository.storeImage(imageId, altDescription, description, gridFSId);

            // Add the uploaded image's ObjectId to the list
            uploadedImageIds.add(imageIdInDb);
        }

        return uploadedImageIds;
    }
    
    /**
     * Fetch random images from GridFS.
     *
     * @param count The number of random images to fetch.
     * @return A list of random GridFsImage objects.
     */
    public List<GridFsImage> fetchRandomImagesFromGridFS(int count) {
        return imageRepository.fetchRandomImagesFromGridFS(count);
    }

    /**
     * Fetch a single image by its ID from GridFS.
     *
     * @param id The ObjectId of the image.
     * @return The GridFsImage object containing metadata and binary data.
     */
    public GridFsImage fetchImageById(String id) {
        return imageRepository.fetchImageById(id);
    }

}
