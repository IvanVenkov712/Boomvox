import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthResponse, LoginRequest, RegisterRequest } from '../models/auth';
import { UserRole } from '../models/enums';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly url = `${environment.apiUrl}/auth`;

  login(req: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.url}/login`, req)
      .pipe(tap((res) => this.storeSession(res)));
  }

  register(req: RegisterRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.url}/register`, req)
      .pipe(tap((res) => this.storeSession(res)));
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('email');
    localStorage.removeItem('role');
    this.router.navigate(['/login']);
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  getRole(): UserRole | null {
    return localStorage.getItem('role') as UserRole | null;
  }

  getEmail(): string | null {
    return localStorage.getItem('email');
  }

  private storeSession(res: AuthResponse): void {
    localStorage.setItem('token', res.token);
    localStorage.setItem('email', res.email);
    localStorage.setItem('role', res.role);
  }
}
