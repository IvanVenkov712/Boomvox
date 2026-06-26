import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { SongResponse, SongRequest } from '../models/songs';
import { SongTagRequest, TagResponse } from '../models/tags';

export interface SongBrowseParams {
  search?: string;
  genre?: string;
  format?: string;
  albumId?: number;
  artistId?: number;
  tagId?: number;
}

@Injectable({
  providedIn: 'root',
})
export class Song {
  private readonly url = `${environment.apiUrl}/songs`;

  constructor(private http: HttpClient) {}

  browse(filters: SongBrowseParams = {}): Observable<SongResponse[]> {
    let params = new HttpParams();
    Object.entries(filters).forEach(([key, val]) => {
      if (val !== undefined && val !== null && val !== '') {
        params = params.set(key, String(val));
      }
    });
    return this.http.get<SongResponse[]>(this.url, { params });
  }

  getById(songId: number): Observable<SongResponse> {
    return this.http.get<SongResponse>(`${this.url}/${songId}`);
  }

  upload(
    audio: File,
    metadata: { name: string; genre: string; albumId: number | null },
  ): Observable<SongResponse> {
    const form = new FormData();
    form.append('audio', audio);
    form.append('metadata', new Blob([JSON.stringify(metadata)], { type: 'application/json' }));
    return this.http.post<SongResponse>(this.url, form);
  }

  update(songId: number, req: SongRequest): Observable<SongResponse> {
    return this.http.put<SongResponse>(`${this.url}/${songId}`, req);
  }

  delete(songId: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${songId}`);
  }

  addTag(songId: number, tagId: number): Observable<TagResponse> {
    const body: SongTagRequest = { songId, tagId };
    return this.http.post<TagResponse>(`${this.url}/${songId}/tags`, body);
  }

  removeTag(songId: number, tagId: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${songId}/tags/${tagId}`);
  }

  getTags(songId: number): Observable<TagResponse[]> {
    return this.http.get<TagResponse[]>(`${this.url}/${songId}/tags`);
  }
}
