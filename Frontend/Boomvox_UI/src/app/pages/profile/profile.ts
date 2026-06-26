import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../services/user';
import { UserResponse, UpdateUserRequest, ListeningHistoryResponse, UserPreferenceResponse, Page } from '../../models/users';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class Profile implements OnInit {

  user: UserResponse | null = null;
  preferences: UserPreferenceResponse | null = null;
  historyPage: Page<ListeningHistoryResponse> | null = null;

  editMode = false;
  editData: UpdateUserRequest = { username: '', firstName: '', lastName: '' };

  currentPage = 0;
  pageSize = 20;

  loading = true;
  saving = false;
  error: string | null = null;
  saveError: string | null = null;
  saveSuccess = false;

  constructor(private userService: UserService) {}

  ngOnInit() {
    this.loadProfile();
    this.loadHistory();
    this.loadPreferences();
  }

  loadProfile() {
    this.userService.getMe().subscribe({
      next: (user) => {
        this.user = user;
        this.editData = {
          username: user.username,
          firstName: user.firstName,
          lastName: user.lastName,
        };
        this.loading = false;
      },
      error: () => {
        this.error = 'Could not load profile';
        this.loading = false;
      },
    });
  }

  loadHistory(page = 0) {
    this.userService.getHistory(page, this.pageSize).subscribe({
      next: (h) => (this.historyPage = h),
    });
  }

  loadPreferences() {
    this.userService.getPreferences().subscribe({
      next: (p) => (this.preferences = p),
    });
  }

  toggleEdit() {
    this.editMode = !this.editMode;
    this.saveError = null;
    this.saveSuccess = false;
  }

  saveProfile() {
    this.saving = true;
    this.saveError = null;
    this.saveSuccess = false;
    this.userService.updateMe(this.editData).subscribe({
      next: (user) => {
        this.user = user;
        this.saving = false;
        this.saveSuccess = true;
        this.editMode = false;
      },
      error: (err) => {
        this.saveError = err.error?.message ?? 'Could not save profile';
        this.saving = false;
      },
    });
  }

  goToPage(page: number) {
    this.currentPage = page;
    this.loadHistory(page);
  }

  get preferenceGenres(): [string, number][] {
    return Object.entries(this.preferences?.genreScores ?? {}).sort((a, b) => b[1] - a[1]);
  }

  get preferenceArtists(): [string, number][] {
    return Object.entries(this.preferences?.artistScores ?? {}).sort((a, b) => b[1] - a[1]);
  }

  get preferenceTags(): [string, number][] {
    return Object.entries(this.preferences?.tagScores ?? {}).sort((a, b) => b[1] - a[1]);
  }
}
