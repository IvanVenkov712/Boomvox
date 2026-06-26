import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { RatingService } from '../../services/rating';
import { RatingResponse, SongStatsResponse } from '../../models/ratingsAndStats';


@Component({
  selector: 'app-rating',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './rating.html',
  styleUrl: './rating.css',
})
export class Rating {

  @Input() songId!: number;

  myRating: RatingResponse | null = null;
  stats: SongStatsResponse | null = null;

  grade: number = 5;
  comment: string = '';

  loading = true;
  submitting = false;
  error: string | null = null;

  constructor(private ratingService: RatingService) {}

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading = true;
    this.error = null;

    forkJoin({
      mine: this.ratingService.getMine(this.songId),
      stats: this.ratingService.getStats(this.songId),
    }).subscribe({
      next: ({ mine, stats }) => {
        this.myRating = mine;
        this.stats = stats;
        this.grade = mine.grade;
        this.comment = mine.comment;
        this.loading = false;
      },
      error: (err) => {
        // 404 on ratings/me just means not yet rated — still load stats
        if (err.status === 404) {
          this.ratingService.getStats(this.songId).subscribe({
            next: (stats) => {
              this.stats = stats;
              this.loading = false;
            },
            error: () => {
              this.error = 'Could not load stats';
              this.loading = false;
            },
          });
        } else {
          this.error = 'Could not load rating';
          this.loading = false;
        }
      },
    });
  }

  submit() {
    this.submitting = true;
    this.error = null;
    this.ratingService
      .submit(this.songId, {
        songId: this.songId,
        grade: this.grade,
        comment: this.comment,
      })
      .subscribe({
        next: (rating) => {
          this.myRating = rating;
          this.submitting = false;
          this.ratingService.getStats(this.songId).subscribe((s) => (this.stats = s));
        },
        error: (err) => {
          this.error = err.error?.message ?? 'Could not submit rating';
          this.submitting = false;
        },
      });
  }

  deleteRating() {
    this.submitting = true;
    this.ratingService.deleteMine(this.songId).subscribe({
      next: () => {
        this.myRating = null;
        this.grade = 5;
        this.comment = '';
        this.submitting = false;
        this.ratingService.getStats(this.songId).subscribe((s) => (this.stats = s));
      },
      error: () => {
        this.error = 'Could not delete rating';
        this.submitting = false;
      },
    });
  }
}
