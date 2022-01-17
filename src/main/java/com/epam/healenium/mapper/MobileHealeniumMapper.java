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
package com.epam.healenium.mapper;

import com.epam.healenium.model.MobileHealingResultDto;
import com.epam.healenium.model.MobileLocator;
import com.epam.healenium.model.MobileRequestDto;
import com.epam.healenium.treecomparing.Node;
import com.epam.healenium.treecomparing.Scored;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.openqa.selenium.By;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MobileHealeniumMapper {

    default MobileRequestDto buildDto(By by, StackTraceElement element){
        String[] locatorParts = by.toString().split(":");
        MobileRequestDto dto = new MobileRequestDto()
                .setLocator(locatorParts[1].trim())
                .setType(locatorParts[0].trim());
        if(element != null){
            dto.setClassName(element.getClassName());
            dto.setMethodName(element.getMethodName());
        }
        return dto;
    }

    default MobileRequestDto buildDto(By by, StackTraceElement element, List<Node> nodePath){
        MobileRequestDto dto = buildDto(by, element);
        dto.setNodePath(nodePath);
        return dto;

    }

    default MobileRequestDto buildDto(By by, StackTraceElement element, String page, List<Scored<By>> healingResults, Scored<By> selected, byte[] screenshot){
        MobileRequestDto dto = buildDto(by, element);
        dto.setPageContent(page)
                .setResults(buildResultDto(healingResults))
                .setUsedResult(buildResultDto(selected))
                .setScreenshot(screenshot);
        return dto;
    }

    default MobileHealingResultDto buildResultDto(Scored<By> scored){
        return new MobileHealingResultDto(byToLocator(scored.getValue()), scored.getScore());
    }

    default List<MobileHealingResultDto> buildResultDto(Collection<Scored<By>> scored){
        return scored.stream().map(this::buildResultDto).collect(Collectors.toList());
    }

    default MobileLocator byToLocator(By by){
        String[] locatorParts = by.toString().split(":");
        return new MobileLocator(locatorParts[1].trim(), locatorParts[0].trim());
    }

    default List<MobileLocator> byToLocator(Collection<By> by){
        return by.stream().map(this::byToLocator).collect(Collectors.toList());
    }
}
