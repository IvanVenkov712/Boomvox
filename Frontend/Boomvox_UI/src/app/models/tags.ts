export interface TagResponse {
  id: number;
  name: string;
}

export interface SongTagRequest {
  songId: number;
  tagId: number;
}
