package bg.fmi.uni.boomvox.controller;

import bg.fmi.uni.boomvox.dto.*;
import bg.fmi.uni.boomvox.enums.Genre;
import bg.fmi.uni.boomvox.enums.SongFormat;
import bg.fmi.uni.boomvox.service.AlbumService;
import bg.fmi.uni.boomvox.service.SongService;
import bg.fmi.uni.boomvox.service.SongTagService;
import bg.fmi.uni.boomvox.service.TagService;
import bg.fmi.uni.boomvox.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class LibraryController {

    private final SongService songService;
    private final SongTagService songTagService;
    private final TagService tagService;
    private final AlbumService albumService;
    private final UserService userService;

    public LibraryController(
        SongService songService,
        SongTagService songTagService,
        TagService tagService,
        AlbumService albumService,
        UserService userService
    ) {
        this.songService = songService;
        this.songTagService = songTagService;
        this.tagService = tagService;
        this.albumService = albumService;
        this.userService = userService;
    }

    // ── Songs ────────────────────────────────────────────────────────────────

    @GetMapping("/songs")
    public ResponseEntity<List<SongResponse>> browseSongs(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Genre genre,
        @RequestParam(required = false) SongFormat format,
        @RequestParam(required = false) Long albumId,
        @RequestParam(required = false) Long artistId,
        @RequestParam(required = false) Long tagId
    ) {
        boolean hasFilter = search != null || genre != null || format != null
            || albumId != null || artistId != null || tagId != null;

        List<SongResponse> songs = hasFilter
            ? songService.filterSongs(search, albumId, artistId, genre, format, tagId)
            : songService.browseSongs();

        return ResponseEntity.ok(songs);
    }

    @GetMapping("/songs/{songId}")
    public ResponseEntity<SongResponse> getSong(@PathVariable long songId) {
        return ResponseEntity.ok(songService.getSongById(songId));
    }

    @PutMapping("/songs/{songId}")
    @PreAuthorize("hasAnyRole('ARTIST', 'ADMIN')")
    public ResponseEntity<SongResponse> updateSong(
        @PathVariable long songId,
        @Valid @RequestBody SongRequest request
    ) {
        return ResponseEntity.ok(songService.updateSong(songId, request));
    }

    @DeleteMapping("/songs/{songId}")
    @PreAuthorize("hasAnyRole('ARTIST', 'ADMIN')")
    public ResponseEntity<Void> deleteSong(@PathVariable long songId) {
        songService.deleteSong(songId);
        return ResponseEntity.noContent().build();
    }

    // ── Song ↔ Tag ───────────────────────────────────────────────────────────

    @PostMapping("/songs/{songId}/tags")
    @PreAuthorize("hasAnyRole('ARTIST', 'ADMIN')")
    public ResponseEntity<SongTagResponse> attachTag(
        @PathVariable long songId,
        @Valid @RequestBody SongTagRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(songTagService.attachTagToSong(request));
    }

    @DeleteMapping("/songs/{songId}/tags/{tagId}")
    @PreAuthorize("hasAnyRole('ARTIST', 'ADMIN')")
    public ResponseEntity<Void> detachTag(
        @PathVariable long songId,
        @PathVariable long tagId
    ) {
        songTagService.detachTagFromSong(songId, tagId);
        return ResponseEntity.noContent().build();
    }

    // ── Tags ─────────────────────────────────────────────────────────────────

    @GetMapping("/tags")
    public ResponseEntity<List<TagResponse>> browseTags(
        @RequestParam(required = false) String search
    ) {
        List<TagResponse> tags = search != null
            ? tagService.searchTags(search)
            : tagService.browseTags();

        return ResponseEntity.ok(tags);
    }

    @PostMapping("/tags")
    @PreAuthorize("hasAnyRole('ARTIST', 'ADMIN')")
    public ResponseEntity<TagResponse> createTag(@Valid @RequestBody TagRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tagService.createTag(request));
    }

    // ── Albums ───────────────────────────────────────────────────────────────

    @GetMapping("/albums")
    public ResponseEntity<List<AlbumResponse>> browseAlbums(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Long artistId,
        @RequestParam(required = false) Genre genre
    ) {
        boolean hasFilter = search != null || artistId != null || genre != null;

        List<AlbumResponse> albums = hasFilter
            ? albumService.filterAlbums(search, artistId, genre)
            : albumService.browseAlbums();

        return ResponseEntity.ok(albums);
    }

    @GetMapping("/albums/{albumId}")
    public ResponseEntity<AlbumResponse> getAlbum(@PathVariable long albumId) {
        return ResponseEntity.ok(albumService.getAlbumById(albumId));
    }

    @PostMapping("/albums")
    @PreAuthorize("hasAnyRole('ARTIST', 'ADMIN')")
    public ResponseEntity<AlbumResponse> createAlbum(@Valid @RequestBody AlbumRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(albumService.createAlbum(request));
    }

    @PutMapping("/albums/{albumId}")
    @PreAuthorize("hasAnyRole('ARTIST', 'ADMIN')")
    public ResponseEntity<AlbumResponse> updateAlbum(
        @PathVariable long albumId,
        @Valid @RequestBody AlbumRequest request
    ) {
        return ResponseEntity.ok(albumService.updateAlbum(albumId, request));
    }

    // ── Artists ───────────────────────────────────────────────────────────────

    @GetMapping("/artists")
    public ResponseEntity<List<UserResponse>> getArtists() {
        return ResponseEntity.ok(userService.getArtists());
    }

    @GetMapping("/artists/{artistId}")
    public ResponseEntity<UserResponse> getArtist(@PathVariable long artistId) {
        return ResponseEntity.ok(userService.getUserById(artistId));
    }

    @GetMapping("/artists/{artistId}/songs")
    public ResponseEntity<List<SongResponse>> getArtistSongs(@PathVariable long artistId) {
        return ResponseEntity.ok(songService.getSongsByArtist(artistId));
    }

    @GetMapping("/artists/{artistId}/albums")
    public ResponseEntity<List<AlbumResponse>> getArtistAlbums(@PathVariable long artistId) {
        return ResponseEntity.ok(albumService.getAlbumsByAuthor(artistId));
    }
}