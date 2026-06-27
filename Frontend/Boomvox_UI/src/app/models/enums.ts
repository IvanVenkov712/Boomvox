export enum UserRole {
  ADMIN = 'ADMIN',
  AUTHOR = 'AUTHOR',
  ORDINARY_USER = 'ORDINARY_USER',
}

export enum Genre {
  METAL = 'METAL',
  POP_FOLK = 'POP_FOLK',
  POP = 'POP',
  ROCK = 'ROCK',
  COUNTRY = 'COUNTRY',
  TECHNO = 'TECHNO',
}

export enum SongFormat {
  MP3 = 'MP3',
  WAV = 'WAV',
  AAC = 'AAC',
}

export enum SongProcessingStatus {
  PROCESSING = 'PROCESSING',
  ACTIVE = 'ACTIVE',
  FAILED = 'FAILED',
}

export enum SessionStatus {
  ACTIVE = 'ACTIVE',
  COMPLETED = 'COMPLETED',
  INTERRUPTED = 'INTERRUPTED',
  SKIPPED = 'SKIPPED',
}

export enum StreamingEventType {
  PAUSE = 'PAUSE',
  RESUME = 'RESUME',
  SEEK = 'SEEK',
  REPLAY = 'REPLAY',
}
