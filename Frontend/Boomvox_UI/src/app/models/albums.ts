import { Genre } from './enums';

export interface AlbumResponse {
  id: number;
  name: string;
  genre: Genre;
  createdAt: string;
}

export interface AlbumRequest {
  name: string;
  genre: Genre;
}
