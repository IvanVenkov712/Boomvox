import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, catchError, finalize, of, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  UserResponse,
  UpdateUserRequest,
  ListeningHistoryResponse,
  UserPreferenceResponse,
  Page,
} from '../models/users';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/users`;

  private readonly _currentUser = signal<UserResponse | null>(null);
  readonly currentUser = this._currentUser.asReadonly();
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  loadMe(): void {
    this.loading.set(true);
    this.error.set(null);
    this.http
      .get<UserResponse>(`${this.url}/me`)
      .pipe(
        tap((res) => this._currentUser.set(res)),
        catchError(() => {
          this.error.set('Failed to load user.');
          return of(null);
        }),
        finalize(() => this.loading.set(false)),
      )
      .subscribe();
  }

  updateMe(req: UpdateUserRequest): Observable<UserResponse> {
    return this.http
      .put<UserResponse>(`${this.url}/me`, req)
      .pipe(tap((updated) => this._currentUser.set(updated)));
  }

  getById(userId: number): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.url}/${userId}`);
  }

  getHistory(page = 0, size = 20): Observable<Page<ListeningHistoryResponse>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', 'listenedAt,desc');
    return this.http.get<Page<ListeningHistoryResponse>>(`${this.url}/me/listening-history`, {
      params,
    });
  }

  getPreferences(): Observable<UserPreferenceResponse> {
    return this.http.get<UserPreferenceResponse>(`${this.url}/me/preferences`);
  }
}
