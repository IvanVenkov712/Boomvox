import { Routes } from '@angular/router';
import { Login } from './pages/login/login';
import { Home } from './pages/home/home';
import { Profile } from './pages/profile/profile';
import { Songs } from './pages/songs/songs';
import { SongDetail } from './pages/song-detail/song-detail';
import { Artists } from './pages/artists/artists';
import { ArtistDetail } from './pages/artist-detail/artist-detail';
import { AlbumDetail } from './pages/album-detail/album-detail';
import { Playlists } from './pages/playlists/playlists';
import { PlaylistDetail } from './pages/playlist-detail/playlist-detail';
import { Favourites } from './pages/favourites/favourites';
import { UserDetail } from './pages/user-detail/user-detail';
import { authGuard } from './guards/auth-guard';

export const routes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'home', component: Home, canActivate: [authGuard] },
  { path: 'profile', component: Profile, canActivate: [authGuard] },
  { path: 'songs', component: Songs, canActivate: [authGuard] },
  { path: 'songs/:id', component: SongDetail, canActivate: [authGuard] },
  { path: 'artists', component: Artists, canActivate: [authGuard] },
  { path: 'artists/:id', component: ArtistDetail, canActivate: [authGuard] },
  { path: 'album/:id', component: AlbumDetail, canActivate: [authGuard] },
  { path: 'playlists', component: Playlists, canActivate: [authGuard] },
  { path: 'playlists/:id', component: PlaylistDetail, canActivate: [authGuard] },
  { path: 'favourites', component: Favourites, canActivate: [authGuard] },
  { path: 'users/:id', component: UserDetail, canActivate: [authGuard] },
];
