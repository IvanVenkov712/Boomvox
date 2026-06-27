import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, finalize, of, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  FavouritesListResponse,
  FavouritesListSongResponse,
  FavouritesListSongRequest,
} from '../models/favourites';

@Injectable({
  providedIn: 'root',
})
export class FavouritesService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/favourites`;

  private readonly _favourites = signal<FavouritesListResponse | null>(null);
  readonly favourites = this._favourites.asReadonly();
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.http
      .get<FavouritesListResponse>(this.url)
      .pipe(
        tap((res) => this._favourites.set(res)),
        catchError(() => {
          this.error.set('Failed to load favourites.');
          return of(null);
        }),
        finalize(() => this.loading.set(false)),
      )
      .subscribe();
  }

  addSong(songId: number): Observable<FavouritesListSongResponse> {
    const body: FavouritesListSongRequest = { songId };
    return this.http.post<FavouritesListSongResponse>(`${this.url}/songs`, body);
  }

  getSongs(): Observable<FavouritesListSongResponse[]> {
    return this.http.get<FavouritesListSongResponse[]>(this.url);
  }

  removeSong(songId: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/songs/${songId}`);
  }
}
