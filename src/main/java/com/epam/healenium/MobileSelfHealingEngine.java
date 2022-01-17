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
package com.epam.healenium;

import com.epam.healenium.data.FileSystemPathStorage;
import com.epam.healenium.data.LocatorInfo;
import com.epam.healenium.data.PathStorage;
import com.epam.healenium.treecomparing.*;
import com.typesafe.config.Config;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/**
 * A healing com.epam.healenium.engine that encapsulates all the healing logic, leaving the persistence to {@link PathStorage} abstraction
 * and elements and locators handling to the driver.
 */
@Slf4j
public abstract class MobileSelfHealingEngine<D,E> {

    /**
     * A JavaScript source to extract an HTML item with its attributes
     */
    private final Config config;
    private final D webDriver;
    private final PathStorage storage;
    private final int recoveryTries;
    private final double scoreCap;

    public Config getConfig() {
        return config;
    }

    public D getWebDriver() {
        return webDriver;
    }

    public MobileSelfHealingEngine(D delegate, Config config) {
        this.webDriver = delegate;
        this.config = config;
        this.storage = new FileSystemPathStorage(config);
        this.recoveryTries = config.getInt("recovery-tries");
        this.scoreCap = config.getDouble("score-cap");
    }

    /**
     * Stores the valid locator state: the element it found and the page.
     *
     * @param locator    the locator
     * @param context
     * @param webElement the element while it is still accessible by the locator
     */
    public void savePath(Object locator, String context, E webElement) {
        log.info("* savePath start: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
        List<Node> nodePath = getNodePath(webElement);
        storage.persistLastValidPath(locator, context, nodePath);
        log.info("* savePath finish: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
    }

    @SneakyThrows
    public void saveLocator(LocatorInfo info) {
        storage.saveLocatorInfo(info);
    }

    public boolean isPathExists(Object locator, String context){
        return storage.isNodePathPersisted(locator, context);
    }

    /**
     *
     * @param element
     * @return
     */
    public abstract List<Node> getNodePath(E element);

    /**
     * Get available document parser
     * @return
     */
    public abstract DocumentParser getParser();

    /**
     * @param targetPage the new HTML page source on which we should search for the element
     * @return a list of candidate locators, ordered by revelance, or empty list if was unable to heal
     */
    protected List<Scored<Node>> findNewNodes(Object locator, String context, String targetPage) {
        List<Node> nodes = storage.getLastValidPath(locator, context);
        if (nodes.isEmpty()) {
            return Collections.emptyList();
        }
        return findNearest(nodes.toArray(new Node[0]), targetPage);
    }

    /**
     * @param targetPage the new HTML page source on which we should search for the element
     * @return a list of candidate locators, ordered by revelance, or empty list if was unable to heal
     */
    protected List<Scored<Node>> findNewNodes(String targetPage, List<Node> nodes) {
        if (nodes.isEmpty()) {
            return Collections.emptyList();
        }
        log.info("!!! findNewNodes - nodes not empty\n");
        return findNearest(nodes.toArray(new Node[0]), targetPage);
    }

    /**
     * @param nodePath        the array of nodes which actually represent the full path of an element in HTML tree,
     *                        ordered from deepest to shallowest
     * @param destinationTree the HTML code of the current page
     * @return a list of nodes which are the candidates to be the searched element, ordered by relevance descending.
     */
    private List<Scored<Node>> findNearest(Node[] nodePath, String destinationTree) {
        Node destination = parseTree(destinationTree);
        PathFinder pathFinder =
                new PathFinder(new LCSPathDistance(), new HeuristicNodeDistance());
        return pathFinder.find(new Path(nodePath), destination, recoveryTries);
    }

    /**
     *
     * @param tree
     * @return
     */
    private Node parseTree(String tree) {
        return getParser().parse(new ByteArrayInputStream(tree.getBytes(StandardCharsets.UTF_8)));
    }
}
