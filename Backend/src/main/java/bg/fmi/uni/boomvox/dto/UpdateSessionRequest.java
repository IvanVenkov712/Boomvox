package bg.fmi.uni.boomvox.dto;

import bg.fmi.uni.boomvox.enums.SessionStatus;

public record UpdateSessionRequest(SessionStatus status) {}

