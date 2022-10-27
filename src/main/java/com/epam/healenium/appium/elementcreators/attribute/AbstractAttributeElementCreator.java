package com.epam.healenium.appium.elementcreators.attribute;

import com.epam.healenium.elementcreators.ElementCreator;
import com.epam.healenium.treecomparing.Node;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public abstract class AbstractAttributeElementCreator implements ElementCreator {

    @Override
    public String create(Node node) {
        return Optional.ofNullable(node.getOtherAttributes().get(getFieldName()))
                .filter(StringUtils::isNotBlank)
                .map(a -> String.format("[@%s='%s']", getFieldName(), a))
                .orElse("");
    }

    protected abstract String getFieldName();
}
