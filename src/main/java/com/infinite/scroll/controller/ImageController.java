package com.infinite.scroll.controller;

import java.io.IOException;
import java.util.List;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.infinite.scroll.model.GridFsImage;
import com.infinite.scroll.service.ImageService;


@RestController
@RequestMapping("/images")
public class ImageController {

	@Autowired
	private ImageService imageService;
	
	private final Logger log = LoggerFactory.getLogger(ImageController.class);

	// Upload image from Unsplash
	@PostMapping("/upload")
	public ResponseEntity<String> uploadFromUnsplash(@RequestParam(defaultValue = "5") Integer count) {
		if(count == null || count == 0) {
			count = 5;
		}
		try {
			List<ObjectId> id = imageService.fetchAndUploadFromUnsplash(count);
			return ResponseEntity.ok("Image uploaded with ID: " + id.toString());
		} catch (IOException e) {
			return ResponseEntity.internalServerError().body("Failed to fetch and upload image from Unsplash");
		}
	}
    
    /**
     * Fetch random images stored in GridFS.
     *
     * @param count The number of random images to fetch (default: 5).
     * @return A list of random GridFsImage objects.
     */
	@CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/random")
    public ResponseEntity<?> fetchRandomImagesFromGridFS(@RequestParam(defaultValue = "5") int count) {
		long start = System.currentTimeMillis();
        try {
            List<GridFsImage> randomImages = imageService.fetchRandomImagesFromGridFS(count);
            return ResponseEntity.ok(randomImages);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching images: " + e.getMessage());
        }finally {
        	log.info("Time taken for random response:: {} ms",  System.currentTimeMillis() - start);
        }
    }

}
