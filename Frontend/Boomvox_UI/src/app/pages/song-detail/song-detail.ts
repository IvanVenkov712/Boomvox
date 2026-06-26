import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { SongService } from '../../services/song';
import { SongResponse } from '../../models/songs';
import { Rating } from '../../components/rating/rating';

@Component({
  selector: 'app-song-detail',
  standalone: true,
  imports: [Rating, RouterLink],
  templateUrl: './song-detail.html',
  styleUrl: './song-detail.css',
})
export class SongDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly songService = inject(SongService);

  readonly song = signal<SongResponse | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
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
}
