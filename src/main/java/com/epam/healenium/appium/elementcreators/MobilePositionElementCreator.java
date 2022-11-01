package com.epam.healenium.appium.elementcreators;

import com.epam.healenium.elementcreators.ElementCreator;
import com.epam.healenium.treecomparing.Node;


public class MobilePositionElementCreator implements ElementCreator {

    @Override
    public String create(Node current) {
        Node parent = current.getParent();
        if (parent == null) {
            return "";
        }
        int i = 1;
        for (Node child : parent.getChildren()) {
            if (child.getTag().equals(current.getTag())) {
                if (child.getOtherAttributes().get("index").equals(current.getOtherAttributes().get("index"))) {
                    break;
                } else {
                    i++;
                }
            }
        }
        return String.format("[%s]", i);
    }

}
