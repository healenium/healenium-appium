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
package com.epam.healenium.utils;

import com.epam.healenium.MobileSelfHealingEngine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Function;
import java.util.stream.Stream;

public class ResourceReader {

    public static <T> T readResource(String classpath, Function<Stream<String>, T> function) {
        ClassLoader classLoader = MobileSelfHealingEngine.class.getClassLoader();
        try (InputStream stream = classLoader.getResourceAsStream(classpath)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            return function.apply(reader.lines());
        } catch (IOException e) {
            throw new IllegalStateException("Could not initialize com.epam.healenium.engine", e);
        }
    }
}
