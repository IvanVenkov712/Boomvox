import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { UserService } from '../../services/user';
import { UserResponse } from '../../models/users';

@Component({
  selector: 'app-user-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './user-detail.html',
  styleUrl: './user-detail.css',
})
export class UserDetail implements OnInit {

  user: UserResponse | null = null;
  loading = true;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private userService: UserService,
  ) {}

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.userService.getById(id).subscribe({
      next: (user) => {
        this.user = user;
        this.loading = false;
      },
      error: () => {
        this.error = 'User not found';
        this.loading = false;
      },
    });
  }
}
