import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, catchError, finalize, of, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { AlbumResponse, AlbumRequest } from '../models/albums';
import { SongResponse } from '../models/songs';

@Injectable({
  providedIn: 'root',
})
export class AlbumService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/albums`;

  private readonly _albums = signal<AlbumResponse[]>([]);
  readonly albums = this._albums.asReadonly();
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  loadAll(): void {
    this.loading.set(true);
    this.error.set(null);
    this.http
      .get<AlbumResponse[]>(this.url)
      .pipe(
        tap((res) => this._albums.set(res)),
        catchError(() => {
          this.error.set('Failed to load albums.');
          return of([]);
        }),
        finalize(() => this.loading.set(false)),
      )
      .subscribe();
  }

  getById(albumId: number): AlbumResponse | undefined {
    return this._albums().find((a) => a.id === albumId);
  }

  getSongs(albumId: number): Observable<SongResponse[]> {
    return this.http.get<SongResponse[]>(`${this.url}/${albumId}/songs`);
  }

  create(req: AlbumRequest): Observable<AlbumResponse> {
    return this.http
      .post<AlbumResponse>(this.url, req)
      .pipe(tap((created) => this._albums.update((list) => [...list, created])));
  }

  update(albumId: number, req: AlbumRequest): Observable<AlbumResponse> {
    return this.http
      .put<AlbumResponse>(`${this.url}/${albumId}`, req)
      .pipe(
        tap((updated) =>
          this._albums.update((list) => list.map((a) => (a.id === albumId ? updated : a))),
        ),
      );
  }

  delete(albumId: number): void {
    this.http
      .delete<void>(`${this.url}/${albumId}`)
      .pipe(
        tap(() => this._albums.update((list) => list.filter((a) => a.id !== albumId))),
        catchError(() => {
          this.error.set('Failed to delete album.');
          return of(null);
        }),
      )
      .subscribe();
  }

  fetchById(id: number): Observable<AlbumResponse> {
    return this.http.get<AlbumResponse>(`${this.url}/${id}`);
  }

  browse(filters: { search?: string; artistId?: number; genre?: string } = {}): Observable<AlbumResponse[]> {
    let params = new HttpParams();
    Object.entries(filters).forEach(([key, val]) => {
      if (val !== undefined && val !== null && val !== '') {
        params = params.set(key, String(val));
      }
    });
    return this.http
      .get<AlbumResponse[]>(this.url, { params })
      .pipe(tap((res) => this._albums.set(res)));
  }
}
