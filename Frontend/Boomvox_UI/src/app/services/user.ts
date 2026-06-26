import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { UserResponse, UpdateUserRequest, ListeningHistoryResponse, UserPreferenceResponse, Page} from '../models/users';

@Injectable({
  providedIn: 'root',
})
export class User {

  private readonly url = `${environment.apiUrl}/users`;

  constructor(private http: HttpClient) {}

  getMe(): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.url}/me`);
  }

  updateMe(req: UpdateUserRequest): Observable<UserResponse> {
    return this.http.put<UserResponse>(`${this.url}/me`, req);
  }

  getById(userId: number): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.url}/${userId}`);
  }

  getHistory(page = 0, size = 20): Observable<Page<ListeningHistoryResponse>> {
    const params = new HttpParams().set('page', page).set('size', size).set('sort', 'listenedAt,desc');
    return this.http.get<Page<ListeningHistoryResponse>>(`${this.url}/me/listening-history`, { params });
  }

  getPreferences(): Observable<UserPreferenceResponse> {
    return this.http.get<UserPreferenceResponse>(`${this.url}/me/preferences`);
  }
}
