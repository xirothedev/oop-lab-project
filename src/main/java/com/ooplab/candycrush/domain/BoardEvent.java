package com.ooplab.candycrush.domain;

import java.util.Map;

public record BoardEvent(BoardEventType type, Map<String, Object> payload) {
}