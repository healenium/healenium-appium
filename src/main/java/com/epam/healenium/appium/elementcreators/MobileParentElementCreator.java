package com.epam.healenium.appium.elementcreators;

import com.epam.healenium.appium.elementcreators.attribute.ContentDescElementCreator;
import com.epam.healenium.appium.elementcreators.attribute.MobileIdElementCreator;
import com.epam.healenium.appium.elementcreators.attribute.ResourceIdElementCreator;
import com.epam.healenium.appium.elementcreators.attribute.TextElementCreator;
import com.epam.healenium.elementcreators.ElementCreator;
import com.epam.healenium.treecomparing.Node;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MobileParentElementCreator implements ElementCreator {

    private final MobileTagElementCreator classElementCreator = new MobileTagElementCreator();
    private final MobileIdElementCreator idElementCreator = new MobileIdElementCreator();
    private final ResourceIdElementCreator resourceIdElementCreator = new ResourceIdElementCreator();
    private final TextElementCreator textCreator = new TextElementCreator();
    private final ContentDescElementCreator contentDescElementCreator = new ContentDescElementCreator();

    @Override
    public String create(Node node) {
        Node parent = node.getParent();
        if (parent == null) {
            return "";
        }
        return Stream.of(classElementCreator, resourceIdElementCreator, idElementCreator, textCreator, contentDescElementCreator)
                .map(creator -> creator.create(parent))
                .filter(s -> !StringUtils.isEmpty(s))
                .collect(Collectors.joining())
                .concat("/");
    }
}
