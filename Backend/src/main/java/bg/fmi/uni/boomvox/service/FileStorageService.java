package bg.fmi.uni.boomvox.service;

import bg.fmi.uni.boomvox.exception.StorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;

@Service
@Slf4j
public class FileStorageService {

    private final S3Client  s3Client;

    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.signedUrlExpiryMinutes}")
    private int signedUrlExpiryMinutes;

    public FileStorageService(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    // --------------------------------------------
    // UPLOAD
    // --------------------------------------------

    /**
     * Upload any file to S3. Returns the S3 key (store this in the database).
     *
     * @param key         S3 object key, e.g. "variants/song_42_128k.mp3"
     * @param inputStream file content
     * @param contentLength byte length of the file (required by SDK)
     * @param contentType  MIME type, e.g. "audio/mpeg"
     * @return the S3 key (same as input key)
     */
    public String uploadFile(String key, InputStream inputStream,
                             long contentLength, String contentType) {
        try {
            log.info("Uploading to S3: bucket={} key={}", bucket, key);
            s3Client.putObject(
                PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .contentLength(contentLength)
                    .build(),
                RequestBody.fromInputStream(inputStream, contentLength)
            );
            log.info("Upload complete: {}", key);
            return key;
        } catch (S3Exception e) {
            log.error("S3 upload failed for key {}: {}", key, e.awsErrorDetails().errorMessage());
            throw new StorageException("Failed to upload file to S3", e);
        }
    }

    /**
     * Convenience overload for MultipartFile uploads (from Controller layer).
     */
    public String uploadMultipartFile(String key, MultipartFile file) {
        try {
            return uploadFile(key, file.getInputStream(),
                file.getSize(), file.getContentType());
        } catch (IOException e) {
            throw new StorageException("Failed to read uploaded file", e);
        }
    }

    // --------------------------------------------
    // SIGNED URL GENERATION (for streaming)
    // --------------------------------------------

    /**
     * Generate a pre-signed GET URL for an S3 object.
     * The URL expires in `signedUrlExpiryMinutes` minutes (default: 15).
     *
     * @param s3Key the streaming_key stored in audio_variants
     * @return a temporary HTTPS URL the browser can use to stream the file
     */
    public String generateSignedUrl(String s3Key) {
        try {
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(signedUrlExpiryMinutes))
                .getObjectRequest(req -> req
                    .bucket(bucket)
                    .key(s3Key)
                    .responseContentType("audio/mpeg") // hint to browser
                )
                .build();

            return s3Presigner.presignGetObject(presignRequest)
                .url()
                .toString();
        } catch (S3Exception e) {
            log.error("Failed to generate signed URL for key {}: {}", s3Key, e.awsErrorDetails().errorMessage());
            throw new StorageException("Failed to generate streaming URL", e);
        }
    }

    // --------------------------------------------
    // DELETE
    // --------------------------------------------

    /**
     * Delete a single file from S3 (used after FFmpeg processing to remove raw uploads).
     */
    public void deleteFile(String key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
            log.info("Deleted S3 object: {}", key);
        } catch (S3Exception e) {
            // Log but don't throw — a failed delete of a raw file is not critical
            log.warn("Failed to delete S3 object {}: {}", key, e.awsErrorDetails().errorMessage());
        }
    }

    // --------------------------------------------
    // KEY BUILDERS (keep naming consistent)
    // --------------------------------------------

    public static String songKey(String uuid, String extension) {
        return "songs/song_" + uuid + "." + extension.toLowerCase();
    }

    public static String rawUploadKey(String uuid, String originalFilename) {
        return "raw/uploads/song_" + uuid + "_" + sanitize(originalFilename);
    }

    public static String variantKey(long songId, int bitrateKbps, String format) {
        return "variants/song_" + songId + "_" + bitrateKbps + "k." + format.toLowerCase();
    }

    public static String coverKey(long songId, String ext) {
        return "covers/song_" + songId + "_cover." + ext.toLowerCase();
    }

    public static String avatarKey(long userId, String ext) {
        return "avatars/user_" + userId + "_avatar." + ext.toLowerCase();
    }

    private static String sanitize(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_").toLowerCase();
    }

    /**
     * Deletes all three standard quality variants from S3 for a given song.
     * Called when a song is permanently deleted.
     */
    public void deleteSongVariants(long songId) {
        List<String> keys = List.of(
            variantKey(songId, 128, "mp3"),
            variantKey(songId, 320, "mp3"),
            variantKey(songId, 256, "aac")
        );
        keys.forEach(this::deleteFile);
    }
}
