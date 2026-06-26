export interface FavouritesListResponse {
  id: number;
  name: string;
  createdAt: string;
}

export interface FavouritesListSongResponse {
  favouritesListId: number;
  songId: number;
  position: number;
  addedAt: string;
}

export interface FavouritesListSongRequest {
  songId: number;
}
