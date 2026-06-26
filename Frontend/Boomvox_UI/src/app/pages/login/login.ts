import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {

  activeTab: 'login'|'register' = 'login';
  error: string|null = null;
  loading = false;

  loginData = { email: '', password: '' };

  registerData = {
    username: '',
    firstName: '',
    lastName: '',
    email: '',
    password: ''
  };

  constructor(private auth: AuthService, private router: Router) {}

  switchTab(tab: 'login'|'register') {
    this.activeTab = tab;
    this.error = null;
  }

  onLogin(): void {
    this.error = null;
    this.loading = true;
    this.auth.login(this.loginData).subscribe({
      next: () => this.router.navigate(['/home']),
      error: err => {
        this.error = err.error?.message ?? 'Login failed';
        this.loading = false;
      }
    });
  }

  onRegister(): void {
    this.error = null;
    this.loading = true;
    this.auth.register(this.registerData).subscribe({
      next: () => this.router.navigate(['/home']),
      error: err => {
        this.error = err.error?.message ?? 'Registration failed';
        this.loading = false;
      }
    });
  }
}
