import { Component, inject, OnInit, signal } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { filter } from 'rxjs';
import { LayoutService, SidebarMode } from '../../services/layout';
import { SongService } from '../../services/song';
import { ArtistService } from '../../services/artist';
import { AlbumService } from '../../services/album';
import { PlaylistService } from '../../services/playlist';
import { FavouritesService } from '../../services/favourites';
import { SongResponse } from '../../models/songs';
import { AlbumResponse } from '../../models/albums';
import { PlaylistSongResponse } from '../../models/playlists';
import { FavouritesListSongResponse } from '../../models/favourites';
import { UserResponse } from '../../models/users';

@Component({
  selector: 'app-sidebar-browser',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './sidebar-browser.html',
  styleUrl: './sidebar-browser.css',
})
export class SidebarBrowser implements OnInit {
  private readonly router = inject(Router);
  readonly layoutService = inject(LayoutService);
  private readonly songService = inject(SongService);
  private readonly artistService = inject(ArtistService);
  private readonly albumService = inject(AlbumService);
  private readonly playlistService = inject(PlaylistService);
  private readonly favouritesService = inject(FavouritesService);

  readonly mode = this.layoutService.sidebarMode;
  readonly collapsed = this.layoutService.sidebarCollapsed;

  // search/songs mode
  readonly songs = signal<SongResponse[]>([]);
  searchQuery = '';
  genreFilter = '';

  // artists mode
  readonly artists = signal<UserResponse[]>([]);

  // album mode
  readonly albumSongs = signal<SongResponse[]>([]);

  // playlist mode
  readonly playlistSongs = signal<PlaylistSongResponse[]>([]);

  // favourites mode
  readonly favouriteSongs = signal<FavouritesListSongResponse[]>([]);

  readonly loading = signal(false);

  private currentAlbumId: number | null = null;
  private currentPlaylistId: number | null = null;

  ngOnInit() {
    this.router.events.pipe(filter((e) => e instanceof NavigationEnd)).subscribe((e) => {
      const url = (e as NavigationEnd).urlAfterRedirects;
      this.onRouteChange(url);
    });

    // handle initial load
    this.onRouteChange(this.router.url);
  }

  private onRouteChange(url: string) {
    if (url === '/login' || url === '/profile') {
      this.layoutService.setSidebarMode('hidden');
      return;
    }

    if (url === '/songs' || url === '/home') {
      this.layoutService.setSidebarMode('search');
      this.loadSongs();
      return;
    }

    if (url.startsWith('/artists')) {
      this.layoutService.setSidebarMode('artists');
      this.loadArtists();
      return;
    }

    const albumMatch = url.match(/^\/albums\/(\d+)/);
    if (albumMatch) {
      const id = Number(albumMatch[1]);
      if (id !== this.currentAlbumId) {
        this.currentAlbumId = id;
        this.layoutService.setSidebarMode('album');
        this.loadAlbumSongs(id);
      }
      return;
    }

    const playlistMatch = url.match(/^\/playlists\/(\d+)/);
    if (playlistMatch) {
      const id = Number(playlistMatch[1]);
      if (id !== this.currentPlaylistId) {
        this.currentPlaylistId = id;
        this.layoutService.setSidebarMode('playlist');
        this.loadPlaylistSongs(id);
      }
      return;
    }

    if (url === '/favourites') {
      this.layoutService.setSidebarMode('favourites');
      this.loadFavourites();
      return;
    }

    // default — show search for any other route
    this.layoutService.setSidebarMode('search');
    this.loadSongs();
  }

  loadSongs() {
    this.loading.set(true);
    this.songService.browse({ search: this.searchQuery, genre: this.genreFilter }).subscribe({
      next: (songs) => {
        this.songs.set(songs);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  private loadArtists() {
    this.loading.set(true);
    this.artistService.getAll().subscribe({
      next: (artists) => {
        this.artists.set(artists);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  private loadAlbumSongs(albumId: number) {
    this.loading.set(true);
    this.albumService.getSongs(albumId).subscribe({
      next: (songs) => {
        this.albumSongs.set(songs);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  private loadPlaylistSongs(playlistId: number) {
    this.loading.set(true);
    this.playlistService.getSongs(playlistId).subscribe({
      next: (songs) => {
        this.playlistSongs.set(songs);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  private loadFavourites() {
    this.loading.set(true);
    this.favouritesService.getSongs().subscribe({
      next: (songs) => {
        this.favouriteSongs.set(songs);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  goToSong(id: number) {
    this.router.navigate(['/songs', id]);
  }
  goToArtist(id: number) {
    this.router.navigate(['/artists', id]);
  }
}
