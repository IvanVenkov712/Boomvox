package bg.fmi.uni.boomvox.service;

import bg.fmi.uni.boomvox.domain.FavouritesListSong;
import bg.fmi.uni.boomvox.domain.Recommendation;
import bg.fmi.uni.boomvox.domain.Song;
import bg.fmi.uni.boomvox.domain.SongTag;
import bg.fmi.uni.boomvox.domain.User;
import bg.fmi.uni.boomvox.dto.RecommendationResponse;
import bg.fmi.uni.boomvox.dto.UserPreferenceResponse;
import bg.fmi.uni.boomvox.enums.SongProcessingStatus;
import bg.fmi.uni.boomvox.exception.NotFoundException;
import bg.fmi.uni.boomvox.exception.ValidationException;
import bg.fmi.uni.boomvox.repository.FavouritesListSongRepository;
import bg.fmi.uni.boomvox.repository.ListeningHistoryRepository;
import bg.fmi.uni.boomvox.repository.RecommendationRepository;
import bg.fmi.uni.boomvox.repository.SongRepository;
import bg.fmi.uni.boomvox.repository.SongTagRepository;
import bg.fmi.uni.boomvox.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class RecommendationService {

    private static final int MAX_RECOMMENDATIONS = 100;
    private static final double GENRE_WEIGHT = 3.0;
    private static final double ARTIST_WEIGHT = 2.0;
    private static final double TAG_WEIGHT = 1.0;
    private static final double POPULARITY_WEIGHT = 0.25;

    private final UserPreferenceService userPreferenceService;
    private final UserRepository userRepository;
    private final SongRepository songRepository;
    private final SongTagRepository songTagRepository;
    private final ListeningHistoryRepository listeningHistoryRepository;
    private final FavouritesListSongRepository favouritesListSongRepository;
    private final RecommendationRepository recommendationRepository;

    public RecommendationService(
        UserPreferenceService userPreferenceService,
        UserRepository userRepository,
        SongRepository songRepository,
        SongTagRepository songTagRepository,
        ListeningHistoryRepository listeningHistoryRepository,
        FavouritesListSongRepository favouritesListSongRepository,
        RecommendationRepository recommendationRepository
    ) {
        this.userPreferenceService = userPreferenceService;
        this.userRepository = userRepository;
        this.songRepository = songRepository;
        this.songTagRepository = songTagRepository;
        this.listeningHistoryRepository = listeningHistoryRepository;
        this.favouritesListSongRepository = favouritesListSongRepository;
        this.recommendationRepository = recommendationRepository;
    }

    public List<RecommendationResponse> generateRecommendations(long userId, int limit) {
        validateLimit(limit);
        User user = findUser(userId);
        UserPreferenceResponse preferences = userPreferenceService.getPreferences(userId);
        Set<Long> excludedSongIds = findExcludedSongIds(user);

        List<ScoredSong> scoredSongs = songRepository.findAll().stream()
            .filter(song -> song.getProcessingStatus() == SongProcessingStatus.ACTIVE)
            .filter(song -> !excludedSongIds.contains(song.getId()))
            .map(song -> new ScoredSong(song, calculateScore(song, preferences)))
            .filter(scoredSong -> scoredSong.score() >= 0)
            .sorted(Comparator.comparingDouble(ScoredSong::score).reversed())
            .limit(limit)
            .toList();

        double maxScore = scoredSongs.stream()
            .mapToDouble(ScoredSong::score)
            .max()
            .orElse(0);

        recommendationRepository.deleteByUser(user);
        recommendationRepository.flush();

        List<Recommendation> recommendations = scoredSongs.stream()
            .map(scoredSong -> new Recommendation(
                user,
                scoredSong.song(),
                toPercent(scoredSong.score(), maxScore)
            ))
            .toList();

        return recommendationRepository.saveAll(recommendations).stream()
            .map(RecommendationResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<RecommendationResponse> getRecommendations(long userId, int limit) {
        validateLimit(limit);
        User user = findUser(userId);

        return recommendationRepository.findByUserOrderByPercentDesc(user).stream()
            .limit(limit)
            .map(RecommendationResponse::from)
            .toList();
    }

    private double calculateScore(Song song, UserPreferenceResponse preferences) {
        double genreScore = preferences.genreScores().getOrDefault(song.getAlbum().getGenre(), 0.0);
        double artistScore = preferences.artistScores().getOrDefault(song.getAlbum().getAuthor().getId(), 0.0);
        double tagScore = songTagRepository.findBySong(song).stream()
            .map(SongTag::getTag)
            .mapToDouble(tag -> preferences.tagScores().getOrDefault(tag.getWord(), 0.0))
            .average()
            .orElse(0);

        return genreScore * GENRE_WEIGHT
            + artistScore * ARTIST_WEIGHT
            + tagScore * TAG_WEIGHT
            + calculatePopularityScore(song) * POPULARITY_WEIGHT;
    }

    private double calculatePopularityScore(Song song) {
        double ratingScore = song.getStats().getAvgRating() / 10.0;
        double playsScore = Math.log1p(song.getStats().getPlaysCount());
        return ratingScore + playsScore;
    }

    private Set<Long> findExcludedSongIds(User user) {
        Set<Long> excludedSongIds = new HashSet<>();

        listeningHistoryRepository.findByUserOrderByListenedAtDesc(user).forEach(history ->
            excludedSongIds.add(history.getSong().getId())
        );

        if (user.getFavouritesList() != null) {
            favouritesListSongRepository.findByFavouritesList(user.getFavouritesList()).stream()
                .map(FavouritesListSong::getSong)
                .map(Song::getId)
                .forEach(excludedSongIds::add);
        }

        return excludedSongIds;
    }

    private int toPercent(double score, double maxScore) {
        if (maxScore <= 0) {
            return 0;
        }

        return (int) Math.round(Math.clamp(score / maxScore, 0, 1) * 100);
    }

    private User findUser(long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User", userId));
    }

    private void validateLimit(int limit) {
        if (limit < 1 || limit > MAX_RECOMMENDATIONS) {
            throw new ValidationException("Recommendation limit must be between 1 and " + MAX_RECOMMENDATIONS);
        }
    }

    private record ScoredSong(Song song, double score) {
    }
}
