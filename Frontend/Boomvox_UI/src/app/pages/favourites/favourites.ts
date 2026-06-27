import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router } from '@angular/router';
import { FavouritesService } from '../../services/favourites';
import { SongService } from '../../services/song';
import { PlayerService } from '../../services/player';
import { FavouritesListSongResponse } from '../../models/favourites';

@Component({
  selector: 'app-favourites',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './favourites.html',
  styleUrl: './favourites.css',
})
export class Favourites implements OnInit {
  private readonly favouritesService = inject(FavouritesService);
  private readonly songService = inject(SongService);
  readonly playerService = inject(PlayerService);
  private readonly router = inject(Router);

  readonly songs = signal<FavouritesListSongResponse[]>([]);
  readonly songNames = signal<Map<number, string>>(new Map());
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  ngOnInit() {
    this.load();
  }

  load() {
    this.favouritesService.getSongs().subscribe({
      next: (songs) => {
        this.songs.set(songs);
        this.loading.set(false);
        if (songs.length === 0) return;
        const map = new Map<number, string>();
        let done = 0;
        songs.forEach(entry => {
          this.songService.fetchById(entry.songId).subscribe({
            next: (song) => {
              map.set(song.id, song.name);
              if (++done === songs.length) this.songNames.set(new Map(map));
            },
            error: () => {
              if (++done === songs.length) this.songNames.set(new Map(map));
            }
          });
        });
      },
      error: () => {
        this.error.set('Could not load favourites');
        this.loading.set(false);
      },
    });
  }

  remove(songId: number) {
    this.favouritesService.removeSong(songId).subscribe({
      next: () => {
        this.songs.update((list) => list.filter((s) => s.songId !== songId));
        this.songNames.update(map => { const m = new Map(map); m.delete(songId); return m; });
      },
      error: () => this.error.set('Could not remove song'),
    });
  }

  playSong(songId: number) {
    this.songService.fetchById(songId).subscribe({
      next: (song) => this.playerService.play(song),
    });
  }

  goToSong(songId: number) {
    this.router.navigate(['/songs', songId]);
  }
}