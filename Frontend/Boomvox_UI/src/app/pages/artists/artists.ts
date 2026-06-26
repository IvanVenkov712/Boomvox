import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ArtistService } from '../../services/artist';
import { UserResponse } from '../../models/users';

@Component({
  selector: 'app-artists',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './artists.html',
  styleUrl: './artists.css',
})
export class Artists implements OnInit {
  artists: UserResponse[] = [];
  loading = true;
  error: string | null = null;

  constructor(
    private artistService: ArtistService,
    private router: Router,
  ) {}

  ngOnInit() {
    this.artistService.getAll().subscribe({
      next: (artists) => {
        this.artists = artists;
        this.loading = false;
      },
      error: () => {
        this.error = 'Could not load artists';
        this.loading = false;
      },
    });
  }

  goToArtist(id: number) {
    this.router.navigate(['/artists', id]);
  }
}
