package bg.fmi.uni.boomvox.service;

import bg.fmi.uni.boomvox.domain.FavouritesListSong;
import bg.fmi.uni.boomvox.domain.ListeningHistory;
import bg.fmi.uni.boomvox.domain.Rating;
import bg.fmi.uni.boomvox.domain.Song;
import bg.fmi.uni.boomvox.domain.SongTag;
import bg.fmi.uni.boomvox.domain.User;
import bg.fmi.uni.boomvox.dto.UserPreferenceResponse;
import bg.fmi.uni.boomvox.enums.Genre;
import bg.fmi.uni.boomvox.exception.NotFoundException;
import bg.fmi.uni.boomvox.repository.FavouritesListSongRepository;
import bg.fmi.uni.boomvox.repository.ListeningHistoryRepository;
import bg.fmi.uni.boomvox.repository.RatingRepository;
import bg.fmi.uni.boomvox.repository.SongTagRepository;
import bg.fmi.uni.boomvox.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class UserPreferenceService {

    private static final double FAVOURITE_WEIGHT = 4.0;
    private static final double MIN_RATING_GRADE = 5.0;

    private final UserRepository userRepository;
    private final ListeningHistoryRepository listeningHistoryRepository;
    private final FavouritesListSongRepository favouritesListSongRepository;
    private final RatingRepository ratingRepository;
    private final SongTagRepository songTagRepository;

    public UserPreferenceService(
        UserRepository userRepository,
        ListeningHistoryRepository listeningHistoryRepository,
        FavouritesListSongRepository favouritesListSongRepository,
        RatingRepository ratingRepository,
        SongTagRepository songTagRepository
    ) {
        this.userRepository = userRepository;
        this.listeningHistoryRepository = listeningHistoryRepository;
        this.favouritesListSongRepository = favouritesListSongRepository;
        this.ratingRepository = ratingRepository;
        this.songTagRepository = songTagRepository;
    }

    public UserPreferenceResponse getPreferences(long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User", userId));

        Map<Song, Double> songScores = buildSongScores(user);
        Map<Genre, Double> genreScores = aggregateScores(
            songScores,
            song -> song.getAlbum().getGenre()
        );
        Map<Long, Double> artistScores = aggregateScores(
            songScores,
            song -> song.getAlbum().getAuthor().getId()
        );
        Map<String, Double> tagScores = buildTagScores(songScores);

        return new UserPreferenceResponse(userId, genreScores, artistScores, tagScores);
    }

    private Map<Song, Double> buildSongScores(User user) {
        Map<Song, Double> scores = new LinkedHashMap<>();

        listeningHistoryRepository.findByUserOrderByListenedAtDesc(user).forEach(history ->
            addScore(scores, history.getSong(), calculateListeningScore(history))
        );

        if (user.getFavouritesList() != null) {
            favouritesListSongRepository.findByFavouritesList(user.getFavouritesList()).stream()
                .map(FavouritesListSong::getSong)
                .forEach(song -> addScore(scores, song, FAVOURITE_WEIGHT));
        }

        ratingRepository.findByUser(user).forEach(rating ->
            addScore(scores, rating.getSong(), calculateRatingScore(rating))
        );

        return scores;
    }

    private double calculateListeningScore(ListeningHistory history) {
        long songDuration = history.getSong().getDuration();
        if (songDuration <= 0) {
            return 0;
        }

        return Math.min((double) history.getListenedDurationSec() / songDuration, 1.0);
    }

    private double calculateRatingScore(Rating rating) {
        return rating.getGrade() - MIN_RATING_GRADE;
    }

    private Map<String, Double> buildTagScores(Map<Song, Double> songScores) {
        Map<String, Double> scores = new LinkedHashMap<>();

        songScores.forEach((song, songScore) ->
            songTagRepository.findBySong(song).stream()
                .map(SongTag::getTag)
                .forEach(tag -> scores.merge(tag.getWord(), songScore, Double::sum))
        );

        return sortByScore(scores);
    }

    private <K> Map<K, Double> aggregateScores(
        Map<Song, Double> songScores,
        Function<Song, K> classifier
    ) {
        Map<K, Double> scores = songScores.entrySet().stream()
            .collect(Collectors.toMap(
                entry -> classifier.apply(entry.getKey()),
                Map.Entry::getValue,
                Double::sum
            ));

        return sortByScore(scores);
    }

    private <K> Map<K, Double> sortByScore(Map<K, Double> scores) {
        return scores.entrySet().stream()
            .sorted(Map.Entry.<K, Double>comparingByValue(Comparator.reverseOrder()))
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (first, second) -> first,
                LinkedHashMap::new
            ));
    }

    private void addScore(Map<Song, Double> scores, Song song, double score) {
        scores.merge(song, score, Double::sum);
    }
}
