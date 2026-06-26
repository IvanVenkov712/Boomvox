import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { TagResponse } from '../models/tags';

@Injectable({
  providedIn: 'root',
})
export class Tag {

  private readonly url = `${environment.apiUrl}/tags`;

  constructor(private http: HttpClient) {}

  search(query: string): Observable<TagResponse[]> {
    const params = new HttpParams().set('search', query);
    return this.http.get<TagResponse[]>(this.url, { params });
  }

  create(name: string): Observable<TagResponse> {
    return this.http.post<TagResponse>(this.url, { name });
  }
}
