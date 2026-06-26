import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ArtistService } from '../../services/artist';

@Component({
  selector: 'app-artists',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './artists.html',
  styleUrl: './artists.css',
})
export class Artists implements OnInit {
  private readonly artistService = inject(ArtistService);
  private readonly router = inject(Router);

  readonly artists = this.artistService.artists;
  readonly loading = this.artistService.loading;
  readonly error = this.artistService.error;

  ngOnInit(): void {
    this.artistService.loadAll();
  }

  goToArtist(id: number): void {
    this.router.navigate(['/artists', id]);
  }
}
