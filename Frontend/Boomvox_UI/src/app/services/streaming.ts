import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { StreamUrlResponse, StreamingEventRequest, SessionStatusRequest } from '../models/streaming';

@Injectable({
  providedIn: 'root',
})
export class StreamingService {

  private readonly url = `${environment.apiUrl}/streaming`;

  constructor(private http: HttpClient) {}

  getStreamUrl(songId: number): Observable<StreamUrlResponse> {
    return this.http.get<StreamUrlResponse>(`${this.url}/songs/${songId}`);
  }

  closeSession(sessionId: number, req: SessionStatusRequest): Observable<void> {
    return this.http.patch<void>(`${this.url}/sessions/${sessionId}`, req);
  }

  logEvent(sessionId: number, req: StreamingEventRequest): Observable<void> {
    return this.http.post<void>(`${this.url}/events`, { sessionId, ...req });
  }
}
