import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { ArtistService } from '../../services/artist';
import { UserResponse } from '../../models/users';
import { SongResponse } from '../../models/songs';
import { AlbumResponse } from '../../models/albums';

@Component({
  selector: 'app-artist-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './artist-detail.html',
  styleUrl: './artist-detail.css',
})
export class ArtistDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly artistService = inject(ArtistService);

  readonly artist = signal<UserResponse | null>(null);
  readonly songs = signal<SongResponse[]>([]);
  readonly albums = signal<AlbumResponse[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    forkJoin({
      songs: this.artistService.getSongs(id),
      albums: this.artistService.getAlbums(id),
    }).subscribe({
      next: ({ songs, albums }) => {
        const artist = this.artistService.getById(id);
        this.artist.set(artist ?? null);
        this.songs.set(songs);
        this.albums.set(albums);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Could not load artist.');
        this.loading.set(false);
      },
    });
  }

  goToSong(id: number): void {
    this.router.navigate(['/songs', id]);
  }

  goToAlbum(id: number): void {
    this.router.navigate(['/albums', id]);
  }

  formatDuration(seconds: number): string {
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${m}:${s.toString().padStart(2, '0')}`;
  }
}
