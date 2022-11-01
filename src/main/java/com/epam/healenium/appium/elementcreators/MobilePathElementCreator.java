package com.epam.healenium.appium.elementcreators;

import com.epam.healenium.elementcreators.ElementCreator;
import com.epam.healenium.treecomparing.Node;

import java.util.ArrayDeque;
import java.util.Deque;

public class MobilePathElementCreator implements ElementCreator {

    private final MobilePositionElementCreator positionCreator = new MobilePositionElementCreator();

    @Override
    public String create(Node node) {
        Node current = node;
        Deque<String> path = new ArrayDeque<>();
        while (current != null) {
            String item = current.getTag();
            if (hasSimilarNeighbours(current)) {
                item += positionCreator.create(current);
            }
            path.addFirst(item);
            current = current.getParent();
        }
        return String.join("/", path);
    }

    private boolean hasSimilarNeighbours(Node current) {
        Node parent = current.getParent();
        if (parent == null) {
            return false;
        }
        return parent.getChildren()
                .stream()
                .map(Node::getTag)
                .filter(tag -> tag.equals(current.getTag()))
                .count() > 1L;
    }
}
