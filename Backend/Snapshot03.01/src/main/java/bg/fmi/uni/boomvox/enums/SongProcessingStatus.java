package bg.fmi.uni.boomvox.enums;

public enum SongProcessingStatus {
    PROCESSING,  // at least one variant is still being processed
    ACTIVE,      // all variants are READY — song is streamable
    FAILED       // processing failed; no variants are available
}
