import { inject, Injectable, signal } from '@angular/core';
import { StreamingService } from './streaming';
import { SongResponse } from '../models/songs';
import { SessionStatus, StreamingEventType } from '../models/enums';

@Injectable({ providedIn: 'root' })
export class PlayerService {
  private readonly streamingService = inject(StreamingService);

  private audio = new Audio();
  private sessionId: number | null = null;
  private expiresAt: string | null = null;

  readonly currentSong = signal<SongResponse | null>(null);
  readonly playing = signal(false);
  readonly currentTime = signal(0);
  readonly duration = signal(0);

  private queue: SongResponse[] = [];
  private queueIndex = 0;

  constructor() {
    this.audio.addEventListener('timeupdate', () => {
      this.currentTime.set(this.audio.currentTime);
    });

    this.audio.addEventListener('loadedmetadata', () => {
      this.duration.set(this.audio.duration);
    });

    this.audio.addEventListener('ended', () => {
      this.closeSession(SessionStatus.COMPLETED);
      if (this.queueIndex < this.queue.length - 1) {
        this.queueIndex++;
        this.playSong(this.queue[this.queueIndex]);
      } else {
        this.playing.set(false);
      }
    });

    window.addEventListener('beforeunload', () => {
      if (this.sessionId) {
        this.closeSession(SessionStatus.INTERRUPTED);
      }
    });
  }

  play(song: SongResponse, queue: SongResponse[] = []) {
    if (this.sessionId) {
      this.closeSession(SessionStatus.SKIPPED);
    }
    this.queue = queue.length ? queue : [song];
    this.queueIndex = this.queue.findIndex((s) => s.id === song.id);
    if (this.queueIndex === -1) this.queueIndex = 0;
    this.playSong(song);
  }

  private playSong(song: SongResponse) {
    this.currentSong.set(song);
    this.streamingService.getStreamUrl(song.id).subscribe({
      next: (res) => {
        this.sessionId = res.sessionId;
        this.expiresAt = res.expiresAt;
        this.audio.src = res.streamUrl;
        this.audio.play();
        this.playing.set(true);
      },
      error: () => console.error('Could not get stream URL'),
    });
  }

  pause() {
    this.audio.pause();
    this.playing.set(false);
    if (this.sessionId) {
      this.logEvent(StreamingEventType.PAUSE);
    }
  }

  resume() {
    if (this.isUrlExpired()) {
      this.refreshStream();
      return;
    }
    this.audio.play();
    this.playing.set(true);
    if (this.sessionId) {
      this.logEvent(StreamingEventType.RESUME);
    }
  }

  seek(seconds: number) {
    const from = this.audio.currentTime;
    this.audio.currentTime = seconds;
    if (this.sessionId) {
      this.streamingService
        .logEvent(this.sessionId, {
          type: 'SEEK' as StreamingEventType,
          seekFromSec: from,
          seekToSec: seconds,
        })
        .subscribe();
    }
  }

  next() {
    if (this.queueIndex < this.queue.length - 1) {
      this.closeSession(SessionStatus.SKIPPED);
      this.queueIndex++;
      this.playSong(this.queue[this.queueIndex]);
    }
  }

  previous() {
    if (this.queueIndex > 0) {
      this.closeSession(SessionStatus.SKIPPED);
      this.queueIndex--;
      this.playSong(this.queue[this.queueIndex]);
    }
  }

  setQueue(songs: SongResponse[]) {
    this.queue = songs;
  }

  private isUrlExpired(): boolean {
    if (!this.expiresAt) return true;
    return new Date(this.expiresAt) <= new Date();
  }

  private refreshStream() {
    const song = this.currentSong();
    if (!song) return;
    this.streamingService.getStreamUrl(song.id).subscribe({
      next: (res) => {
        this.sessionId = res.sessionId;
        this.expiresAt = res.expiresAt;
        this.audio.src = res.streamUrl;
        this.audio.currentTime = this.currentTime();
        this.audio.play();
        this.playing.set(true);
      },
    });
  }

  private logEvent(type: StreamingEventType) {
    if (!this.sessionId) return;
    this.streamingService.logEvent(this.sessionId, { type }).subscribe();
  }

  private closeSession(status: SessionStatus) {
    if (!this.sessionId) return;
    this.streamingService.closeSession(this.sessionId, { status }).subscribe();
    this.sessionId = null;
  }
}
