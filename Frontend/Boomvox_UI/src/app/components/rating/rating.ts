import { Component, signal, Input, OnChanges, SimpleChanges, ChangeDetectorRef, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RatingService } from '../../services/rating';
import { RatingResponse, SongStatsResponse } from '../../models/ratingsAndStats';

@Component({
  selector: 'app-rating',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './rating.html',
  styleUrl: './rating.css',
})
export class Rating implements OnInit, OnChanges {

  private readonly ratingService = inject(RatingService);
  private readonly cdr = inject(ChangeDetectorRef);

  @Input() songId!: number;

  myRating: RatingResponse | null = null;
  stats: SongStatsResponse | null = null;

  grade: number = 5;
  comment: string = '';

  loading = true;
  submitting = false;
  error: string | null = null;

  ngOnInit() {
    this.load();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['songId'] && !changes['songId'].firstChange) {
      this.myRating = null;
      this.stats = null;
      this.grade = 5;
      this.comment = '';
      this.load();
    }
  }

  load() {
    this.loading = true;
    this.error = null;
    this.myRating = null;

    this.ratingService.getStats(this.songId).subscribe({
      next: (stats) => {
        this.stats = stats;
        this.ratingService.getMine(this.songId).subscribe({
          next: (mine) => {
            this.myRating = mine;
            this.grade = mine.grade;
            this.comment = mine.comment;
            this.loading = false;
            this.cdr.markForCheck();
          },
          error: () => {
            // 403 = expired token or no rating yet
            // 404 = no rating yet
            // either way show the form, backend will reject if not allowed
            this.loading = false;
            this.cdr.markForCheck();
          },
        });
      },
      error: () => {
        this.error = 'Could not load rating';
        this.loading = false;
        this.cdr.markForCheck();
      },
    });
  }

  submit() {
    this.submitting = true;
    this.error = null;
    this.ratingService.submit(this.songId, {
      songId: this.songId,
      grade: this.grade,
      comment: this.comment,
    }).subscribe({
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