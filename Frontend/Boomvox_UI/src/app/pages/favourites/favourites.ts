import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router } from '@angular/router';
import { FavouritesService } from '../../services/favourites';
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
  private readonly router = inject(Router);

  readonly songs = signal<FavouritesListSongResponse[]>([]);
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
      },
      error: () => {
        this.error.set('Could not load favourites');
        this.loading.set(false);
      },
    });
  }

  remove(songId: number) {
    this.favouritesService.removeSong(songId).subscribe({
      next: () => this.songs.update((list) => list.filter((s) => s.songId !== songId)),
      error: () => this.error.set('Could not remove song'),
    });
  }

  goToSong(songId: number) {
    this.router.navigate(['/songs', songId]);
  }
}
