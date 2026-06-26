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

  getById(playlistId: number): PlaylistResponse | undefined {
    return this._playlists().find((p) => p.id === playlistId);
  }

  create(req: PlaylistRequest): Observable<PlaylistResponse> {
    return this.http
      .post<PlaylistResponse>(this.url, req)
      .pipe(tap((created) => this._playlists.update((list) => [...list, created])));
  }

  delete(playlistId: number): void {
    this.http
      .delete<void>(`${this.url}/${playlistId}`)
      .pipe(
        tap(() => this._playlists.update((list) => list.filter((p) => p.id !== playlistId))),
        catchError(() => {
          this.error.set('Failed to delete playlist.');
          return of(null);
        }),
      )
      .subscribe();
  }

  addSong(playlistId: number, req: PlaylistSongRequest): Observable<PlaylistSongResponse> {
    return this.http.post<PlaylistSongResponse>(`${this.url}/${playlistId}/songs`, req);
  }

  removeSong(playlistId: number, songId: number): void {
    this.http
      .delete<void>(`${this.url}/${playlistId}/songs/${songId}`)
      .pipe(
        catchError(() => {
          this.error.set('Failed to remove song from playlist.');
          return of(null);
        }),
      )
      .subscribe();
  }
}
