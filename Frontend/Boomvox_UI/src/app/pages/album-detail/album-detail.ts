import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AlbumService } from '../../services/album';
import { AlbumResponse } from '../../models/albums';
import { SongResponse } from '../../models/songs';

@Component({
  selector: 'app-album-detail',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './album-detail.html',
  styleUrl: './album-detail.css',
})
export class AlbumDetail implements OnInit {

  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly albumService = inject(AlbumService);

  readonly album = signal<AlbumResponse | null>(null);
  readonly songs = signal<SongResponse[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    forkJoin({
      album: this.albumService.fetchById(id),
      songs: this.albumService.getSongs(id),
    }).subscribe({
      next: ({ album, songs }) => {
        this.album.set(album);
        this.songs.set(songs);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Could not load album');
        this.loading.set(false);
      },
    });
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
