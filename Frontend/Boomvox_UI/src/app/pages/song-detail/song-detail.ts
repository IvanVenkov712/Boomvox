import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe, LowerCasePipe } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { SongService } from '../../services/song';
import { SongResponse } from '../../models/songs';
import { Rating } from '../../components/rating/rating';
import { PlayerService } from '../../services/player';
import { FavouritesService } from '../../services/favourites';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-song-detail',
  standalone: true,
  imports: [Rating, RouterLink, DatePipe, LowerCasePipe],
  templateUrl: './song-detail.html',
  styleUrl: './song-detail.css',
})
export class SongDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly songService = inject(SongService);
  readonly playerService = inject(PlayerService);
  readonly favouritesService = inject(FavouritesService);
  private readonly authService = inject(AuthService);

  readonly song = signal<SongResponse | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly addedToFavourites = signal(false);
  readonly addingToFavourites = signal(false);

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      const id = Number(params.get('id'));
      this.song.set(null);
      this.loading.set(true);
      this.error.set(null);
      this.addedToFavourites.set(false);
      this.addingToFavourites.set(false);
      
      this.songService.fetchById(id).subscribe({
        next: (song) => {
          this.song.set(song);
          this.loading.set(false);
        },
        error: () => {
          this.error.set('Song not found');
          this.loading.set(false);
        },
      });
    });
  }

  formatDuration(seconds: number): string {
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${m}:${s.toString().padStart(2, '0')}`;
  }

  formatFileSize(bytes: number): string {
    return (bytes / 1_000_000).toFixed(1) + ' MB';
  }

  goBack() {
    this.router.navigate(['/songs']);
  }

  addToFavourites() {
    const song = this.song();
    if (!song) return;
    this.addingToFavourites.set(true);
    this.favouritesService.addSong(song.id).subscribe({
      next: () => {
        this.addedToFavourites.set(true);
        this.addingToFavourites.set(false);
      },
      error: () => this.addingToFavourites.set(false),
    });
  }

  get isAuthor(): boolean {
    const role = this.authService.getRole();
    return role === 'AUTHOR' || role === 'ADMIN';
  }
}