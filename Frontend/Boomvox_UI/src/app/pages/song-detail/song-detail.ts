import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { SongService } from '../../services/song';
import { SongResponse } from '../../models/songs';
import { Rating } from '../../components/rating/rating';

@Component({
  selector: 'app-song-detail',
  standalone: true,
  imports: [CommonModule, Rating, RouterLink],
  templateUrl: './song-detail.html',
  styleUrl: './song-detail.css',
})
export class SongDetail implements OnInit {
  song: SongResponse | null = null;
  loading = true;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private songService: SongService,
  ) {}

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.songService.getById(id).subscribe({
      next: (song) => {
        this.song = song;
        this.loading = false;
      },
      error: () => {
        this.error = 'Song not found';
        this.loading = false;
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
