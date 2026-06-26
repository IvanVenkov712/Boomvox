import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { UserResponse } from '../models/users';
import { UserRole } from '../models/enums';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/admin`;

  getUsers(search?: string, role?: UserRole): Observable<UserResponse[]> {
    let params = new HttpParams();
    if (search) params = params.set('search', search);
    if (role) params = params.set('role', role);
    return this.http.get<UserResponse[]>(`${this.url}/users`, { params });
  }

  promoteUser(userId: number, role: UserRole): Observable<UserResponse> {
    return this.http.patch<UserResponse>(`${this.url}/users/${userId}/role`, { role });
  }
}
