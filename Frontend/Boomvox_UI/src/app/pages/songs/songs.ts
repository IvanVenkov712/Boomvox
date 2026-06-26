import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { SongService, SongBrowseParams } from '../../services/song';
import { SongResponse } from '../../models/songs';
import { Genre, SongFormat} from '../../models/enums';

@Component({
  selector: 'app-songs',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './songs.html',
  styleUrl: './songs.css',
})
export class Songs implements OnInit {

  songs: SongResponse[] = [];
  loading = true;
  error: string | null = null;

  filters: SongBrowseParams = {
    search: '',
    genre: '',
    format: '',
  };

  genres: Genre[] = ['METAL' , 'POP_FOLK' , 'POP' , 'ROCK' , 'COUNTRY' , 'TECHNO'];

  formats: SongFormat[] = ['MP3', 'WAV', 'AAC'];

  constructor(
    private songService: SongService,
    private router: Router,
  ) {}

  ngOnInit() {
    this.search();
  }

  search() {
    this.loading = true;
    this.error = null;
    this.songService.browse(this.filters).subscribe({
      next: (songs) => {
        this.songs = songs;
        this.loading = false;
      },
      error: () => {
        this.error = 'Could not load songs';
        this.loading = false;
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
