import { Component } from '@angular/core';
import { RouterOutlet, Router } from '@angular/router';
import { Navbar } from './components/navbar/navbar';
import { SidebarBrowser } from './components/sidebar-browser/sidebar-browser';
import { PlayerBar } from './components/player-bar/player-bar';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Navbar, SidebarBrowser, PlayerBar],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {

  constructor(public router: Router) {}

  get isLoginPage(): boolean {
    return this.router.url === '/login';
  }
}
