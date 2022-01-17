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
package com.epam.healenium.converter;

import com.epam.healenium.MobileFieldName;
import com.epam.healenium.treecomparing.Node;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

import java.io.IOException;

public class MobileNodeSerializer extends JsonSerializer<Node> {

    @Override
    public void serializeWithType(Node value, JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
        WritableTypeId typeId = typeSer.typeId(value, Node.class, JsonToken.START_OBJECT);
        typeSer.writeTypePrefix(gen, typeId);
        serialize(value, gen, serializers);
        typeSer.writeTypeSuffix(gen, typeId);
    }

    @Override
    public void serialize(Node value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeStringField(MobileFieldName.TAG, value.getTag());
        gen.writeNumberField(MobileFieldName.INDEX, value.getIndex());
        gen.writeStringField(MobileFieldName.INNER_TEXT, value.getInnerText());
        gen.writeStringField(MobileFieldName.ID, value.getId());
        gen.writeStringField(MobileFieldName.CLASSES, String.join(" ", value.getClasses()));
        gen.writeObjectField(MobileFieldName.OTHER, value.getOtherAttributes());
        gen.writeEndObject();
        gen.flush();
    }

}
