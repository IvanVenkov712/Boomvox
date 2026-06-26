export interface RatingRequest {
  songId: number;
  grade: number;
  comment: string;
}

export interface RatingResponse {
  userId: number;
  songId: number;
  grade: number;
  comment: string;
  lastUpdatedAt: string;
}

export interface SongStatsResponse {
  id: number;
  ratingsCount: number;
  avgRating: number;
  playsCount: number;
  playlistsCount: number;
  recommendationsCount: number;
}
