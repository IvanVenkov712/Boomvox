import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, catchError, of, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { SongResponse, SongRequest, SongBrowseParams } from '../models/songs';
import { SongTagRequest, TagResponse } from '../models/tags';

@Injectable({
  providedIn: 'root',
})
export class SongService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/songs`;

  private readonly _songs = signal<SongResponse[]>([]);
  readonly songs = this._songs.asReadonly();
  readonly error = signal<string | null>(null);

  getById(songId: number): SongResponse | undefined {
    return this._songs().find((s) => s.id === songId);
  }

  browse(filters: SongBrowseParams = {}): Observable<SongResponse[]> {
    let params = new HttpParams();
    Object.entries(filters).forEach(([key, val]) => {
      if (val !== undefined && val !== null && val !== '') {
        params = params.set(key, String(val));
      }
    });
    return this.http
      .get<SongResponse[]>(this.url, { params })
      .pipe(tap((res) => this._songs.set(res)));
  }

  upload(
    audio: File,
    metadata: { name: string; genre: string; albumId: number | null },
  ): Observable<SongResponse> {
    const form = new FormData();
    form.append('audio', audio);
    form.append('metadata', new Blob([JSON.stringify(metadata)], { type: 'application/json' }));
    return this.http
      .post<SongResponse>(this.url, form)
      .pipe(tap((created) => this._songs.update((list) => [...list, created])));
  }

  update(songId: number, req: SongRequest): Observable<SongResponse> {
    return this.http
      .put<SongResponse>(`${this.url}/${songId}`, req)
      .pipe(
        tap((updated) =>
          this._songs.update((list) => list.map((s) => (s.id === songId ? updated : s))),
        ),
      );
  }

  delete(songId: number): void {
    this.http
      .delete<void>(`${this.url}/${songId}`)
      .pipe(
        tap(() => this._songs.update((list) => list.filter((s) => s.id !== songId))),
        catchError(() => {
          this.error.set('Failed to delete song.');
          return of(null);
        }),
      )
      .subscribe();
  }

  addTag(songId: number, tagId: number): Observable<TagResponse> {
    const body: SongTagRequest = { songId, tagId };
    return this.http.post<TagResponse>(`${this.url}/${songId}/tags`, body);
  }

  removeTag(songId: number, tagId: number): void {
    this.http
      .delete<void>(`${this.url}/${songId}/tags/${tagId}`)
      .pipe(
        catchError(() => {
          this.error.set('Failed to remove tag from song.');
          return of(null);
        }),
      )
      .subscribe();
  }

  getTags(songId: number): Observable<TagResponse[]> {
    return this.http.get<TagResponse[]>(`${this.url}/${songId}/tags`);
  }

  fetchById(id: number): Observable<SongResponse> {
    return this.http.get<SongResponse>(`${this.url}/${id}`);
  }
}
