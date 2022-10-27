package com.epam.healenium.appium;

import com.epam.healenium.appium.elementcreators.MobileParentElementCreator;
import com.epam.healenium.appium.elementcreators.MobilePathElementCreator;
import com.epam.healenium.appium.elementcreators.MobilePositionElementCreator;
import com.epam.healenium.appium.elementcreators.MobileTagElementCreator;
import com.epam.healenium.appium.elementcreators.attribute.ContentDescElementCreator;
import com.epam.healenium.appium.elementcreators.attribute.MobileIdElementCreator;
import com.epam.healenium.appium.elementcreators.attribute.ResourceIdElementCreator;
import com.epam.healenium.appium.elementcreators.attribute.TextElementCreator;
import com.epam.healenium.elementcreators.ElementCreator;
import com.epam.healenium.treecomparing.Node;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum MobileSelectorComponent {

    MOBILE_PATH(new MobilePathElementCreator()),
    MOBILE_PARENT(new MobileParentElementCreator()),
    MOBILE_TAG(new MobileTagElementCreator()),
    RESOURCE_ID(new ResourceIdElementCreator()),
    MOBILE_ID(new MobileIdElementCreator()),
    TEXT(new TextElementCreator()),
    CONTENT_DESC(new ContentDescElementCreator()),
    MOBILE_POSITION(new MobilePositionElementCreator());

    private final ElementCreator elementCreator;

    public String createComponent(Node node) {
        return elementCreator.create(node);
    }
}
