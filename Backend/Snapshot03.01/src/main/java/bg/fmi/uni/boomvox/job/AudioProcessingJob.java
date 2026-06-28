package bg.fmi.uni.boomvox.job;

import bg.fmi.uni.boomvox.domain.AudioVariant;
import bg.fmi.uni.boomvox.domain.Song;
import bg.fmi.uni.boomvox.enums.AudioVariantStatus;
import bg.fmi.uni.boomvox.enums.SongFormat;
import bg.fmi.uni.boomvox.enums.SongProcessingStatus;
import bg.fmi.uni.boomvox.exception.AudioProcessingException;
import bg.fmi.uni.boomvox.exception.NotFoundException;
import bg.fmi.uni.boomvox.repository.AudioVariantRepository;
import bg.fmi.uni.boomvox.repository.SongRepository;
import bg.fmi.uni.boomvox.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class AudioProcessingJob {

    private final FileStorageService fileStorageService;
    private final AudioVariantRepository audioVariantRepository;
    private final SongRepository songRepository;
    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    public AudioProcessingJob(
        FileStorageService     fileStorageService,
        AudioVariantRepository audioVariantRepository,
        SongRepository         songRepository,
        S3Client               s3Client
    ) {
        this.fileStorageService     = fileStorageService;
        this.audioVariantRepository = audioVariantRepository;
        this.songRepository         = songRepository;
        this.s3Client               = s3Client;
    }

    @Transactional
    @Async("audioProcessingExecutor")
    public void process(long songId, String rawS3Key) {
        Path tempInput  = null;
        List<Path> tempOutputs = new ArrayList<>();

        try {
            log.info("Starting audio processing for songId={}", songId);

            // 1. Download the raw file from S3 to a local temp path
            tempInput = downloadToTemp(rawS3Key);

            // 2. Validate with FFprobe
            FFprobe ffprobe = new FFprobe();
            FFmpegProbeResult probe = ffprobe.probe(tempInput.toString());
            if (probe.hasError()) {
                throw new AudioProcessingException("Invalid audio file for songId=" + songId);
            }
            int durationSec = (int) probe.getFormat().duration;

            // 3. Transcode into 3 variants
            FFmpeg ffmpeg = new FFmpeg();
            Song song = songRepository.findById(songId)
                .orElseThrow(() -> new NotFoundException("Song", songId));

            List<VariantSpec> specs = List.of(
                new VariantSpec(128, "mp3", "libmp3lame", SongFormat.MP3, true),
                new VariantSpec(320, "mp3", "libmp3lame", SongFormat.MP3, false),
                new VariantSpec(256, "aac", "aac",        SongFormat.AAC, false)
            );

            for (VariantSpec spec : specs) {
                Path tempOut = Files.createTempFile("variant_", "." + spec.format());
                tempOutputs.add(tempOut);

                FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(tempInput.toString())
                    .done()
                    .addOutput(tempOut.toString())
                    .setAudioCodec(spec.codec())
                    .setAudioBitRate(spec.bitrateKbps() * 1000L)
                    .done();

                new FFmpegExecutor(ffmpeg, ffprobe).createJob(builder).run();

                // Upload variant to S3
                String variantKey  = FileStorageService.variantKey(songId, spec.bitrateKbps(), spec.format());
                String contentType = spec.format().equals("mp3") ? "audio/mpeg" : "audio/aac";

                try (InputStream is = Files.newInputStream(tempOut)) {
                    fileStorageService.uploadFile(variantKey, is, Files.size(tempOut), contentType);
                }

                // Persist AudioVariant row
                AudioVariant variant = new AudioVariant(
                    song,
                    spec.songFormat(),
                    spec.bitrateKbps(),
                    durationSec,
                    Files.size(tempOut),
                    "s3://" + bucket + "/" + variantKey,
                    variantKey,
                    spec.isDefault()
                );
                variant.setStatus(AudioVariantStatus.READY);
                audioVariantRepository.save(variant);

                log.info("Variant ready: songId={} {}k.{}", songId, spec.bitrateKbps(), spec.format());
            }

            // 4. Mark song as ACTIVE — now streamable
            songRepository.updateDuration(songId, durationSec);
            songRepository.updateProcessingStatus(songId, SongProcessingStatus.ACTIVE);

            // 5. Delete the raw staging file
            fileStorageService.deleteFile(rawS3Key);

            log.info("Audio processing complete for songId={}", songId);

        } catch (Exception e) {
            log.error("Audio processing failed for songId={}", songId, e);
            songRepository.updateProcessingStatus(songId, SongProcessingStatus.FAILED);
        } finally {
            cleanupTemp(tempInput);
            tempOutputs.forEach(this::cleanupTemp);
        }
    }

    private Path downloadToTemp(String s3Key) throws IOException {
        Path temp = Files.createTempFile("raw_", ".audio");
        Files.delete(temp);
        s3Client.getObject(
            GetObjectRequest.builder().bucket(bucket).key(s3Key).build(),
            temp
        );
        return temp;
    }

    private void cleanupTemp(Path path) {
        if (path != null) {
            try { Files.deleteIfExists(path); } catch (IOException ignored) {}
        }
    }

    record VariantSpec(int bitrateKbps, String format, String codec,
                       SongFormat songFormat, boolean isDefault) {}
}
