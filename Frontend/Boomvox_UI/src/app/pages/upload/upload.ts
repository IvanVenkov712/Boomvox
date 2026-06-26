import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { SongService } from '../../services/song';
import { AuthService } from '../../services/auth';
import { SongResponse } from '../../models/songs';
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
  private readonly router = inject(Router);

  readonly uploading = signal(false);
  readonly polling = signal(false);
  readonly error = signal<string | null>(null);
  readonly uploadedSong = signal<SongResponse | null>(null);

  readonly genres: Genre[] = Object.values(Genre);

  name = '';
  genre: Genre = Genre.POP;
  albumId: number | null = null;
  selectedFile: File | null = null;

  constructor() {
    // redirect non-authors away
    const role = this.authService.getRole();
    if (role !== 'AUTHOR' && role !== 'ADMIN') {
      this.router.navigate(['/home']);
    }
  }

  onFileChange(event: Event) {
    const input = event.target as HTMLInputElement;
    this.selectedFile = input.files?.[0] ?? null;
  }

  upload() {
    if (!this.selectedFile || !this.name.trim()) return;
    this.uploading.set(true);
    this.error.set(null);

    this.songService
      .upload(this.selectedFile, {
        name: this.name.trim(),
        genre: this.genre,
        albumId: this.albumId,
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
