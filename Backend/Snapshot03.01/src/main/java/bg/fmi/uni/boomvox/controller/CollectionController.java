package bg.fmi.uni.boomvox.controller;

import bg.fmi.uni.boomvox.dto.FavouritesListResponse;
import bg.fmi.uni.boomvox.dto.FavouritesListSongRequest;
import bg.fmi.uni.boomvox.dto.FavouritesListSongResponse;
import bg.fmi.uni.boomvox.dto.PlaylistRequest;
import bg.fmi.uni.boomvox.dto.PlaylistResponse;
import bg.fmi.uni.boomvox.dto.PlaylistSongRequest;
import bg.fmi.uni.boomvox.dto.PlaylistSongResponse;
import bg.fmi.uni.boomvox.security.UserPrincipal;
import bg.fmi.uni.boomvox.service.FavouritesService;
import bg.fmi.uni.boomvox.service.PlaylistService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CollectionController {

    private final PlaylistService playlistService;
    private final FavouritesService favouritesService;

    public CollectionController(PlaylistService playlistService, FavouritesService favouritesService) {
        this.playlistService = playlistService;
        this.favouritesService = favouritesService;
    }

    // ── Playlists ─────────────────────────────────────────────────────────────

    @GetMapping("/playlists")
    public ResponseEntity<List<PlaylistResponse>> getPlaylists(
        @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(
            playlistService.getPlaylistsByOwner(currentUser.getUser().getId())
        );
    }

    @PostMapping("/playlists")
    public ResponseEntity<PlaylistResponse> createPlaylist(
        @AuthenticationPrincipal UserPrincipal currentUser,
        @Valid @RequestBody PlaylistRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(playlistService.createPlaylist(currentUser.getUser().getId(), request));
    }

    @GetMapping("/playlists/{playlistId}")
    public ResponseEntity<PlaylistResponse> getPlaylist(
        @AuthenticationPrincipal UserPrincipal currentUser,
        @PathVariable long playlistId
    ) {
        return ResponseEntity.ok(
            playlistService.getPlaylistById(currentUser.getUser().getId(), playlistId)
        );
    }

    @DeleteMapping("/playlists/{playlistId}")
    public ResponseEntity<Void> deletePlaylist(
        @AuthenticationPrincipal UserPrincipal currentUser,
        @PathVariable long playlistId
    ) {
        playlistService.deletePlaylist(currentUser.getUser().getId(), playlistId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/playlists/{playlistId}/songs")
    public ResponseEntity<PlaylistSongResponse> addSongToPlaylist(
        @AuthenticationPrincipal UserPrincipal currentUser,
        @PathVariable long playlistId,
        @Valid @RequestBody PlaylistSongRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(playlistService.addSongToPlaylist(currentUser.getUser().getId(), request));
    }

    @DeleteMapping("/playlists/{playlistId}/songs/{songId}")
    public ResponseEntity<Void> removeSongFromPlaylist(
        @AuthenticationPrincipal UserPrincipal currentUser,
        @PathVariable long playlistId,
        @PathVariable long songId
    ) {
        playlistService.removeSongFromPlaylist(currentUser.getUser().getId(), playlistId, songId);
        return ResponseEntity.noContent().build();
    }

    // ── Favourites ────────────────────────────────────────────────────────────

    @GetMapping("/favourites")
    public ResponseEntity<FavouritesListResponse> getFavourites(
        @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(
            favouritesService.getFavouritesList(currentUser.getUser().getId())
        );
    }

    @PostMapping("/favourites/songs")
    public ResponseEntity<FavouritesListSongResponse> addSongToFavourites(
        @AuthenticationPrincipal UserPrincipal currentUser,
        @Valid @RequestBody FavouritesListSongRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(favouritesService.addSongToFavourites(currentUser.getUser().getId(), request));
    }

    @DeleteMapping("/favourites/songs/{songId}")
    public ResponseEntity<Void> removeSongFromFavourites(
        @AuthenticationPrincipal UserPrincipal currentUser,
        @PathVariable long songId
    ) {
        FavouritesListResponse favourites = favouritesService.getFavouritesList(currentUser.getUser().getId());
        favouritesService.removeSongFromFavourites(currentUser.getUser().getId(), favourites.id(), songId);
        return ResponseEntity.noContent().build();
    }
}
