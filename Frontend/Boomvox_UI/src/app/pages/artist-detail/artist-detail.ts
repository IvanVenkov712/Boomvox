import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { ArtistService } from '../../services/artist';
import { UserResponse } from '../../models/users';
import { SongResponse } from '../../models/songs';
import { AlbumResponse } from '../../models/albums';

@Component({
  selector: 'app-artist-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './artist-detail.html',
  styleUrl: './artist-detail.css',
})
export class ArtistDetail implements OnInit {
  artist: UserResponse | null = null;
  songs: SongResponse[] = [];
  albums: AlbumResponse[] = [];
  loading = true;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private artistService: ArtistService,
  ) {}

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    forkJoin({
      artist: this.artistService.getById(id),
      songs: this.artistService.getSongs(id),
      albums: this.artistService.getAlbums(id),
    }).subscribe({
      next: ({ artist, songs, albums }) => {
        this.artist = artist;
        this.songs = songs;
        this.albums = albums;
        this.loading = false;
      },
      error: () => {
        this.error = 'Could not load artist';
        this.loading = false;
      },
    });
  }

  goToSong(id: number) {
    this.router.navigate(['/songs', id]);
  }
  goToAlbum(id: number) {
    this.router.navigate(['/albums', id]);
  }

  formatDuration(seconds: number): string {
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${m}:${s.toString().padStart(2, '0')}`;
  }
}
