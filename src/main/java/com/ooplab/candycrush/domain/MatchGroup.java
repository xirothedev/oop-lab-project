package com.ooplab.candycrush.domain;

import java.util.Set;

public record MatchGroup(Set<Position> positions, MatchPattern pattern, CandyColor color) {
}

