import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { PlaylistService } from '../../services/playlist';
import { PlaylistResponse, PlaylistSongResponse } from '../../models/playlists';

@Component({
  selector: 'app-playlist-detail',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './playlist-detail.html',
  styleUrl: './playlist-detail.css',
})
export class PlaylistDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly playlistService = inject(PlaylistService);

  readonly playlist = signal<PlaylistResponse | null>(null);
  readonly songs = signal<PlaylistSongResponse[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly deleting = signal(false);

  private playlistId!: number;

  ngOnInit() {
    this.playlistId = Number(this.route.snapshot.paramMap.get('id'));
    this.load();
  }

  load() {
    this.loading.set(true);
    this.playlistService.getById(this.playlistId).subscribe({
      next: (playlist) => {
        this.playlist.set(playlist);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Could not load playlist');
        this.loading.set(false);
      },
    });
    this.playlistService.getSongs(this.playlistId).subscribe({
      next: (songs) => this.songs.set(songs),
    });
  }

  removeSong(songId: number) {
    this.playlistService.removeSong(this.playlistId, songId).subscribe({
      next: () => this.songs.update((list) => list.filter((s) => s.songId !== songId)),
      error: () => this.error.set('Could not remove song'),
    });
  }

  deletePlaylist() {
    this.deleting.set(true);
    this.playlistService.delete(this.playlistId).subscribe({
      next: () => this.router.navigate(['/playlists']),
      error: () => {
        this.error.set('Could not delete playlist');
        this.deleting.set(false);
      },
    });
  }

  goToSong(songId: number) {
    this.router.navigate(['/songs', songId]);
  }
}
