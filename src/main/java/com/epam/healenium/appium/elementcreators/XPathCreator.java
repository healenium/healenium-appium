/**
 * Healenium-web Copyright (C) 2019 EPAM
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.epam.healenium.appium.elementcreators;

import com.epam.healenium.engine.elementcreators.ElementCreator;
import com.epam.healenium.treecomparing.Node;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class XPathCreator implements ElementCreator {

    @Override
    public String create(Node node) {
        Node current = node;
        Deque<String> path = new ArrayDeque<>();

        while (current != null) {
            String item = current.getTag();
            String id = current.getId();
            String resourceId = current.getOtherAttributes().getOrDefault("resource-id", "");
            String text = current.getOtherAttributes().getOrDefault("text", "");
            if (!StringUtils.isEmpty(id)) {
                item += "[@id = '" + id + "']";
            } else if (!StringUtils.isEmpty(resourceId)) {
                item += "[@resource-id = '" + resourceId + "']";
            } else if (!StringUtils.isEmpty(text)) {
                item += "[@text = '" + text + "']";
            }
            path.addFirst(item);
            if (!StringUtils.isEmpty(id) || !StringUtils.isEmpty(resourceId)) {
                break;
            }
            current = current.getParent();
        }
        String result = path.stream().collect(Collectors.joining("/", "//", ""));
        log.debug("Node selector: {}", result);
        return result;
    }
}
