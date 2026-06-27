import { Component, inject, OnInit, signal } from '@angular/core';
import { LowerCasePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { SongService } from '../../services/song';
import { SongResponse, SongBrowseParams } from '../../models/songs';
import { Genre, SongFormat } from '../../models/enums';

@Component({
  selector: 'app-songs',
  standalone: true,
  imports: [FormsModule, LowerCasePipe],
  templateUrl: './songs.html',
  styleUrl: './songs.css',
})
export class Songs implements OnInit {
  private readonly songService = inject(SongService);
  private readonly router = inject(Router);

  readonly songs = signal<SongResponse[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  readonly genres: Genre[] = Object.values(Genre);
  readonly formats: SongFormat[] = Object.values(SongFormat);

  filters: SongBrowseParams = {
    search: '',
    genre: '',
    format: '',
  };

  ngOnInit() {
    this.search();
  }

  search() {
    this.loading.set(true);
    this.error.set(null);
    this.songService.browse(this.filters).subscribe({
      next: (songs) => {
        this.songs.set(songs);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Could not load songs');
        this.loading.set(false);
      },
    });
  }

  clearFilters() {
    this.filters = { search: '', genre: '', format: '' };
    this.search();
  }

  goToSong(id: number) {
    this.router.navigate(['/songs', id]);
  }

  formatDuration(seconds: number): string {
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${m}:${s.toString().padStart(2, '0')}`;
  }
}
