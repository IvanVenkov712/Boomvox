import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { PlaylistService } from '../../services/playlist';
import { PlaylistResponse } from '../../models/playlists';

@Component({
  selector: 'app-playlists',
  standalone: true,
  imports: [FormsModule, DatePipe],
  templateUrl: './playlists.html',
  styleUrl: './playlists.css',
})
export class Playlists implements OnInit {
  private readonly playlistService = inject(PlaylistService);
  private readonly router = inject(Router);

  readonly playlists = signal<PlaylistResponse[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly creating = signal(false);

  newName = '';
  showForm = false;

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.playlistService.getAll().subscribe({
      next: (playlists) => {
        this.playlists.set(playlists);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Could not load playlists');
        this.loading.set(false);
      },
    });
  }

  create() {
    if (!this.newName.trim()) return;
    this.creating.set(true);
    this.playlistService.create({ name: this.newName.trim() }).subscribe({
      next: (playlist) => {
        this.playlists.update((list) => [...list, playlist]);
        this.newName = '';
        this.showForm = false;
        this.creating.set(false);
      },
      error: () => {
        this.error.set('Could not create playlist');
        this.creating.set(false);
      },
    });
  }

  goToPlaylist(id: number) {
    this.router.navigate(['/playlists', id]);
  }
}
