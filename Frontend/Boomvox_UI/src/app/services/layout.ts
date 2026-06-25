import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class Layout {

  sidebarCollapsed = signal(false);

  toggleSidebar(): void {
    this.sidebarCollapsed.update(v => !v);
  }

  setSidebarCollapsed(value: boolean): void {
    this.sidebarCollapsed.set(value);
  }
}
