import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { UserService } from '../../services/user';
import { UserResponse } from '../../models/users';
import { AdminService } from '../../services/admin';
import { AuthService } from '../../services/auth';
import { UserRole } from '../../models/enums';

@Component({
  selector: 'app-user-detail',
  standalone: true,
  imports: [],
  templateUrl: './user-detail.html',
  styleUrl: './user-detail.css',
})
export class UserDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly userService = inject(UserService);
  private readonly adminService = inject(AdminService);
  private readonly authService = inject(AuthService);

  readonly user = signal<UserResponse | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly promoting = signal(false);
  readonly promoteSuccess = signal(false);

  selectedRole: UserRole = UserRole.ORDINARY_USER;
  readonly roles: UserRole[] = Object.values(UserRole);

  get isAdmin(): boolean {
    return this.authService.getRole() === 'ADMIN';
  }

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.userService.getById(id).subscribe({
      next: (user) => {
        this.user.set(user);
        this.selectedRole = user.role;
        this.loading.set(false);
      },
      error: () => {
        this.error.set('User not found');
        this.loading.set(false);
      },
    });
  }

  promoteUser() {
    const user = this.user();
    if (!user) return;
    this.promoting.set(true);
    this.promoteSuccess.set(false);
    this.adminService.promoteUser(user.id, this.selectedRole).subscribe({
      next: (updated) => {
        this.user.set(updated);
        this.promoting.set(false);
        this.promoteSuccess.set(true);
      },
      error: () => {
        this.error.set('Could not update role');
        this.promoting.set(false);
      },
    });
  }
}
