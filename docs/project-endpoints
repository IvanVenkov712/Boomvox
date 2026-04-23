# Music Platform — Full API Reference

> **Base URL:** `https://api.Boomvox.com`  
> **Auth:** All protected endpoints require `Authorization: Bearer <JWT_TOKEN>` header  
> **Content-Type:** `application/json` unless noted as `multipart/form-data`

---

## Table of Contents

1. [Auth](#1-auth)
2. [Users](#2-users)
3. [Songs](#3-songs)
4. [Artists](#4-artists)
5. [Albums](#5-albums)
6. [Ratings](#6-ratings)
7. [Comments](#7-comments)
8. [Playlists](#8-playlists)
9. [Collections](#9-collections)
10. [Favorites](#10-favorites)
11. [Listen Later](#11-listen-later)
12. [Recommendations](#12-recommendations)
13. [Streaming Sessions](#13-streaming-sessions)
14. [Reports](#14-reports)
15. [Genres & Styles & Tags](#15-genres--styles--tags)
16. [Admin](#16-admin)

---

## 1. Auth

### POST `/api/auth/register`
Register a new user account.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "display_name": "John Doe",
  "role": "USER"
}
```

**Response `201 Created`:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "user_id": 1,
    "email": "user@example.com",
    "display_name": "John Doe",
    "role": "USER",
    "status": "ACTIVE"
  }
}
```

---

### POST `/api/auth/login`
Authenticate and receive a JWT token.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "SecurePass123!"
}
```

**Response `200 OK`:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "expires_in": 86400,
  "user": {
    "user_id": 1,
    "role": "USER",
    "display_name": "John Doe"
  }
}
```

---

### POST `/api/auth/refresh`
Exchange a valid token for a new one.

**Headers:** `Authorization: Bearer <token>`

**Response `200 OK`:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "expires_in": 86400
}
```

---

### POST `/api/auth/logout`
Invalidate the current JWT token server-side.

**Headers:** `Authorization: Bearer <token>`

**Response `200 OK`:**
```json
{
  "message": "Logged out successfully"
}
```

---

### POST `/api/auth/forgot-password`
Send a password reset email.

**Request Body:**
```json
{
  "email": "user@example.com"
}
```

**Response `200 OK`:**
```json
{
  "message": "Reset email sent if account exists"
}
```

---

### POST `/api/auth/reset-password`
Reset password using the emailed token.

**Request Body:**
```json
{
  "token": "reset_token_abc123",
  "new_password": "NewPass456!"
}
```

**Response `200 OK`:**
```json
{
  "message": "Password reset successfully"
}
```

---

## 2. Users

### GET `/api/users/me`
Get the full profile of the currently authenticated user.

**Headers:** `Authorization: Bearer <token>`

**Response `200 OK`:**
```json
{
  "user_id": 1,
  "email": "user@example.com",
  "display_name": "John Doe",
  "bio": "Music lover",
  "profile_image_url": "https://s3.../avatar.jpg",
  "social_links": {
    "spotify": "https://spotify.com/user/johndoe",
    "instagram": "https://instagram.com/johndoe"
  },
  "preferred_genres": [
    { "genre_id": 1, "name": "Lo-Fi" },
    { "genre_id": 3, "name": "Jazz" }
  ],
  "role": "USER",
  "status": "ACTIVE",
  "created_at": "2026-01-01T00:00:00Z"
}
```

---

### PUT `/api/users/me`
Update the current user's profile.

**Headers:** `Authorization: Bearer <token>`  
**Content-Type:** `multipart/form-data`

**Request Body:**
```json
{
  "display_name": "John Doe Updated",
  "bio": "Updated bio text",
  "avatar": "<binary image file>",
  "preferred_genre_ids": [1, 3, 5],
  "social_links": {
    "instagram": "https://instagram.com/johndoe"
  }
}
```

**Response `200 OK`:**
```json
{
  "user_id": 1,
  "display_name": "John Doe Updated",
  "bio": "Updated bio text",
  "profile_image_url": "https://s3.../avatar_new.jpg",
  "updated_at": "2026-04-22T10:00:00Z"
}
```

---

### GET `/api/users/{userId}`
Get any user's public profile.

**Path Params:** `userId` — integer

**Response `200 OK`:**
```json
{
  "user_id": 1,
  "display_name": "John Doe",
  "bio": "Music lover",
  "profile_image_url": "https://s3.../avatar.jpg",
  "created_at": "2026-01-01T00:00:00Z"
}
```

---

### GET `/api/users/me/listening-history`
Get the authenticated user's streaming history.

**Headers:** `Authorization: Bearer <token>`

**Query Params:**

| Param | Type    | Default | Description          |
|-------|---------|---------|----------------------|
| page  | integer | 0       | Page number          |
| size  | integer | 20      | Results per page     |

**Response `200 OK`:**
```json
{
  "content": [
    {
      "song_id": 42,
      "title": "Chill Beat",
      "artist": { "artist_id": 5, "stage_name": "BeatMaker" },
      "cover_url": "https://s3.../cover.jpg",
      "listened_at": "2026-04-20T14:00:00Z",
      "listened_duration_sec": 185
    }
  ],
  "total_elements": 120,
  "page": 0,
  "size": 20
}
```

---

## 3. Songs

### GET `/api/songs`
Browse and search the song catalog with filtering.

**Query Params:**

| Param             | Type    | Description                            |
|-------------------|---------|----------------------------------------|
| page              | integer | Page number (default: 0)               |
| size              | integer | Results per page (default: 20)         |
| sort              | string  | e.g. `avg_rating,desc`                 |
| search            | string  | Full-text search (title, artist, tags) |
| genre_id          | integer | Filter by genre                        |
| style_id          | integer | Filter by style                        |
| min_rating        | double  | Minimum average rating                 |
| release_date_from | date    | Format: `YYYY-MM-DD`                   |
| release_date_to   | date    | Format: `YYYY-MM-DD`                   |
| tag               | string  | Filter by tag name                     |
| artist_id         | integer | Filter by artist                       |

**Response `200 OK`:**
```json
{
  "content": [
    {
      "song_id": 42,
      "title": "Chill Beat",
      "artist": {
        "artist_id": 5,
        "stage_name": "BeatMaker"
      },
      "genre": { "genre_id": 1, "name": "Lo-Fi" },
      "style": { "style_id": 2, "name": "Instrumental" },
      "tags": ["chill", "lofi", "beats"],
      "avg_rating": 8.4,
      "ratings_count": 210,
      "plays_count_cached": 5400,
      "duration_sec": 185,
      "cover_url": "https://s3.../cover.jpg",
      "publish_date": "2026-02-10",
      "processing_status": "ACTIVE",
      "visibility": "PUBLIC"
    }
  ],
  "total_elements": 450,
  "page": 0,
  "size": 20,
  "total_pages": 23
}
```

---

### POST `/api/songs`
Upload a new song. Triggers async processing pipeline (FFmpeg conversion, variant generation).

**Headers:** `Authorization: Bearer <token>` (Artist or Admin only)  
**Content-Type:** `multipart/form-data`

**Request Body:**
```json
{
  "audio_file": "<binary MP3 or WAV>",
  "cover_art": "<binary image>",
  "title": "My New Track",
  "description": "A chill lo-fi track I made last summer",
  "genre_id": 1,
  "style_id": 2,
  "tags": ["chill", "lofi", "instrumental"],
  "release_date": "2026-04-20",
  "album_id": null,
  "is_single": true,
  "rights_declaration": true
}
```

**Response `201 Created`:**
```json
{
  "song_id": 42,
  "title": "My New Track",
  "processing_status": "PROCESSING",
  "cover_url": "https://s3.../cover.jpg",
  "artist_id": 5,
  "created_at": "2026-04-20T10:00:00Z"
}
```

---

### GET `/api/songs/{songId}`
Get full details of a single song.

**Path Params:** `songId` — integer

**Response `200 OK`:**
```json
{
  "song_id": 42,
  "title": "Chill Beat",
  "description": "A chill lo-fi track",
  "artist": {
    "artist_id": 5,
    "stage_name": "BeatMaker",
    "verified": true
  },
  "album": null,
  "genre": { "genre_id": 1, "name": "Lo-Fi" },
  "style": { "style_id": 2, "name": "Instrumental" },
  "tags": ["chill", "lofi", "beats"],
  "cover_url": "https://s3.../cover.jpg",
  "duration_sec": 185,
  "avg_rating": 8.4,
  "ratings_count": 210,
  "plays_count_cached": 5400,
  "publish_date": "2026-02-10",
  "processing_status": "ACTIVE",
  "visibility": "PUBLIC",
  "rights_confirmed": true,
  "created_at": "2026-02-01T00:00:00Z",
  "updated_at": "2026-02-10T00:00:00Z"
}
```

---

### PUT `/api/songs/{songId}`
Update song metadata. Artist (own songs) or Admin only.

**Headers:** `Authorization: Bearer <token>`  
**Content-Type:** `multipart/form-data`

**Request Body:**
```json
{
  "title": "Updated Title",
  "description": "Updated description",
  "genre_id": 2,
  "style_id": 3,
  "tags": ["new", "tag", "updated"],
  "visibility": "PUBLIC",
  "cover_art": "<binary image — optional>"
}
```

**Response `200 OK`:**
```json
{
  "song_id": 42,
  "title": "Updated Title",
  "updated_at": "2026-04-21T10:00:00Z"
}
```

---

### DELETE `/api/songs/{songId}`
Soft-delete a song and its variants. Artist (own) or Admin only.

**Headers:** `Authorization: Bearer <token>`

**Response `200 OK`:**
```json
{
  "message": "Song deleted successfully"
}
```

---

### GET `/api/songs/{songId}/status`
Poll the processing status of an uploaded song.

**Headers:** `Authorization: Bearer <token>` (Artist or Admin only)

**Response `200 OK`:**
```json
{
  "song_id": 42,
  "processing_status": "ACTIVE",
  "variants_ready": 3,
  "variants": [
    { "format": "MP3", "bitrate_kbps": 128, "status": "READY" },
    { "format": "MP3", "bitrate_kbps": 320, "status": "READY" },
    { "format": "AAC", "bitrate_kbps": 256, "status": "READY" }
  ]
}
```

> `processing_status` values: `PENDING` · `PROCESSING` · `ACTIVE` · `FAILED` · `DELETED`

---

### GET `/api/songs/{songId}/similar`
Get songs similar by genre, style, tags, and user preference signals.

**Query Params:**

| Param | Type    | Default | Description              |
|-------|---------|---------|--------------------------|
| limit | integer | 10      | Number of results        |

**Response `200 OK`:**
```json
{
  "based_on": { "song_id": 42, "title": "Chill Beat" },
  "songs": [
    {
      "song_id": 43,
      "title": "Study Beats",
      "similarity_score": 0.87,
      "artist": { "artist_id": 6, "stage_name": "FocusWave" }
    }
  ]
}
```

---

### GET `/api/songs/{songId}/stream`
Get a signed streaming URL or stream audio directly. Supports HTTP Range Requests for seeking.

**Headers:**  
`Authorization: Bearer <token>`  
`Range: bytes=0-` *(optional — for seeking)*

**Query Params:**

| Param   | Type   | Default | Description                         |
|---------|--------|---------|-------------------------------------|
| quality | string | `320k`  | Requested bitrate: `128k`, `320k`, `256k` |

**Response `200 OK`:**
```json
{
  "streaming_url": "https://your-bucket.s3.amazonaws.com/variants/song_42_320k.mp3?X-Amz-Expires=900&X-Amz-Signature=...",
  "expires_in": 900,
  "format": "MP3",
  "bitrate_kbps": 320,
  "duration_sec": 185,
  "file_size_bytes": 7375000
}
```

**Response `206 Partial Content`** *(when Range header is provided):*
```
Content-Range: bytes 500000-999999/7375000
Content-Type: audio/mpeg
```

---

### GET `/api/songs/{songId}/variants`
Get all processed audio variants for a song. Artist (own) or Admin only.

**Headers:** `Authorization: Bearer <token>`

**Response `200 OK`:**
```json
{
  "song_id": 42,
  "variants": [
    {
      "variant_id": 1,
      "format": "MP3",
      "bitrate_kbps": 128,
      "duration_sec": 185,
      "file_size_bytes": 2950000,
      "is_default": false,
      "status": "READY"
    },
    {
      "variant_id": 2,
      "format": "MP3",
      "bitrate_kbps": 320,
      "duration_sec": 185,
      "file_size_bytes": 7375000,
      "is_default": true,
      "status": "READY"
    },
    {
      "variant_id": 3,
      "format": "AAC",
      "bitrate_kbps": 256,
      "duration_sec": 185,
      "file_size_bytes": 5920000,
      "is_default": false,
      "status": "READY"
    }
  ]
}
```

---

## 4. Artists

### GET `/api/artists`
Browse the public list of artists.

**Query Params:**

| Param    | Type    | Description                     |
|----------|---------|---------------------------------|
| page     | integer | Page number (default: 0)        |
| size     | integer | Results per page (default: 20)  |
| search   | string  | Search by stage name            |
| verified | boolean | Filter by verified status       |

**Response `200 OK`:**
```json
{
  "content": [
    {
      "artist_id": 5,
      "stage_name": "BeatMaker",
      "public_description": "Lo-Fi producer from Sofia",
      "verified": true,
      "profile_image_url": "https://s3.../avatar.jpg",
      "song_count": 14
    }
  ],
  "total_elements": 80,
  "page": 0
}
```

---

### GET `/api/artists/{artistId}`
Get a public artist profile with their songs and albums.

**Response `200 OK`:**
```json
{
  "artist_id": 5,
  "stage_name": "BeatMaker",
  "public_description": "Lo-Fi producer from Sofia",
  "verified": true,
  "user": {
    "display_name": "John Doe",
    "profile_image_url": "https://s3.../avatar.jpg"
  },
  "songs": [
    {
      "song_id": 42,
      "title": "Chill Beat",
      "avg_rating": 8.4,
      "cover_url": "https://s3.../cover.jpg",
      "publish_date": "2026-02-10"
    }
  ],
  "albums": [
    {
      "album_id": 1,
      "title": "Summer Beats Vol.1",
      "release_date": "2026-01-15"
    }
  ]
}
```

---

### POST `/api/artists/register`
Upgrade current user account to Artist role.

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "stage_name": "BeatMaker",
  "public_description": "Lo-Fi producer based in Sofia"
}
```

**Response `201 Created`:**
```json
{
  "artist_id": 5,
  "stage_name": "BeatMaker",
  "user_id": 1
}
```

---

### PUT `/api/artists/me`
Update current artist's stage name and description.

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "stage_name": "BeatMaker Pro",
  "public_description": "Updated artist bio"
}
```

**Response `200 OK`:**
```json
{
  "artist_id": 5,
  "stage_name": "BeatMaker Pro",
  "updated_at": "2026-04-22T10:00:00Z"
}
```

---

## 5. Albums

### GET `/api/albums`
Browse public albums.

**Query Params:**

| Param     | Type    | Description              |
|-----------|---------|--------------------------|
| page      | integer | Page number              |
| size      | integer | Results per page         |
| artist_id | integer | Filter by artist         |

**Response `200 OK`:**
```json
{
  "content": [
    {
      "album_id": 1,
      "title": "Summer Beats Vol.1",
      "artist": { "artist_id": 5, "stage_name": "BeatMaker" },
      "cover_url": "https://s3.../album.jpg",
      "release_date": "2026-01-15",
      "song_count": 8,
      "visibility": "PUBLIC"
    }
  ],
  "total_elements": 24
}
```

---

### POST `/api/albums`
Create a new album. Artist or Admin only.

**Headers:** `Authorization: Bearer <token>`  
**Content-Type:** `multipart/form-data`

**Request Body:**
```json
{
  "title": "Summer Beats Vol.1",
  "description": "My debut album",
  "cover_art": "<binary image>",
  "release_date": "2026-06-01",
  "visibility": "PUBLIC"
}
```

**Response `201 Created`:**
```json
{
  "album_id": 1,
  "title": "Summer Beats Vol.1",
  "artist_id": 5,
  "created_at": "2026-04-22T10:00:00Z"
}
```

---

### GET `/api/albums/{albumId}`
Get album details with full ordered song list.

**Response `200 OK`:**
```json
{
  "album_id": 1,
  "title": "Summer Beats Vol.1",
  "description": "My debut album",
  "cover_url": "https://s3.../album.jpg",
  "artist": { "artist_id": 5, "stage_name": "BeatMaker" },
  "release_date": "2026-01-15",
  "visibility": "PUBLIC",
  "songs": [
    {
      "position": 1,
      "song_id": 42,
      "title": "Chill Beat",
      "duration_sec": 185,
      "avg_rating": 8.4
    }
  ],
  "total_duration_sec": 2400
}
```

---

### PUT `/api/albums/{albumId}`
Update album metadata. Artist (own) or Admin only.

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "title": "Summer Beats Vol.1 (Deluxe)",
  "description": "Extended edition",
  "visibility": "PUBLIC"
}
```

**Response `200 OK`:**
```json
{
  "album_id": 1,
  "title": "Summer Beats Vol.1 (Deluxe)",
  "updated_at": "2026-04-22T10:00:00Z"
}
```

---

### DELETE `/api/albums/{albumId}`
Delete an album. Does not delete the songs themselves.

**Headers:** `Authorization: Bearer <token>`

**Response `200 OK`:**
```json
{
  "message": "Album deleted successfully"
}
```

---

### POST `/api/albums/{albumId}/songs`
Add an existing song to an album at a given position.

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "song_id": 43,
  "position": 2
}
```

**Response `200 OK`:**
```json
{
  "album_id": 1,
  "song_id": 43,
  "position": 2,
  "added_at": "2026-04-22T10:00:00Z"
}
```

---

### DELETE `/api/albums/{albumId}/songs/{songId}`
Remove a song from an album.

**Headers:** `Authorization: Bearer <token>`

**Response `200 OK`:**
```json
{
  "message": "Song removed from album"
}
```

---

## 6. Ratings

### POST `/api/songs/{songId}/ratings`
Submit or update a rating (1–10). One rating per user per song.

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "value": 9
}
```

**Response `200 OK`:**
```json
{
  "rating_id": 100,
  "song_id": 42,
  "user_id": 1,
  "value": 9,
  "new_avg": 8.5,
  "ratings_count": 211,
  "created_at": "2026-04-20T10:00:00Z"
}
```

---

### GET `/api/songs/{songId}/ratings/me`
Get the current user's own rating for a song.

**Headers:** `Authorization: Bearer <token>`

**Response `200 OK`:**
```json
{
  "rating_id": 100,
  "song_id": 42,
  "value": 9,
  "created_at": "2026-04-20T10:00:00Z",
  "updated_at": "2026-04-21T10:00:00Z"
}
```

---

### DELETE `/api/songs/{songId}/ratings/me`
Remove the current user's rating from a song.

**Headers:** `Authorization: Bearer <token>`

**Response `200 OK`:**
```json
{
  "message": "Rating removed",
  "new_avg": 8.3,
  "ratings_count": 209
}
```

---

### GET `/api/songs/{songId}/ratings`
Get aggregate rating statistics for a song.

**Response `200 OK`:**
```json
{
  "song_id": 42,
  "avg_rating": 8.4,
  "ratings_count": 210,
  "distribution": {
    "10": 45,
    "9": 60,
    "8": 55,
    "7": 30,
    "6": 12,
    "5": 5,
    "4": 2,
    "3": 1,
    "2": 0,
    "1": 0
  }
}
```

---

## 7. Comments

### GET `/api/songs/{songId}/comments`
Get paginated top-level comments with nested replies.

**Query Params:**

| Param | Type    | Default |
|-------|---------|---------|
| page  | integer | 0       |
| size  | integer | 20      |

**Response `200 OK`:**
```json
{
  "content": [
    {
      "comment_id": 1,
      "content": "Great track!",
      "author": {
        "user_id": 1,
        "display_name": "John Doe",
        "profile_image_url": "https://s3.../avatar.jpg"
      },
      "status": "ACTIVE",
      "created_at": "2026-04-20T10:00:00Z",
      "updated_at": "2026-04-20T10:00:00Z",
      "replies": [
        {
          "comment_id": 2,
          "content": "Agreed!",
          "author": { "user_id": 2, "display_name": "Jane Smith" },
          "created_at": "2026-04-20T11:00:00Z"
        }
      ]
    }
  ],
  "total_elements": 45,
  "page": 0
}
```

> `status` values: `ACTIVE` · `HIDDEN` · `DELETED` · `FLAGGED`

---

### POST `/api/songs/{songId}/comments`
Post a top-level comment on a song.

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "content": "This track is absolutely amazing!"
}
```

**Response `201 Created`:**
```json
{
  "comment_id": 10,
  "content": "This track is absolutely amazing!",
  "author": { "user_id": 1, "display_name": "John Doe" },
  "created_at": "2026-04-22T09:00:00Z"
}
```

---

### POST `/api/songs/{songId}/comments/{commentId}/replies`
Add a threaded reply to an existing comment.

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "content": "I totally agree with this!"
}
```

**Response `201 Created`:**
```json
{
  "comment_id": 11,
  "parent_comment_id": 1,
  "content": "I totally agree with this!",
  "author": { "user_id": 1, "display_name": "John Doe" },
  "created_at": "2026-04-22T09:05:00Z"
}
```

---

### DELETE `/api/comments/{commentId}`
Soft-delete a comment. Author (own) or Admin only.

**Headers:** `Authorization: Bearer <token>`

**Response `200 OK`:**
```json
{
  "message": "Comment deleted successfully"
}
```

---

## 8. Playlists

### GET `/api/playlists`
Get all playlists owned by the current user.

**Headers:** `Authorization: Bearer <token>`

**Response `200 OK`:**
```json
{
  "playlists": [
    {
      "playlist_id": 1,
      "name": "Morning Vibes",
      "description": "Start the day right",
      "song_count": 12,
      "total_duration_sec": 2220,
      "is_public": true,
      "created_at": "2026-01-01T00:00:00Z",
      "updated_at": "2026-04-20T00:00:00Z"
    }
  ]
}
```

---

### POST `/api/playlists`
Create a new playlist.

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "name": "Morning Vibes",
  "description": "Start the day right",
  "is_public": true
}
```

**Response `201 Created`:**
```json
{
  "playlist_id": 1,
  "name": "Morning Vibes",
  "owner_user_id": 1,
  "created_at": "2026-04-22T10:00:00Z"
}
```

---

### GET `/api/playlists/{playlistId}`
Get playlist details with full ordered song list.

**Response `200 OK`:**
```json
{
  "playlist_id": 1,
  "name": "Morning Vibes",
  "description": "Start the day right",
  "owner": { "user_id": 1, "display_name": "John Doe" },
  "is_public": true,
  "total_duration_sec": 2220,
  "songs": [
    {
      "position": 1,
      "song_id": 42,
      "title": "Chill Beat",
      "artist": { "artist_id": 5, "stage_name": "BeatMaker" },
      "duration_sec": 185,
      "cover_url": "https://s3.../cover.jpg",
      "added_at": "2026-04-01T00:00:00Z"
    }
  ]
}
```

---

### PUT `/api/playlists/{playlistId}`
Update playlist name, description, or visibility.

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "name": "Morning Vibes — Updated",
  "description": "New description",
  "is_public": false
}
```

**Response `200 OK`:**
```json
{
  "playlist_id": 1,
  "name": "Morning Vibes — Updated",
  "updated_at": "2026-04-22T10:00:00Z"
}
```

---

### DELETE `/api/playlists/{playlistId}`
Delete a playlist. Owner only.

**Headers:** `Authorization: Bearer <token>`

**Response `200 OK`:**
```json
{
  "message": "Playlist deleted successfully"
}
```

---

### POST `/api/playlists/{playlistId}/songs`
Add a song to a playlist at a given position.

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "song_id": 43,
  "position": 2
}
```

**Response `200 OK`:**
```json
{
  "playlist_id": 1,
  "song_id": 43,
  "position": 2,
  "added_by_user_id": 1,
  "added_at": "2026-04-22T10:00:00Z"
}
```

---

### DELETE `/api/playlists/{playlistId}/songs/{songId}`
Remove a song from a playlist.

**Headers:** `Authorization: Bearer <token>`

**Response `200 OK`:**
```json
{
  "message": "Song removed from playlist"
}
```

---

### GET `/api/playlists/{playlistId}/stream`
Get ordered stream info for playlist auto-advance playback.

**Headers:** `Authorization: Bearer <token>`

**Query Params:**

| Param    | Type    | Default | Description              |
|----------|---------|---------|--------------------------|
| position | integer | 0       | Current track index      |

**Response `200 OK`:**
```json
{
  "playlist_id": 1,
  "playlist_name": "Morning Vibes",
  "current_position": 0,
  "total_tracks": 12,
  "current_track": {
    "song_id": 42,
    "title": "Chill Beat",
    "artist": "BeatMaker",
    "duration_sec": 185,
    "stream_endpoint": "/api/songs/42/stream"
  },
  "next_track": {
    "song_id": 43,
    "title": "Study Beats",
    "artist": "FocusWave",
    "duration_sec": 210,
    "stream_endpoint": "/api/songs/43/stream"
  }
}
```

---

## 9. Collections

### GET `/api/collections`
Get all collections owned by the current user.

**Headers:** `Authorization: Bearer <token>`

**Response `200 OK`:**
```json
{
  "collections": [
    {
      "collection_id": 1,
      "name": "Best of 2026",
      "description": "Top picks of the year",
      "type": "COLLECTION",
      "song_count": 25,
      "is_public": false,
      "created_at": "2026-01-01T00:00:00Z"
    }
  ]
}
```

> `type` values: `COLLECTION` · `ALBUM_WISHLIST`

---

### POST `/api/collections`
Create a new collection.

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "name": "Best of 2026",
  "description": "Top picks of the year",
  "type": "COLLECTION",
  "is_public": true
}
```

**Response `201 Created`:**
```json
{
  "collection_id": 2,
  "name": "Best of 2026",
  "owner_user_id": 1,
  "created_at": "2026-04-22T10:00:00Z"
}
```

---

### GET `/api/collections/{collectionId}`
Get collection details with full song list.

**Response `200 OK`:**
```json
{
  "collection_id": 1,
  "name": "Best of 2026",
  "owner": { "user_id": 1, "display_name": "John Doe" },
  "type": "COLLECTION",
  "is_public": false,
  "songs": [
    {
      "song_id": 42,
      "title": "Chill Beat",
      "position": 1,
      "added_at": "2026-04-10T10:00:00Z"
    }
  ]
}
```

---

### PUT `/api/collections/{collectionId}`
Update a collection's name, description, or visibility.

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "name": "Best of 2026 — Final",
  "is_public": true
}
```

**Response `200 OK`:**
```json
{
  "collection_id": 1,
  "name": "Best of 2026 — Final",
  "updated_at": "2026-04-22T10:00:00Z"
}
```

---

### DELETE `/api/collections/{collectionId}`
Delete a collection.

**Headers:** `Authorization: Bearer <token>`

**Response `200 OK`:**
```json
{
  "message": "Collection deleted successfully"
}
```

---

### POST `/api/collections/{collectionId}/songs`
Add a song to a collection.

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "song_id": 42
}
```

**Response `200 OK`:**
```json
{
  "collection_id": 1,
  "song_id": 42,
  "added_at": "2026-04-22T10:00:00Z"
}
```

---

### DELETE `/api/collections/{collectionId}/songs/{songId}`
Remove a song from a collection.

**Headers:** `Authorization: Bearer <token>`

**Response `200 OK`:**
```json
{
  "message": "Song removed from collection"
}
```

---

## 10. Favorites

### GET `/api/favorites`
Get all songs marked as favorites by the current user.

**Headers:** `Authorization: Bearer <token>`

**Query Params:**

| Param | Type    | Default |
|-------|---------|---------|
| page  | integer | 0       |
| size  | integer | 20      |

**Response `200 OK`:**
```json
{
  "content": [
    {
      "song_id": 42,
      "title": "Chill Beat",
      "artist": { "artist_id": 5, "stage_name": "BeatMaker" },
      "cover_url": "https://s3.../cover.jpg",
      "avg_rating": 8.4,
      "favorited_at": "2026-04-20T10:00:00Z"
    }
  ],
  "total_elements": 35
}
```

---

### POST `/api/favorites/{songId}`
Add a song to favorites.

**Headers:** `Authorization: Bearer <token>`

**Response `200 OK`:**
```json
{
  "song_id": 42,
  "user_id": 1,
  "favorited_at": "2026-04-22T10:00:00Z"
}
```

---

### DELETE `/api/favorites/{songId}`
Remove a song from favorites.

**Headers:** `Authorization: Bearer <token>`

**Response `200 OK`:**
```json
{
  "message": "Removed from favorites"
}
```

---

## 11. Listen Later

### GET `/api/listen-later`
Get all songs saved to the listen later list.

**Headers:** `Authorization: Bearer <token>`

**Response `200 OK`:**
```json
{
  "songs": [
    {
      "song_id": 43,
      "title": "Study Beats",
      "artist": { "artist_id": 6, "stage_name": "FocusWave" },
      "cover_url": "https://s3.../cover.jpg",
      "duration_sec": 210,
      "added_at": "2026-04-21T10:00:00Z"
    }
  ],
  "total": 8
}
```

---

### POST `/api/listen-later/{songId}`
Add a song to the listen later list.

**Headers:** `Authorization: Bearer <token>`

**Response `200 OK`:**
```json
{
  "song_id": 43,
  "user_id": 1,
  "added_at": "2026-04-22T10:00:00Z"
}
```

---

### DELETE `/api/listen-later/{songId}`
Remove a song from the listen later list.

**Headers:** `Authorization: Bearer <token>`

**Response `200 OK`:**
```json
{
  "message": "Removed from listen later"
}
```

---

## 12. Recommendations

### GET `/api/recommendations/me`
Get personalized recommendations for the current user based on ratings, listening history, favorites, and preferred genres.

**Headers:** `Authorization: Bearer <token>`

**Query Params:**

| Param       | Type    | Default         | Description                                           |
|-------------|---------|-----------------|-------------------------------------------------------|
| limit       | integer | 20              | Number of recommendations                            |
| source_type | string  | (all)           | `CONTENT_BASED`, `COLLABORATIVE`, `ML_MODEL`, `GENRE_BASED` |

**Response `200 OK`:**
```json
{
  "recommendations": [
    {
      "recommendation_id": 1,
      "song": {
        "song_id": 55,
        "title": "Jazz Fusion Vol.2",
        "artist": { "artist_id": 7, "stage_name": "JazzLab" },
        "cover_url": "https://s3.../cover.jpg",
        "avg_rating": 8.9
      },
      "score": 0.94,
      "source_type": "COLLABORATIVE",
      "model_version": "v2.1",
      "generated_at": "2026-04-22T00:00:00Z"
    }
  ]
}
```

> `source_type` values: `CONTENT_BASED` · `COLLABORATIVE` · `ML_MODEL` · `GENRE_BASED` · `MANUAL`

---

### GET `/api/recommendations/similar/{songId}`
Get song recommendations similar to a given song.

**Query Params:**

| Param | Type    | Default |
|-------|---------|---------|
| limit | integer | 10      |

**Response `200 OK`:**
```json
{
  "based_on": {
    "song_id": 42,
    "title": "Chill Beat"
  },
  "recommendations": [
    {
      "song_id": 43,
      "title": "Study Beats",
      "artist": { "artist_id": 6, "stage_name": "FocusWave" },
      "similarity_score": 0.88,
      "avg_rating": 8.1
    }
  ]
}
```

---

### POST `/api/recommendations/{recommendationId}/click`
Log that the user clicked/played a recommended song. Improves the ML model.

**Headers:** `Authorization: Bearer <token>`

**Response `200 OK`:**
```json
{
  "message": "Click logged",
  "recommendation_id": 1,
  "clicked_at": "2026-04-22T10:05:00Z"
}
```

---

## 13. Streaming Sessions

### POST `/api/streaming/sessions`
Log the start of a streaming session when playback begins.

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "song_id": 42,
  "playlist_id": null,
  "collection_id": null,
  "audio_variant_id": 2,
  "device_type": "WEB"
}
```

> `device_type` values: `WEB` · `MOBILE_IOS` · `MOBILE_ANDROID` · `DESKTOP`

**Response `201 Created`:**
```json
{
  "session_id": 500,
  "song_id": 42,
  "audio_variant_id": 2,
  "started_at": "2026-04-22T10:00:00Z",
  "session_token": "tok_abc123xyz"
}
```

---

### PATCH `/api/streaming/sessions/{sessionId}`
Update a session with current playback progress. Called periodically by the player.

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "listened_seconds": 90,
  "last_position_sec": 90,
  "playback_status": "PLAYING"
}
```

> `playback_status` values: `PLAYING` · `PAUSED` · `BUFFERING`

**Response `200 OK`:**
```json
{
  "session_id": 500,
  "last_position_sec": 90,
  "updated_at": "2026-04-22T10:01:30Z"
}
```

---

### POST `/api/streaming/sessions/{sessionId}/end`
Mark a streaming session as ended (completed or interrupted).

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "listened_seconds": 185,
  "last_position_sec": 185,
  "completed": true,
  "skipped": false
}
```

**Response `200 OK`:**
```json
{
  "session_id": 500,
  "status": "COMPLETED",
  "listened_seconds": 185,
  "ended_at": "2026-04-22T10:03:05Z"
}
```

> `status` values: `COMPLETED` · `INTERRUPTED` · `SKIPPED`

---

### POST `/api/streaming/events`
Log a granular playback event. Feeds the recommendation engine.

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "session_id": 500,
  "song_id": 42,
  "event_type": "SEEK",
  "position_sec": 120,
  "seek_from_sec": 45,
  "seek_to_sec": 120,
  "event_time": "2026-04-22T10:01:30Z"
}
```

> `event_type` values: `START` · `PAUSE` · `RESUME` · `SEEK` · `COMPLETE` · `SKIP` · `REPLAY` · `BUFFER`

**Response `201 Created`:**
```json
{
  "event_id": 1001,
  "session_id": 500,
  "event_type": "SEEK",
  "event_time": "2026-04-22T10:01:30Z"
}
```

---

## 14. Reports

### POST `/api/reports`
Submit a report for a song, comment, or user.

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "report_type": "SONG",
  "song_id": 42,
  "comment_id": null,
  "reported_user_id": null,
  "reason": "COPYRIGHT_VIOLATION",
  "description": "This song contains copyrighted material without permission"
}
```

> `report_type` values: `SONG` · `COMMENT` · `USER`  
> `reason` values: `COPYRIGHT_VIOLATION` · `INAPPROPRIATE_CONTENT` · `SPAM` · `HARASSMENT` · `ILLEGAL_CONTENT` · `OTHER`  
> At least one of `song_id`, `comment_id`, `reported_user_id` must be provided.

**Response `201 Created`:**
```json
{
  "report_id": 10,
  "report_type": "SONG",
  "status": "PENDING",
  "created_at": "2026-04-22T10:00:00Z"
}
```

> `status` values: `PENDING` · `UNDER_REVIEW` · `RESOLVED` · `DISMISSED`

---

## 15. Genres & Styles & Tags

### GET `/api/genres`
Get the full list of music genres.

**Response `200 OK`:**
```json
{
  "genres": [
    {
      "genre_id": 1,
      "name": "Lo-Fi",
      "description": "Low fidelity music with a relaxed aesthetic"
    },
    {
      "genre_id": 2,
      "name": "Jazz",
      "description": "Traditional and contemporary jazz"
    }
  ]
}
```

---

### GET `/api/genres/{genreId}/styles`
Get all styles belonging to a specific genre.

**Response `200 OK`:**
```json
{
  "genre_id": 1,
  "genre_name": "Lo-Fi",
  "styles": [
    { "style_id": 2, "name": "Instrumental" },
    { "style_id": 3, "name": "Hip-Hop Beats" },
    { "style_id": 4, "name": "Chillhop" }
  ]
}
```

---

### GET `/api/tags`
Get the most popular tags across the platform.

**Query Params:**

| Param | Type    | Default |
|-------|---------|---------|
| limit | integer | 50      |

**Response `200 OK`:**
```json
{
  "tags": [
    { "tag_id": 1, "name": "chill", "song_count": 320 },
    { "tag_id": 2, "name": "lofi", "song_count": 280 },
    { "tag_id": 3, "name": "instrumental", "song_count": 210 }
  ]
}
```

---

## 16. Admin

> All endpoints in this section require `Authorization: Bearer <token>` with role `ADMIN`.

---

### GET `/api/admin/users`
Get a paginated list of all users with optional filters.

**Query Params:**

| Param  | Type    | Description                                 |
|--------|---------|---------------------------------------------|
| page   | integer | Page number                                 |
| size   | integer | Results per page                            |
| status | string  | `ACTIVE`, `DEACTIVATED`, `RESTRICTED`       |
| role   | string  | `USER`, `ARTIST`, `ADMIN`                   |
| search | string  | Search by email or display name             |

**Response `200 OK`:**
```json
{
  "content": [
    {
      "user_id": 1,
      "email": "user@example.com",
      "display_name": "John Doe",
      "role": "USER",
      "status": "ACTIVE",
      "created_at": "2026-01-01T00:00:00Z"
    }
  ],
  "total_elements": 1200,
  "page": 0
}
```

---

### PATCH `/api/admin/users/{userId}/status`
Activate, deactivate, or restrict a user account.

**Request Body:**
```json
{
  "status": "DEACTIVATED",
  "reason": "Repeated violations of terms of service"
}
```

> `status` values: `ACTIVE` · `DEACTIVATED` · `RESTRICTED`

**Response `200 OK`:**
```json
{
  "user_id": 1,
  "status": "DEACTIVATED",
  "updated_at": "2026-04-22T10:00:00Z"
}
```

---

### GET `/api/admin/reports`
Get paginated list of all submitted reports.

**Query Params:**

| Param       | Type    | Description                                      |
|-------------|---------|--------------------------------------------------|
| page        | integer | Page number                                      |
| size        | integer | Results per page                                 |
| status      | string  | `PENDING`, `UNDER_REVIEW`, `RESOLVED`, `DISMISSED` |
| report_type | string  | `SONG`, `COMMENT`, `USER`                        |

**Response `200 OK`:**
```json
{
  "content": [
    {
      "report_id": 10,
      "report_type": "SONG",
      "song_id": 42,
      "reporter": { "user_id": 3, "display_name": "Jane Smith" },
      "reason": "COPYRIGHT_VIOLATION",
      "status": "PENDING",
      "created_at": "2026-04-22T10:00:00Z"
    }
  ],
  "total_elements": 15
}
```

---

### PATCH `/api/admin/reports/{reportId}`
Review and resolve a report.

**Request Body:**
```json
{
  "status": "RESOLVED",
  "action_taken": "SONG_HIDDEN",
  "admin_note": "Copyright confirmed — song hidden pending review"
}
```

> `action_taken` values: `NONE` · `SONG_HIDDEN` · `SONG_DELETED` · `COMMENT_DELETED` · `USER_RESTRICTED` · `USER_DEACTIVATED`

**Response `200 OK`:**
```json
{
  "report_id": 10,
  "status": "RESOLVED",
  "action_taken": "SONG_HIDDEN",
  "reviewed_by_admin_id": 99,
  "reviewed_at": "2026-04-22T12:00:00Z"
}
```

---

### PATCH `/api/admin/songs/{songId}/visibility`
Change the visibility of a song (hide or restore).

**Request Body:**
```json
{
  "visibility": "HIDDEN",
  "reason": "Reported content under copyright review"
}
```

> `visibility` values: `PUBLIC` · `PRIVATE` · `HIDDEN` · `UNLISTED`

**Response `200 OK`:**
```json
{
  "song_id": 42,
  "visibility": "HIDDEN",
  "updated_at": "2026-04-22T10:00:00Z"
}
```

---

### POST `/api/admin/genres`
Create a new genre.

**Request Body:**
```json
{
  "name": "Neo-Soul",
  "description": "Modern soul music blending R&B and jazz elements"
}
```

**Response `201 Created`:**
```json
{
  "genre_id": 10,
  "name": "Neo-Soul",
  "description": "Modern soul music blending R&B and jazz elements"
}
```

---

### PUT `/api/admin/genres/{genreId}`
Update an existing genre.

**Request Body:**
```json
{
  "name": "Lo-Fi Updated",
  "description": "Updated genre description"
}
```

**Response `200 OK`:**
```json
{
  "genre_id": 1,
  "name": "Lo-Fi Updated",
  "updated_at": "2026-04-22T10:00:00Z"
}
```

---

### DELETE `/api/admin/genres/{genreId}`
Delete a genre. Will fail if songs are still assigned to it.

**Response `200 OK`:**
```json
{
  "message": "Genre deleted successfully"
}
```

---

### POST `/api/admin/genres/{genreId}/styles`
Create a new style under a genre.

**Request Body:**
```json
{
  "name": "Ambient Beats",
  "description": "Slow, atmospheric instrumental beats"
}
```

**Response `201 Created`:**
```json
{
  "style_id": 12,
  "genre_id": 1,
  "name": "Ambient Beats"
}
```

---

### DELETE `/api/admin/styles/{styleId}`
Delete a style.

**Response `200 OK`:**
```json
{
  "message": "Style deleted successfully"
}
```

---

### PATCH `/api/admin/artists/{artistId}/verify`
Mark or unmark an artist as verified.

**Request Body:**
```json
{
  "verified": true
}
```

**Response `200 OK`:**
```json
{
  "artist_id": 5,
  "stage_name": "BeatMaker",
  "verified": true,
  "updated_at": "2026-04-22T10:00:00Z"
}
```

---

### GET `/api/admin/songs`
Get all songs with admin-level details (including hidden/failed).

**Query Params:**

| Param             | Type    | Description                                     |
|-------------------|---------|-------------------------------------------------|
| page              | integer | Page number                                     |
| size              | integer | Results per page                                |
| processing_status | string  | `PENDING`, `PROCESSING`, `ACTIVE`, `FAILED`     |
| visibility        | string  | `PUBLIC`, `HIDDEN`, `PRIVATE`                   |
| artist_id         | integer | Filter by artist                                |

**Response `200 OK`:**
```json
{
  "content": [
    {
      "song_id": 42,
      "title": "Chill Beat",
      "artist_id": 5,
      "processing_status": "ACTIVE",
      "visibility": "PUBLIC",
      "rights_confirmed": true,
      "created_at": "2026-02-01T00:00:00Z"
    }
  ],
  "total_elements": 890
}
```

---

## Error Responses

All endpoints follow a consistent error format:

```json
{
  "timestamp": "2026-04-22T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for field 'value': must be between 1 and 10",
  "path": "/api/songs/42/ratings"
}
```

| Status | Meaning                                      |
|--------|----------------------------------------------|
| 400    | Bad Request — invalid input or missing field |
| 401    | Unauthorized — missing or invalid JWT        |
| 403    | Forbidden — insufficient role/permissions    |
| 404    | Not Found — resource does not exist          |
| 409    | Conflict — duplicate (e.g. rating exists)    |
| 422    | Unprocessable — business rule violation      |
| 500    | Internal Server Error                        |

---

## Enum Reference

| Enum                  | Values                                                                 |
|-----------------------|------------------------------------------------------------------------|
| `SongProcessingStatus`| `PENDING` · `PROCESSING` · `ACTIVE` · `FAILED` · `DELETED`            |
| `SongVisibility`      | `PUBLIC` · `PRIVATE` · `UNLISTED` · `HIDDEN`                          |
| `UserStatus`          | `ACTIVE` · `DEACTIVATED` · `RESTRICTED`                               |
| `UserRole`            | `GUEST` · `USER` · `ARTIST` · `ADMIN`                                 |
| `CommentStatus`       | `ACTIVE` · `HIDDEN` · `DELETED` · `FLAGGED`                           |
| `ReportType`          | `SONG` · `COMMENT` · `USER`                                           |
| `ReportStatus`        | `PENDING` · `UNDER_REVIEW` · `RESOLVED` · `DISMISSED`                 |
| `ReportReason`        | `COPYRIGHT_VIOLATION` · `INAPPROPRIATE_CONTENT` · `SPAM` · `HARASSMENT` · `ILLEGAL_CONTENT` · `OTHER` |
| `SessionStatus`       | `ACTIVE` · `COMPLETED` · `INTERRUPTED` · `SKIPPED`                    |
| `PlaybackStatus`      | `PLAYING` · `PAUSED` · `BUFFERING`                                    |
| `StreamingEventType`  | `START` · `PAUSE` · `RESUME` · `SEEK` · `COMPLETE` · `SKIP` · `REPLAY` · `BUFFER` |
| `RecommendationSource`| `CONTENT_BASED` · `COLLABORATIVE` · `ML_MODEL` · `GENRE_BASED` · `MANUAL` |
| `DeviceType`          | `WEB` · `MOBILE_IOS` · `MOBILE_ANDROID` · `DESKTOP`                  |
| `AudioVariantStatus`  | `PROCESSING` · `READY` · `FAILED`                                     |
| `AdminActionTaken`    | `NONE` · `SONG_HIDDEN` · `SONG_DELETED` · `COMMENT_DELETED` · `USER_RESTRICTED` · `USER_DEACTIVATED` |
| `CollectionType`      | `COLLECTION` · `ALBUM_WISHLIST`                                       |
