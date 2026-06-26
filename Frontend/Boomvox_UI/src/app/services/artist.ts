import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { UserResponse } from '../models/users';
import { SongResponse } from '../models/songs';
import { AlbumResponse } from '../models/albums';

@Injectable({
  providedIn: 'root',
})
export class ArtistService {

  private readonly url = `${environment.apiUrl}/artists`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<UserResponse[]> {
    return this.http.get<UserResponse[]>(this.url);
  }

  getById(artistId: number): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.url}/${artistId}`);
  }

  getSongs(artistId: number): Observable<SongResponse[]> {
    return this.http.get<SongResponse[]>(`${this.url}/${artistId}/songs`);
  }

  getAlbums(artistId: number): Observable<AlbumResponse[]> {
    return this.http.get<AlbumResponse[]>(`${this.url}/${artistId}/albums`);
  }
}
