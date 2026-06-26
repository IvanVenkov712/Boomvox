import { Component, inject, signal } from '@angular/core';
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
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly activeTab = signal<'login' | 'register'>('login');
  readonly error = signal<string | null>(null);
  readonly loading = signal(false);

  loginData = { email: '', password: '' };

  registerData = {
    username: '',
    firstName: '',
    lastName: '',
    email: '',
    password: '',
  };

  switchTab(tab: 'login' | 'register'): void {
    this.activeTab.set(tab);
    this.error.set(null);
  }

  onLogin(): void {
    this.error.set(null);
    this.loading.set(true);
    this.auth.login(this.loginData).subscribe({
      next: () => this.router.navigate(['/home']),
      error: (err) => {
        this.error.set(err.error?.message ?? 'Login failed.');
        this.loading.set(false);
      },
    });
  }

  onRegister(): void {
    this.error.set(null);
    this.loading.set(true);
    this.auth.register(this.registerData).subscribe({
      next: () => this.router.navigate(['/home']),
      error: (err) => {
        this.error.set(err.error?.message ?? 'Registration failed.');
        this.loading.set(false);
      },
    });
  }
}
