import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, finalize, of, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  PlaylistResponse,
  PlaylistRequest,
  PlaylistSongResponse,
  PlaylistSongRequest,
} from '../models/playlists';

@Injectable({
  providedIn: 'root',
})
export class PlaylistService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/playlists`;

  private readonly _playlists = signal<PlaylistResponse[]>([]);
  readonly playlists = this._playlists.asReadonly();
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  loadAll(): void {
    this.loading.set(true);
    this.error.set(null);
    this.http
      .get<PlaylistResponse[]>(this.url)
      .pipe(
        tap((res) => this._playlists.set(res)),
        catchError(() => {
          this.error.set('Failed to load playlists.');
          return of([]);
        }),
        finalize(() => this.loading.set(false)),
      )
      .subscribe();
  }

  create(req: PlaylistRequest): Observable<PlaylistResponse> {
    return this.http
      .post<PlaylistResponse>(this.url, req)
      .pipe(tap((created) => this._playlists.update((list) => [...list, created])));
  }

  addSong(playlistId: number, req: PlaylistSongRequest): Observable<PlaylistSongResponse> {
    return this.http.post<PlaylistSongResponse>(`${this.url}/${playlistId}/songs`, req);
  }

  getAll(): Observable<PlaylistResponse[]> {
    return this.http.get<PlaylistResponse[]>(this.url);
  }

  getById(playlistId: number): Observable<PlaylistResponse> {
    return this.http.get<PlaylistResponse>(`${this.url}/${playlistId}`);
  }

  getSongs(playlistId: number): Observable<PlaylistSongResponse[]> {
    return this.http.get<PlaylistSongResponse[]>(`${this.url}/${playlistId}/songs`);
  }

  removeSong(playlistId: number, songId: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${playlistId}/songs/${songId}`);
  }

  delete(playlistId: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${playlistId}`);
  }
}
