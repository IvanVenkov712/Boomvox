import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { SongService } from '../../services/song';
import { AuthService } from '../../services/auth';
import { AlbumService } from '../../services/album';
import { SongResponse } from '../../models/songs';
import { AlbumResponse } from '../../models/albums';
import { Genre } from '../../models/enums';

@Component({
  selector: 'app-upload',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './upload.html',
  styleUrl: './upload.css',
})
export class Upload {
  private readonly songService = inject(SongService);
  private readonly authService = inject(AuthService);
  private readonly albumService = inject(AlbumService);
  private readonly router = inject(Router);

  readonly uploading = signal(false);
  readonly polling = signal(false);
  readonly error = signal<string | null>(null);
  readonly uploadedSong = signal<SongResponse | null>(null);

  // Album search
  readonly albumResults = signal<AlbumResponse[]>([]);
  readonly albumSearch = signal('');
  readonly selectedAlbum = signal<AlbumResponse | null>(null);
  readonly showAlbumDropdown = signal(false);

  // Create album
  readonly showCreateAlbum = signal(false);
  readonly creatingAlbum = signal(false);
  readonly albumCreateError = signal<string | null>(null);
  newAlbumName = '';
  newAlbumGenre: Genre = Genre.POP;

  readonly genres: Genre[] = Object.values(Genre);

  name = '';
  genre: Genre = Genre.POP;
  selectedFile: File | null = null;

  constructor() {
    const role = this.authService.getRole();
    if (role !== 'AUTHOR' && role !== 'ADMIN') {
      this.router.navigate(['/home']);
    }
  }

  onFileChange(event: Event) {
    const input = event.target as HTMLInputElement;
    this.selectedFile = input.files?.[0] ?? null;
  }

  searchAlbums() {
    const q = this.albumSearch().trim();
    if (!q) {
      this.albumResults.set([]);
      this.showAlbumDropdown.set(false);
      return;
    }
    this.albumService.browse({ search: q }).subscribe({
      next: (albums) => {
        this.albumResults.set(albums);
        this.showAlbumDropdown.set(albums.length > 0);
      },
      error: () => this.albumResults.set([]),
    });
  }

  selectAlbum(album: AlbumResponse) {
    this.selectedAlbum.set(album);
    this.albumSearch.set(album.name);
    this.showAlbumDropdown.set(false);
  }

  clearAlbum() {
    this.selectedAlbum.set(null);
    this.albumSearch.set('');
    this.albumResults.set([]);
  }

  createAlbum() {
    if (!this.newAlbumName.trim()) return;
    this.creatingAlbum.set(true);
    this.albumCreateError.set(null);
    this.albumService.create({ name: this.newAlbumName.trim(), genre: this.newAlbumGenre }).subscribe({
      next: (album) => {
        this.creatingAlbum.set(false);
        this.showCreateAlbum.set(false);
        this.selectAlbum(album);
        this.newAlbumName = '';
        this.newAlbumGenre = Genre.POP;
      },
      error: (err) => {
        this.creatingAlbum.set(false);
        this.albumCreateError.set(err.error?.message ?? 'Failed to create album');
      },
    });
  }

  upload() {
    if (!this.selectedFile || !this.name.trim()) return;
    this.uploading.set(true);
    this.error.set(null);

    this.songService
      .upload(this.selectedFile, {
        name: this.name.trim(),
        genre: this.genre,
        albumId: this.selectedAlbum()?.id ?? null,
      })
      .subscribe({
        next: (song) => {
          this.uploadedSong.set(song);
          this.uploading.set(false);
          this.pollStatus(song.id);
        },
        error: (err) => {
          this.error.set(err.error?.message ?? 'Upload failed');
          this.uploading.set(false);
        },
      });
  }

  private pollStatus(songId: number) {
    this.polling.set(true);
    const interval = setInterval(() => {
      this.songService.fetchById(songId).subscribe({
        next: (song) => {
          this.uploadedSong.set(song);
          if (song.processingStatus === 'ACTIVE' || song.processingStatus === 'FAILED') {
            clearInterval(interval);
            this.polling.set(false);
          }
        },
        error: () => {
          clearInterval(interval);
          this.polling.set(false);
        },
      });
    }, 3000);
  }

  goToSong() {
    const song = this.uploadedSong();
    if (song) this.router.navigate(['/songs', song.id]);
  }
}