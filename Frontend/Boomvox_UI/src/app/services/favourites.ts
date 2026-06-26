import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { FavouritesListResponse, FavouritesListSongResponse, FavouritesListSongRequest } from '../models/favourites';

@Injectable({
  providedIn: 'root',
})
export class FavouritesService {

  private readonly url = `${environment.apiUrl}/favourites`;

  constructor(private http: HttpClient) {}

  getList(): Observable<FavouritesListResponse> {
    return this.http.get<FavouritesListResponse>(this.url);
  }

  addSong(songId: number): Observable<FavouritesListSongResponse> {
    const body: FavouritesListSongRequest = { songId };
    return this.http.post<FavouritesListSongResponse>(`${this.url}/songs`, body);
  }

  removeSong(songId: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/songs/${songId}`);
  }
}
