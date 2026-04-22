package com.ooplab.candycrush.ui.animation;

import com.ooplab.candycrush.domain.Position;
import javafx.scene.layout.StackPane;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class CandyNodeManager {
    private final Map<Position, StackPane> nodes = new HashMap<>();

    public void register(Position position, StackPane node) {
        nodes.put(position, node);
    }

    public void unregister(Position position) {
        nodes.remove(position);
    }

    public StackPane getNode(Position position) {
        return nodes.get(position);
    }

    public boolean hasNode(Position position) {
        return nodes.containsKey(position);
    }

    public void swapPositions(Position a, Position b) {
        StackPane nodeA = nodes.get(a);
        StackPane nodeB = nodes.get(b);
        if (nodeA != null && nodeB != null) {
            nodes.put(a, nodeB);
            nodes.put(b, nodeA);
        }
    }

    public void updatePosition(Position oldPos, Position newPos, StackPane node) {
        nodes.remove(oldPos);
        nodes.put(newPos, node);
    }

    public Set<Position> allPositions() {
        return Collections.unmodifiableSet(nodes.keySet());
    }

    public void clear() {
        nodes.clear();
    }
}
