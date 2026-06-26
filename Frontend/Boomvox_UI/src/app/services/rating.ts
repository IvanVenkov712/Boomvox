import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { RatingRequest, RatingResponse, SongStatsResponse } from '../models/ratingsAndStats';

@Injectable({
  providedIn: 'root',
})
export class RatingService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/songs`;

  submit(songId: number, req: RatingRequest): Observable<RatingResponse> {
    return this.http.post<RatingResponse>(`${this.url}/${songId}/ratings`, req);
  }

  getMine(songId: number): Observable<RatingResponse> {
    return this.http.get<RatingResponse>(`${this.url}/${songId}/ratings/me`);
  }

  deleteMine(songId: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${songId}/ratings/me`);
  }

  getAll(songId: number): Observable<RatingResponse[]> {
    return this.http.get<RatingResponse[]>(`${this.url}/${songId}/ratings`);
  }

  getAverage(songId: number): Observable<number> {
    return this.http.get<number>(`${this.url}/${songId}/ratings/average`);
  }

  getStats(songId: number): Observable<SongStatsResponse> {
    return this.http.get<SongStatsResponse>(`${this.url}/${songId}/stats`);
  }

  getMyRatings(): Observable<RatingResponse[]> {
    return this.http.get<RatingResponse[]>(`${environment.apiUrl}/users/me/ratings`);
  }
}
