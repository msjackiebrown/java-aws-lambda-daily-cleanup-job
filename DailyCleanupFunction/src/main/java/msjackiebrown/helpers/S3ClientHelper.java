package msjackiebrown.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.List;

public class S3ClientHelper {

    private static final Logger logger = LoggerFactory.getLogger(S3ClientHelper.class);
    private final S3Client s3Client;

    public S3ClientHelper(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public List<S3Object> listObjects(String bucketName, String prefix) {
        ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                .bucket(bucketName);
                
        if (prefix != null && !prefix.isEmpty()) {
            requestBuilder.prefix(prefix);
        }

        ListObjectsV2Response listObjectsResponse = s3Client.listObjectsV2(requestBuilder.build());
        return listObjectsResponse.contents();
    }

    // Overloaded method for backward compatibility
    public List<S3Object> listObjects(String bucketName) {
        return listObjects(bucketName, null);
    }

    public void deleteObject(String bucketName, String key) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
            logger.info("Deleted object: {}", key);
        } catch (Exception e) {
            logger.error("Failed to delete object {}: {}", key, e.getMessage(), e);
        }
    }
}