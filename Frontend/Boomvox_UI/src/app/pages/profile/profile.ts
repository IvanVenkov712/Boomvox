import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../services/user';
import {
  UpdateUserRequest,
  ListeningHistoryResponse,
  UserPreferenceResponse,
  Page,
} from '../../models/users';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class Profile implements OnInit {
  private readonly userService = inject(UserService);

  readonly user = this.userService.currentUser;
  readonly loading = this.userService.loading;
  readonly error = this.userService.error;

  readonly preferences = signal<UserPreferenceResponse | null>(null);
  readonly historyPage = signal<Page<ListeningHistoryResponse> | null>(null);

  readonly editMode = signal(false);
  readonly saving = signal(false);
  readonly saveError = signal<string | null>(null);
  readonly saveSuccess = signal(false);

  readonly currentPage = signal(0);
  readonly pageSize = 20;

  editData: UpdateUserRequest = { username: '', firstName: '', lastName: '' };

  ngOnInit(): void {
    this.userService.loadMe();
    this.loadHistory();
    this.loadPreferences();
  }

  loadHistory(page = 0): void {
    this.userService.getHistory(page, this.pageSize).subscribe({
      next: (h) => this.historyPage.set(h),
    });
  }

  loadPreferences(): void {
    this.userService.getPreferences().subscribe({
      next: (p) => this.preferences.set(p),
    });
  }

  toggleEdit(): void {
    const user = this.user();
    if (user && !this.editMode()) {
      this.editData = {
        username: user.username,
        firstName: user.firstName,
        lastName: user.lastName,
      };
    }
    this.editMode.update((v) => !v);
    this.saveError.set(null);
    this.saveSuccess.set(false);
  }

  saveProfile(): void {
    this.saving.set(true);
    this.saveError.set(null);
    this.saveSuccess.set(false);
    this.userService.updateMe(this.editData).subscribe({
      next: () => {
        this.saving.set(false);
        this.saveSuccess.set(true);
        this.editMode.set(false);
      },
      error: (err) => {
        this.saveError.set(err.error?.message ?? 'Could not save profile.');
        this.saving.set(false);
      },
    });
  }

  goToPage(page: number): void {
    this.currentPage.set(page);
    this.loadHistory(page);
  }

  get preferenceGenres(): [string, number][] {
    return Object.entries(this.preferences()?.genreScores ?? {}).sort((a, b) => b[1] - a[1]);
  }

  get preferenceArtists(): [string, number][] {
    return Object.entries(this.preferences()?.artistScores ?? {}).sort((a, b) => b[1] - a[1]);
  }

  get preferenceTags(): [string, number][] {
    return Object.entries(this.preferences()?.tagScores ?? {}).sort((a, b) => b[1] - a[1]);
  }
}
