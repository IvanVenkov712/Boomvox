import { Genre, SongFormat, SongProcessingStatus } from './enums';

export interface SongResponse {
  id: number;
  albumId: number|null;
  name: string;
  uploadedAt: string;
  format: SongFormat;
  duration: number;
  fileSize: number;
  storageKey: string;
  statsId: number;
  processingStatus: SongProcessingStatus;
}

export interface SongUploadRequest {
  name: string;
  genre: Genre;
  albumId: number|null;
}

export interface SongRequest {
  name: string;
  genre: Genre;
  albumId: number|null;
}
