import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { UserService } from '../../services/user';
import { UserResponse } from '../../models/users';

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

  readonly user = signal<UserResponse | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.userService.getById(id).subscribe({
      next: (user) => {
        this.user.set(user);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('User not found');
        this.loading.set(false);
      },
    });
  }
}
