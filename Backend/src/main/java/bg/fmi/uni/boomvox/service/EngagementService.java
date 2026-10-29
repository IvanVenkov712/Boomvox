package bg.fmi.uni.boomvox.service;

import bg.fmi.uni.boomvox.domain.Rating;
import bg.fmi.uni.boomvox.domain.Song;
import bg.fmi.uni.boomvox.domain.User;
import bg.fmi.uni.boomvox.dto.RatingRequest;
import bg.fmi.uni.boomvox.dto.RatingResponse;
import bg.fmi.uni.boomvox.dto.SongStatsResponse;
import bg.fmi.uni.boomvox.exception.NotFoundException;
import bg.fmi.uni.boomvox.exception.ValidationException;
import bg.fmi.uni.boomvox.ids.RatingId;
import bg.fmi.uni.boomvox.repository.RatingRepository;
import bg.fmi.uni.boomvox.repository.SongRepository;
import bg.fmi.uni.boomvox.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class EngagementService extends BaseService {

    private final RatingRepository ratingRepository;
    private final SongRepository songRepository;
    private final UserRepository userRepository;

    public EngagementService(
        RatingRepository ratingRepository,
        SongRepository songRepository,
        UserRepository userRepository
    ) {
        this.ratingRepository = ratingRepository;
        this.songRepository = songRepository;
        this.userRepository = userRepository;
    }

    public RatingResponse rateSong(long userId, RatingRequest request) {
        User user = findUser(userId);
        Song song = findSong(request.songId());
        String comment = normalizeComment(request.comment());
        RatingId id = new RatingId(user.getId(), song.getId());
        LocalDateTime now = LocalDateTime.now();
        Rating rating = ratingRepository.findById(id)
            .map(existingRating -> {
                existingRating.update(request.grade(), comment, now);
                return existingRating;
            })
            .orElseGet(() -> new Rating(user, song, request.grade(), comment, now));

        Rating savedRating = ratingRepository.save(rating);
        refreshSongRatingStats(song);

        return RatingResponse.from(savedRating);
    }

    public void deleteRating(long userId, long songId) {
        Rating rating = findRating(userId, songId);
        Song song = rating.getSong();

        ratingRepository.delete(rating);
        refreshSongRatingStats(song);
    }

    public SongStatsResponse refreshSongRatingStats(long songId) {
        return SongStatsResponse.from(refreshSongRatingStats(findSong(songId)).getStats());
    }

    @Transactional(readOnly = true)
    public RatingResponse getRating(long userId, long songId) {
        return RatingResponse.from(findRating(userId, songId));
    }

    @Transactional(readOnly = true)
    public List<RatingResponse> getRatingsByUser(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User", userId);
        }

        return ratingRepository.findByUserIdOrderByLastUpdatedAtDesc(userId).stream()
            .map(RatingResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<RatingResponse> getSongFeedback(long songId) {
        if (!songRepository.existsById(songId)) {
            throw new NotFoundException("Song", songId);
        }

        return ratingRepository.findBySongIdOrderByLastUpdatedAtDesc(songId).stream()
            .map(RatingResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public double getAverageSongRating(long songId) {
        return calculateAverageSongRating(findSong(songId));
    }

    private Song refreshSongRatingStats(Song song) {
        long ratingsCount = ratingRepository.countBySong(song);
        double avgRating = calculateAverageSongRating(song);

        song.getStats().updateRatingSummary(ratingsCount, avgRating);
        return song;
    }

    private double calculateAverageSongRating(Song song) {
        Double average = ratingRepository.findAverageSongRatingBySong(song);
        return average == null ? 0 : average;
    }

    private Rating findRating(long userId, long songId) {
        return ratingRepository.findById(new RatingId(userId, songId))
            .orElseThrow(() -> new NotFoundException(
                "Rating not found with user id " + userId + " and song id " + songId
            ));
    }

    private Song findSong(long id) {
        return songRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Song", id));
    }

    private User findUser(long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User", id));
    }

    private String normalizeComment(String comment) {
        if (comment == null || comment.isBlank()) {
            throw new ValidationException("Rating comment is required");
        }

        return comment.trim();
    }
}
