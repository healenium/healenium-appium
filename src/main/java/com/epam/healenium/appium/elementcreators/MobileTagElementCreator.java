package com.epam.healenium.appium.elementcreators;


import com.epam.healenium.elementcreators.ElementCreator;
import com.epam.healenium.treecomparing.Node;

public class MobileTagElementCreator implements ElementCreator {

    @Override
    public String create(Node node) {
        return node.getTag();
    }

}
