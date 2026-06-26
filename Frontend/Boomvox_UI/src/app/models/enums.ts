export type UserRole = 'ADMIN' | 'AUTHOR' | 'ORDINARY_USER' | 'GUEST';

export type Genre = 'METAL' | 'POP_FOLK' | 'POP' | 'ROCK' | 'COUNTRY' | 'TECHNO';

export type SongFormat = 'MP3' | 'WAV' | 'AAC';

export type SongProcessingStatus = 'PROCESSING' | 'ACTIVE' | 'FAILED';

export type SessionStatus = 'ACTIVE' | 'COMPLETED' | 'INTERRUPTED' | 'SKIPPED';

export type StreamingEventType = 'PAUSE' | 'RESUME' | 'SEEK' | 'REPLAY';
