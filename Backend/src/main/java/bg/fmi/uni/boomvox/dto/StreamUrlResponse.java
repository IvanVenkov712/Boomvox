package bg.fmi.uni.boomvox.dto;

import java.time.Instant;

public record StreamUrlResponse(
    String  streamUrl,   // signed S3 URL — expires in 15 min, never log or store this
    Instant expiresAt,   // Angular uses this to know when to refresh
    long    sessionId,   // opened StreamingSession id — Angular needs this for event calls
    long    durationSec,  // from song.duration
    int     bitrateKbps,   // new
    String  format
) {}
