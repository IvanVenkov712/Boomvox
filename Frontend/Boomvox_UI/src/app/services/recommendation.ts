import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { RecommendationResponse } from '../models/recommendations';

@Injectable({
  providedIn: 'root',
})
export class RecommendationService {

  private readonly url = `${environment.apiUrl}/recommendations`;

  constructor(private http: HttpClient) {}

  generate(limit = 20): Observable<RecommendationResponse[]> {
    const params = new HttpParams().set('limit', limit);
    return this.http.post<RecommendationResponse[]>(`${this.url}/generate`, null, { params });
  }

  getRecommendations(limit = 20): Observable<RecommendationResponse[]> {
    const params = new HttpParams().set('limit', limit);
    return this.http.get<RecommendationResponse[]>(this.url, { params });
  }

  click(songId: number): Observable<RecommendationResponse> {
    return this.http.post<RecommendationResponse>(`${this.url}/${songId}/click`, null);
  }
}
