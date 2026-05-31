package bg.fmi.uni.boomvox.controller;

import bg.fmi.uni.boomvox.domain.AudioVariant;
import bg.fmi.uni.boomvox.domain.Song;
import bg.fmi.uni.boomvox.domain.StreamingSession;
import bg.fmi.uni.boomvox.dto.StreamUrlResponse;
import bg.fmi.uni.boomvox.enums.AudioVariantStatus;
import bg.fmi.uni.boomvox.enums.SongProcessingStatus;
import bg.fmi.uni.boomvox.enums.UserRole;
import bg.fmi.uni.boomvox.exception.NotFoundException;
import bg.fmi.uni.boomvox.repository.AudioVariantRepository;
import bg.fmi.uni.boomvox.repository.SongRepository;
import bg.fmi.uni.boomvox.service.FileStorageService;
import bg.fmi.uni.boomvox.service.PlaybackService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.attribute.UserPrincipal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

import static bg.fmi.uni.boomvox.enums.UserRole.ADMIN;

@RestController
@RequestMapping("/api/songs")
public class StreamingController {

    private final SongRepository      songRepository;
    private final FileStorageService fileStorageService;
    private final PlaybackService     playbackService;
    private final AudioVariantRepository audioVariantRepository;

    public StreamingController(
        SongRepository songRepository,
        FileStorageService fileStorageService,
        PlaybackService playbackService,
        AudioVariantRepository audioVariantRepository
    ) {
        this.songRepository     = songRepository;
        this.fileStorageService = fileStorageService;
        this.playbackService    = playbackService;
        this.audioVariantRepository = audioVariantRepository;
    }

    /**
     * GET /api/songs/{songId}/stream
     *
     * Returns a signed S3 URL. The browser streams audio directly from S3 using this URL.
     * Spring Security has already validated the JWT before this method runs.
     */
    @GetMapping("/{songId}/stream")
    public ResponseEntity<StreamUrlResponse> getStreamUrl(
        @PathVariable long songId,
        @AuthenticationPrincipal UserPrincipal currentUser) {

        Song song = songRepository.findById(songId)
            .orElseThrow(() -> new NotFoundException("Song", songId));

        // Gate: song must be fully processed before any signed URL is issued
        if (song.getProcessingStatus() != SongProcessingStatus.ACTIVE) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }

        // Select the best available variant for this user's role
        List<AudioVariant> variants =
            audioVariantRepository.findBySongIdAndStatus(songId, AudioVariantStatus.READY);

        AudioVariant variant = selectVariant(variants, currentUser.getRole());

        // Sign the variant's streaming key — NOT song.storageKey
        String signedUrl = fileStorageService.generateSignedUrl(variant.getStreamingKey());

        StreamingSession session = playbackService.startSession(
            songId, currentUser.getId(), variant.getId()   // pass variantId — see Step 22
        );

        return ResponseEntity.ok(new StreamUrlResponse(
            signedUrl,
            Instant.now().plus(15, ChronoUnit.MINUTES),
            session.getId(),
            variant.getDurationSec(),
            variant.getBitrateKbps(),   // new fields in StreamUrlResponse
            variant.getFormat().name()
        ));
    }

    private AudioVariant selectVariant(List<AudioVariant> variants, UserRole role) {
        return switch (role) {
            case GUEST -> variants.stream()
                .filter(AudioVariant::isDefault)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("No default variant available"));

            case USER, ARTIST, ADMIN -> variants.stream()
                .max(Comparator.comparingInt(AudioVariant::getBitrateKbps))
                .orElseThrow(() -> new NotFoundException("No variant available"));
        };
    }
}
