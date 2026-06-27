package bg.fmi.uni.boomvox.controller;

import bg.fmi.uni.boomvox.dto.SongResponse;
import bg.fmi.uni.boomvox.dto.SongUploadRequest;
import bg.fmi.uni.boomvox.exception.ValidationException;
import bg.fmi.uni.boomvox.service.SongService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/songs")
public class SongController {

    private final SongService songService;

    public SongController(SongService songService) {
        this.songService = songService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('AUTHOR', 'ADMIN')")
    public ResponseEntity<SongResponse> uploadSong(
        @RequestPart("audio") MultipartFile audioFile,
        @RequestPart("metadata") @Valid SongUploadRequest metadata) {

        validateAudioMimeType(audioFile);

        if (audioFile.getSize() > 100L * 1024 * 1024) {
            throw new ValidationException("Audio file must not exceed 100 MB");
        }

        SongResponse response = songService.uploadSong(audioFile, metadata);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private void validateAudioMimeType(MultipartFile file) {
        String ct = file.getContentType();
        if (!"audio/mpeg".equals(ct) && !"audio/wav".equals(ct)) {
            throw new ValidationException("Only MP3 and WAV files are accepted");
        }
    }
}