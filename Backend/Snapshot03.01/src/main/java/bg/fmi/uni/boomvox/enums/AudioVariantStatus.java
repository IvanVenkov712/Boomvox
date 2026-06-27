package bg.fmi.uni.boomvox.enums;

public enum AudioVariantStatus {
    PROCESSING,  // FFmpeg job is running
    READY,       // uploaded to S3 and available for streaming
    FAILED       // FFmpeg or S3 upload failed for this variant
}
