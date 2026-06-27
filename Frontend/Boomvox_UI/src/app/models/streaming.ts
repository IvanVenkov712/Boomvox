import { SessionStatus, StreamingEventType } from './enums';

export interface StreamUrlResponse {
  sessionId: number;
  streamUrl: string;
  expiresAt: string;
}

export interface StreamingEventRequest {
  type: StreamingEventType;
  positionSec?: number;
  seekFromSec?: number;
  seekToSec?: number;
}

export interface SessionStatusRequest {
  status: SessionStatus;
}
