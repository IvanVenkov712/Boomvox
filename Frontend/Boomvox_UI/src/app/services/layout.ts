import { Injectable, signal } from '@angular/core';

export type SidebarMode = 'search' | 'artists' | 'album' | 'playlist' | 'favourites' | 'hidden';

@Injectable({
  providedIn: 'root',
})
export class LayoutService {

  readonly sidebarCollapsed = signal(false);
  readonly sidebarMode = signal<SidebarMode>('hidden');

  toggleSidebar() {
    this.sidebarCollapsed.update((v) => !v);
  }

  setSidebarCollapsed(value: boolean) {
    this.sidebarCollapsed.set(value);
  }

  setSidebarMode(mode: SidebarMode) {
    this.sidebarMode.set(mode);
  }
}
