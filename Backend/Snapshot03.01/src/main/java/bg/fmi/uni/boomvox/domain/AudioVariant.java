package bg.fmi.uni.boomvox.domain;

import bg.fmi.uni.boomvox.enums.AudioVariantStatus;
import bg.fmi.uni.boomvox.enums.SongFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "audio_variant")
@NoArgsConstructor
public class AudioVariant {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SongFormat format;

    @Getter
    @Column(name = "bitrate_kbps", nullable = false)
    private int bitrateKbps;

    @Getter
    @Column(name = "duration_sec", nullable = false)
    private int durationSec;

    @Getter
    @Column(name = "file_size_bytes", nullable = false)
    private long fileSizeBytes;

    /**
     * Internal S3 URI — e.g. "s3://your-bucket/variants/song_42_128k.mp3".
     * Never returned to clients.
     */
    @Getter
    @Column(name = "storage_url", nullable = false)
    private String storageUrl;

    /**
     * The S3 object key used for pre-signing — e.g. "variants/song_42_128k.mp3".
     * Never returned to clients.
     */
    @Getter
    @Column(name = "streaming_key", nullable = false)
    private String streamingKey;

    /** True for the 128 kbps MP3 — served to guests and free users. */
    @Getter
    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AudioVariantStatus status;

    @Version
    private long version;

    public AudioVariant(Song song, SongFormat format, int bitrateKbps, int durationSec,
                        long fileSizeBytes, String storageUrl, String streamingKey,
                        boolean isDefault) {
        this.song          = song;
        this.format        = format;
        this.bitrateKbps   = bitrateKbps;
        this.durationSec   = durationSec;
        this.fileSizeBytes = fileSizeBytes;
        this.storageUrl    = storageUrl;
        this.streamingKey  = streamingKey;
        this.isDefault     = isDefault;
        this.status        = AudioVariantStatus.PROCESSING;
    }
}
