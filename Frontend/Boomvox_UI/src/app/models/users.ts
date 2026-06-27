import { UserRole } from './enums';

export interface UserResponse {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  role: UserRole;
  favouritesListId: number;
}

export interface UpdateUserRequest {
  username: string;
  firstName: string;
  lastName: string;
}

export interface ListeningHistoryResponse {
  id: number;
  songId: number;
  songTitle: string;
  listenedAt: string;
  listenedDurationSeconds: number;
}

export interface UserPreferenceResponse {
  userId: number;
  genreScores: Record<string, number>;
  artistScores: Record<string, number>;
  tagScores: Record<string, number>;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  number: number;
  size: number;
  totalPages: number;
}
