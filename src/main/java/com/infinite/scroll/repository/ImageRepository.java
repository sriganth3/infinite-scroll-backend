
package com.infinite.scroll.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSUploadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.model.Aggregates;
import com.infinite.scroll.model.GridFsImage;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ImageRepository {

	@Autowired
    private MongoDatabase mongoDatabase;


    /**
     * Store an image in GridFS.
     *
     * @param inputStream The image data.
     * @param fileName    The name of the file.
     * @param contentType The MIME type of the file.
     * @return The ObjectId of the stored file.
     */
    public ObjectId storeImage(InputStream inputStream, String fileName, String contentType) {
        // Get the GridFS bucket
        var gridFSBucket = GridFSBuckets.create(mongoDatabase, "images");  // "images" is the bucket name

        // Create an upload stream for the file
        try (GridFSUploadStream uploadStream = gridFSBucket.openUploadStream(fileName)) {
            // Write the image data to GridFS
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                uploadStream.write(buffer, 0, bytesRead);
            }

            // Return the ObjectId of the uploaded file
            return uploadStream.getObjectId();
        } catch (Exception e) {
            throw new RuntimeException("Error storing image in GridFS", e);
        }
    }
    
    /**
     * Store the image metadata in the database.
     *
     * @param image The image metadata to store.
     * @return The inserted ObjectId of the image.
     */
    public ObjectId storeImage(String imageId, String altDescription, String description, ObjectId gridFsId) {
        Document imageDocument = new Document()
                .append("imageId", imageId)
                .append("altDescription", altDescription)
                .append("description", description)
                .append("gridFsId", gridFsId);
        mongoDatabase.getCollection("images").insertOne(imageDocument);
        return gridFsId;
    }
    
    /**
     * Check if an image with the given imageId already exists in the database.
     *
     * @param imageId The image ID to check.
     * @return true if the image exists, false otherwise.
     */
    public boolean existsByImageId(String imageId) {
        Document query = new Document("imageId", imageId);
        return mongoDatabase.getCollection("images").find(query).first() != null;
    }
    

    /**
     * Fetch random images from GridFS using $sample.
     *
     * @param count The number of random images to fetch.
     * @return A list of GridFsImage objects.
     */
    public List<GridFsImage> fetchRandomImagesFromGridFS(int count) {
        GridFSBucket gridFSBucket = GridFSBuckets.create(mongoDatabase, "images");
        MongoCollection<Document> filesCollection = mongoDatabase.getCollection("images.files");

        // Build the $sample aggregation pipeline
        List<Bson> pipeline = List.of(
            Aggregates.sample(count)
        );

        List<GridFsImage> randomImages = new ArrayList<>();
        for (Document fileDoc : filesCollection.aggregate(pipeline)) {
            ObjectId id = fileDoc.getObjectId("_id");
            GridFSFile gridFSFile = gridFSBucket.find(new Document("_id", id)).first();

            if (gridFSFile != null) {
                randomImages.add(mapGridFsFileToImage(gridFSFile, gridFSBucket));
            }
        }

        return randomImages;
    }

    /**
     * Fetch a single image by its ID from GridFS.
     *
     * @param id The ObjectId of the image.
     * @return The GridFsImage object containing metadata and binary data.
     */
    public GridFsImage fetchImageById(String id) {
        GridFSBucket gridFSBucket = GridFSBuckets.create(mongoDatabase, "images");
        GridFSFile gridFSFile = gridFSBucket.find(new Document("_id", new ObjectId(id))).first();

        if (gridFSFile == null) {
            throw new IllegalArgumentException("Image not found with ID: " + id);
        }

        return mapGridFsFileToImage(gridFSFile, gridFSBucket);
    }

    /**
     * Map a GridFSFile and its binary content to a GridFsImage object.
     *
     * @param gridFSFile The GridFSFile metadata.
     * @param gridFSBucket The GridFSBucket for binary retrieval.
     * @return The mapped GridFsImage object.
     */
    private GridFsImage mapGridFsFileToImage(GridFSFile gridFSFile, GridFSBucket gridFSBucket) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        gridFSBucket.downloadToStream(gridFSFile.getObjectId(), outputStream);

        return new GridFsImage(
            gridFSFile.getObjectId().toString(),
//            gridFSFile.getMetadata().getString("altDescription"),
//            gridFSFile.getMetadata().getString("description"),
            gridFSFile.getFilename(),
//            gridFSFile.getMetadata().getString("_contentType"),
            outputStream.toByteArray()
        );
    }
}
