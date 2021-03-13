/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jreleaser.config.yaml;

import org.jreleaser.config.JReleaserConfigParser;
import org.jreleaser.model.Artifact;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.Plug;
import org.jreleaser.model.Slot;
import org.kordamp.jipsy.annotations.ServiceProviderFor;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@ServiceProviderFor(JReleaserConfigParser.class)
public class YamlJReleaserConfigParser implements JReleaserConfigParser {
    @Override
    public boolean supports(Path configFile) {
        String fileName = configFile.getFileName().toString();
        return fileName.endsWith(".yml") || fileName.endsWith(".yaml");
    }

    @Override
    public JReleaserModel parse(InputStream inputStream) throws IOException {
        Constructor c = new Constructor(JReleaserModel.class);
        TypeDescription td = new TypeDescription(JReleaserModel.class);
        td.addPropertyParameters("artifacts", Artifact.class);
        td.addPropertyParameters("plugs", Plug.class);
        td.addPropertyParameters("slots", Slot.class);
        c.addTypeDescription(td);

        c.setPropertyUtils(new PropertyUtils() {
            @Override
            public Property getProperty(Class<? extends Object> type, String name) {
                if (name.equals("class")) {
                    name = "clazz";
                }
                return super.getProperty(type, name);
            }
        });

        Yaml yaml = new Yaml(c);

        return yaml.load(inputStream);
    }
}
