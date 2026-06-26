import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { PlaylistResponse, PlaylistRequest, PlaylistSongResponse, PlaylistSongRequest } from '../models/playlists';

@Injectable({
  providedIn: 'root',
})
export class PlaylistService {

  private readonly url = `${environment.apiUrl}/playlists`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<PlaylistResponse[]> {
    return this.http.get<PlaylistResponse[]>(this.url);
  }

  getById(playlistId: number): Observable<PlaylistResponse> {
    return this.http.get<PlaylistResponse>(`${this.url}/${playlistId}`);
  }

  create(req: PlaylistRequest): Observable<PlaylistResponse> {
    return this.http.post<PlaylistResponse>(this.url, req);
  }

  delete(playlistId: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${playlistId}`);
  }

  addSong(playlistId: number, req: PlaylistSongRequest): Observable<PlaylistSongResponse> {
    return this.http.post<PlaylistSongResponse>(`${this.url}/${playlistId}/songs`, req);
  }

  removeSong(playlistId: number, songId: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${playlistId}/songs/${songId}`);
  }
}
