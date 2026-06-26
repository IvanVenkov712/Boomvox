import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, finalize, of, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { UserResponse } from '../models/users';
import { SongResponse } from '../models/songs';
import { AlbumResponse } from '../models/albums';

@Injectable({
  providedIn: 'root',
})
export class ArtistService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/artists`;

  private readonly _artists = signal<UserResponse[]>([]);
  readonly artists = this._artists.asReadonly();
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  loadAll(): void {
    this.loading.set(true);
    this.error.set(null);
    this.http
      .get<UserResponse[]>(this.url)
      .pipe(
        tap((res) => this._artists.set(res)),
        catchError(() => {
          this.error.set('Failed to load artists.');
          return of([]);
        }),
        finalize(() => this.loading.set(false)),
      )
      .subscribe();
  }

  getById(artistId: number): UserResponse | undefined {
    return this._artists().find((a) => a.id === artistId);
  }

  getSongs(artistId: number): Observable<SongResponse[]> {
    return this.http.get<SongResponse[]>(`${this.url}/${artistId}/songs`);
  }

  getAlbums(artistId: number): Observable<AlbumResponse[]> {
    return this.http.get<AlbumResponse[]>(`${this.url}/${artistId}/albums`);
  }
}
