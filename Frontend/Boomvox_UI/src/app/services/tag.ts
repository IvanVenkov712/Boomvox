import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, catchError, finalize, of, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { TagResponse } from '../models/tags';

@Injectable({
  providedIn: 'root',
})
export class TagService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/tags`;

  private readonly _tags = signal<TagResponse[]>([]);
  readonly tags = this._tags.asReadonly();
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  loadAll(): void {
    this.loading.set(true);
    this.error.set(null);
    this.http
      .get<TagResponse[]>(this.url)
      .pipe(
        tap((res) => this._tags.set(res)),
        catchError(() => {
          this.error.set('Failed to load tags.');
          return of([]);
        }),
        finalize(() => this.loading.set(false)),
      )
      .subscribe();
  }

  search(query: string): Observable<TagResponse[]> {
    const params = new HttpParams().set('search', query);
    return this.http.get<TagResponse[]>(this.url, { params });
  }

  create(name: string): Observable<TagResponse> {
    return this.http
      .post<TagResponse>(this.url, { name })
      .pipe(tap((created) => this._tags.update((list) => [...list, created])));
  }
}
