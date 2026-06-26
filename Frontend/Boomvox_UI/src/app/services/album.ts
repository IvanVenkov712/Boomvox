import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AlbumResponse, AlbumRequest } from '../models/albums';
import { SongResponse } from '../models/songs';

@Injectable({
  providedIn: 'root',
})
export class AlbumService {

  private readonly url = `${environment.apiUrl}/albums`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<AlbumResponse[]> {
    return this.http.get<AlbumResponse[]>(this.url);
  }

  getById(albumId: number): Observable<AlbumResponse> {
    return this.http.get<AlbumResponse>(`${this.url}/${albumId}`);
  }

  getSongs(albumId: number): Observable<SongResponse[]> {
    return this.http.get<SongResponse[]>(`${this.url}/${albumId}/songs`);
  }

  create(req: AlbumRequest): Observable<AlbumResponse> {
    return this.http.post<AlbumResponse>(this.url, req);
  }

  update(albumId: number, req: AlbumRequest): Observable<AlbumResponse> {
    return this.http.put<AlbumResponse>(`${this.url}/${albumId}`, req);
  }
}
