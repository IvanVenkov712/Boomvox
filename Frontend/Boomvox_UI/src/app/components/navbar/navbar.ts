import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
})
export class Navbar {
  readonly auth = inject(AuthService);

  get isAuthor(): boolean {
    const role = this.auth.getRole();
    return role === 'AUTHOR' || role === 'ADMIN';
  }
}
