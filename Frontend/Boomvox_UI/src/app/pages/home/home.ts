import { Component, inject, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { RecommendationService } from '../../services/recommendation';
import { SongService } from '../../services/song';
import { PlayerService } from '../../services/player';
import { RecommendationResponse } from '../../models/recommendations';
import { SongResponse } from '../../models/songs';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home implements OnInit {
  private readonly recommendationService = inject(RecommendationService);
  private readonly songService = inject(SongService);
  readonly playerService = inject(PlayerService);
  private readonly router = inject(Router);

  readonly recommendations = signal<RecommendationResponse[]>([]);
  readonly songs = signal<Map<number, SongResponse>>(new Map());
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.recommendationService.getRecommendations().subscribe({
      next: (recs) => {
        if (recs.length === 0) {
          this.generate();
        } else {
          this.recommendations.set(recs);
          this.loadSongs(recs);
        }
      },
      error: () => {
        this.error.set('Could not load recommendations');
        this.loading.set(false);
      },
    });
  }

  private generate() {
    this.recommendationService.generate().subscribe({
      next: (recs) => {
        this.recommendations.set(recs);
        this.loadSongs(recs);
      },
      error: () => {
        this.error.set('Could not generate recommendations');
        this.loading.set(false);
      },
    });
  }

  private loadSongs(recs: RecommendationResponse[]) {
    if (recs.length === 0) {
      this.loading.set(false);
      return;
    }
    let loaded = 0;
    const map = new Map<number, SongResponse>();
    recs.forEach((rec) => {
      this.songService.fetchById(rec.songId).subscribe({
        next: (song) => {
          map.set(song.id, song);
          loaded++;
          if (loaded === recs.length) {
            this.songs.set(map);
            this.loading.set(false);
          }
        },
        error: () => {
          loaded++;
          if (loaded === recs.length) {
            this.songs.set(map);
            this.loading.set(false);
          }
        },
      });
    });
  }

  onClickRecommendation(rec: RecommendationResponse) {
    this.recommendationService.click(rec.songId).subscribe();
    const song = this.songs().get(rec.songId);
    if (song) {
      const allSongs = this.recommendations()
        .map((r) => this.songs().get(r.songId))
        .filter((s): s is SongResponse => !!s);
      this.playerService.play(song, allSongs);
    } else {
      this.router.navigate(['/songs', rec.songId]);
    }
  }

  goToSong(songId: number) {
    this.router.navigate(['/songs', songId]);
  }

  getSong(songId: number): SongResponse | undefined {
    return this.songs().get(songId);
  }

  formatPercent(percent: number): string {
    return `${percent}%`;
  }
}
