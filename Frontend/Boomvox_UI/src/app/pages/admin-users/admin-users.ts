import { Component, inject, OnInit, signal } from '@angular/core';
import { LowerCasePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AdminService } from '../../services/admin';
import { UserResponse } from '../../models/users';
import { UserRole } from '../../models/enums';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [FormsModule, LowerCasePipe],
  templateUrl: './admin-users.html',
  styleUrl: './admin-users.css',
})
export class AdminUsers implements OnInit{
  private readonly adminService = inject(AdminService);
  private readonly router = inject(Router);

  readonly users = signal<UserResponse[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  searchQuery = '';
  roleFilter: UserRole | '' = '';

  readonly roles: UserRole[] = Object.values(UserRole);

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.error.set(null);
    this.adminService
      .getUsers(this.searchQuery || undefined, this.roleFilter || undefined)
      .subscribe({
        next: (users) => {
          this.users.set(users);
          this.loading.set(false);
        },
        error: () => {
          this.error.set('Could not load users');
          this.loading.set(false);
        },
      });
  }

  clearFilters() {
    this.searchQuery = '';
    this.roleFilter = '';
    this.load();
  }

  goToUser(id: number) {
    this.router.navigate(['/users', id]);
  }
}
