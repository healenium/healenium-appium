/**
 * Healenium-appium Copyright (C) 2019 EPAM
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
package com.epam.healenium.data;

import com.epam.healenium.treecomparing.Node;

import java.io.IOException;
import java.util.List;

/**
 * A storage interface that is responsible for persisting data necessary for locator recovery
 */
public interface PathStorage {

    /**
     * Persists the last valid DOM path of the locator
     *
     * @param locator persisted locator
     * @param context current page identifier
     * @param nodes   a list of path element nodes
     */
    void persistLastValidPath(Object locator, String context, List<Node> nodes);

    /**
     * @param locator recovery candidate locator
     * @param context current page identifier
     * @return a list of path element nodes or empty list if not persisted
     */
    List<Node> getLastValidPath(Object locator, String context);

    /**
     * Saving the locator to disk
     * @param data
     * @throws IOException if failed to write
     */
    void saveLocatorInfo(LocatorInfo data) throws IOException;

    /**
     * Check if given locator was previously persisted
     * @param locator recovery candidate locator
     * @param context current page identifier
     * @return
     */
    boolean isNodePathPersisted(Object locator, String context);
}
