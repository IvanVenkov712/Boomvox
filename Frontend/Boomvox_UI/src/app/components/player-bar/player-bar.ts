import { Component, inject } from '@angular/core';
import { PlayerService } from '../../services/player';

@Component({
  selector: 'app-player-bar',
  standalone: true,
  imports: [],
  templateUrl: './player-bar.html',
  styleUrl: './player-bar.css',
})
export class PlayerBar {
  readonly playerService = inject(PlayerService);

  get song() {
    return this.playerService.currentSong();
  }
  get playing() {
    return this.playerService.playing();
  }
  get currentTime() {
    return this.playerService.currentTime();
  }
  get duration() {
    return this.playerService.duration();
  }

  togglePlay() {
    this.playing ? this.playerService.pause() : this.playerService.resume();
  }

  onSeek(event: Event) {
    const value = Number((event.target as HTMLInputElement).value);
    this.playerService.seek(value);
  }

  formatTime(seconds: number): string {
    const m = Math.floor(seconds / 60);
    const s = Math.floor(seconds % 60);
    return `${m}:${s.toString().padStart(2, '0')}`;
  }
}
