import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { PlaylistService } from '../../services/playlist';
import { SongService } from '../../services/song';
import { PlayerService } from '../../services/player';
import { PlaylistResponse, PlaylistSongResponse, PlaylistSongRequest } from '../../models/playlists';
import { SongResponse } from '../../models/songs';

@Component({
  selector: 'app-playlist-detail',
  standalone: true,
  imports: [DatePipe, FormsModule],
  templateUrl: './playlist-detail.html',
  styleUrl: './playlist-detail.css',
})
export class PlaylistDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly playlistService = inject(PlaylistService);
  private readonly songService = inject(SongService);
  private readonly playerService = inject(PlayerService);

  readonly playlist = signal<PlaylistResponse | null>(null);
  readonly songs = signal<PlaylistSongResponse[]>([]);
  readonly songNames = signal<Map<number, string>>(new Map());
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly deleting = signal(false);

  readonly songSearch = signal('');
  readonly songResults = signal<SongResponse[]>([]);
  readonly showSongDropdown = signal(false);
  readonly addingIds = signal<Set<number>>(new Set());

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
    this.loadSongs();
  }

  private loadSongs() {
    this.playlistService.getSongs(this.playlistId).subscribe({
      next: (songs) => {
        this.songs.set(songs);
        if (songs.length === 0) return;

        const map = new Map<number, string>();
        let completed = 0;

        songs.forEach(entry => {
          this.songService.fetchById(entry.songId).subscribe({
            next: (song) => {
              map.set(song.id, song.name);
              completed++;
              if (completed === songs.length) {
                this.songNames.set(new Map(map));
              }
            },
            error: () => {
              completed++;
              if (completed === songs.length) {
                this.songNames.set(new Map(map));
              }
            }
          });
        });
      },
    });
  }

  searchSongs() {
    const q = this.songSearch().trim();
    if (!q) {
      this.songResults.set([]);
      this.showSongDropdown.set(false);
      return;
    }
    const existing = new Set(this.songs().map((s) => s.songId));
    this.songService.browse({ search: q }).subscribe({
      next: (results) => {
        const filtered = results.filter((s) => !existing.has(s.id));
        this.songResults.set(filtered);
        this.showSongDropdown.set(filtered.length > 0);
      },
      error: () => this.songResults.set([]),
    });
  }

  addSong(song: SongResponse) {
    const ids = new Set(this.addingIds());
    ids.add(song.id);
    this.addingIds.set(ids);

    const req: PlaylistSongRequest = {
      songId: song.id,
      position: this.songs().length + 1,
    };

    this.playlistService.addSong(this.playlistId, req).subscribe({
      next: () => {
        this.loadSongs();
        this.songResults.update(list => list.filter(s => s.id !== song.id));
        if (this.songResults().length === 0) this.showSongDropdown.set(false);
        const ids = new Set(this.addingIds());
        ids.delete(song.id);
        this.addingIds.set(ids);
      },
      error: (err) => {
        this.error.set(err.error?.message ?? 'Could not add song');
        const ids = new Set(this.addingIds());
        ids.delete(song.id);
        this.addingIds.set(ids);
      },
    });
  }

  removeSong(songId: number) {
    this.playlistService.removeSong(this.playlistId, songId).subscribe({
      next: () => {
        this.songs.update(list => list.filter(s => s.songId !== songId));
        this.songNames.update(map => {
          const m = new Map(map);
          m.delete(songId);
          return m;
        });
      },
      error: () => this.error.set('Could not remove song'),
    });
  }

  playSong(songId: number) {
    const allIds = [...this.songs()]
      .sort((a, b) => a.position - b.position)
      .map(s => s.songId);

    forkJoin(allIds.map(id => this.songService.fetchById(id))).subscribe({
      next: (queue) => {
        const song = queue.find(s => s.id === songId);
        if (song) this.playerService.play(song, queue);
      }
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

  isAdding(songId: number): boolean {
    return this.addingIds().has(songId);
  }
}