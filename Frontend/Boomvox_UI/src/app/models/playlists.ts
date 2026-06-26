export interface PlaylistResponse {
  id: number;
  name: string;
  createdAt: string;
  ownerId: number;
}

export interface PlaylistRequest {
  name: string;
}

export interface PlaylistSongResponse {
  playlistId: number;
  songId: number;
  position: number;
  addedAt: string;
}

export interface PlaylistSongRequest {
  songId: number;
  position: number;
}
