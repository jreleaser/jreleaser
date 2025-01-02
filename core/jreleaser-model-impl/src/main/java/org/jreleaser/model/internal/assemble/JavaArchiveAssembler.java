/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2025 The JReleaser authors.
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
package org.jreleaser.model.internal.assemble;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.jreleaser.model.Active;
import org.jreleaser.model.Archive;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.Stereotype;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.catalog.swid.SwidTagAware;
import org.jreleaser.model.internal.common.AbstractModelObject;
import org.jreleaser.model.internal.common.ArchiveOptions;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.common.Domain;
import org.jreleaser.model.internal.common.EnvironmentVariables;
import org.jreleaser.model.internal.common.Executable;
import org.jreleaser.model.internal.common.FileSet;
import org.jreleaser.model.internal.common.Glob;
import org.jreleaser.model.internal.common.JvmOptions;
import org.jreleaser.mustache.TemplateContext;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.jreleaser.model.JReleaserOutput.nag;
import static org.jreleaser.model.api.assemble.JavaArchiveAssembler.TYPE;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.4.0
 */
public final class JavaArchiveAssembler extends AbstractAssembler<JavaArchiveAssembler, org.jreleaser.model.api.assemble.JavaArchiveAssembler> implements SwidTagAware {
    private static final long serialVersionUID = -7711922837441535536L;

    private final Set<Archive.Format> formats = new LinkedHashSet<>();
    private final ArchiveOptions options = new ArchiveOptions();
    private final List<Glob> jars = new ArrayList<>();
    private final Java java = new Java();
    private final Executable executable = new Executable();
    private final Artifact mainJar = new Artifact();

    private String archiveName;

    @JsonIgnore
    private final org.jreleaser.model.api.assemble.JavaArchiveAssembler immutable = new org.jreleaser.model.api.assemble.JavaArchiveAssembler() {
        private static final long serialVersionUID = -1343319275079561206L;

        private Set<? extends org.jreleaser.model.api.common.Artifact> artifacts;
        private List<? extends org.jreleaser.model.api.common.FileSet> fileSets;
        private Set<? extends org.jreleaser.model.api.common.Artifact> outputs;
        private List<? extends org.jreleaser.model.api.common.Glob> jars;
        private List<? extends org.jreleaser.model.api.common.Glob> files;

        @Override
        public String getArchiveName() {
            return archiveName;
        }

        @Override
        public Set<Archive.Format> getFormats() {
            return unmodifiableSet(formats);
        }

        @Override
        public org.jreleaser.model.api.common.ArchiveOptions getOptions() {
            return options.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.platform.Platform getPlatform() {
            return JavaArchiveAssembler.this.getPlatform().asImmutable();
        }

        @Override
        public org.jreleaser.model.api.catalog.swid.SwidTag getSwid() {
            return JavaArchiveAssembler.this.getSwid().asImmutable();
        }

        @Override
        public Distribution.DistributionType getDistributionType() {
            return JavaArchiveAssembler.this.getDistributionType();
        }

        @Override
        public String getType() {
            return JavaArchiveAssembler.this.getType();
        }

        @Override
        public Stereotype getStereotype() {
            return JavaArchiveAssembler.this.getStereotype();
        }

        @Override
        public boolean isExported() {
            return JavaArchiveAssembler.this.isExported();
        }

        @Override
        public String getName() {
            return JavaArchiveAssembler.this.getName();
        }

        @Override
        public String getTemplateDirectory() {
            return JavaArchiveAssembler.this.getTemplateDirectory();
        }

        @Override
        public Set<String> getSkipTemplates() {
            return unmodifiableSet(JavaArchiveAssembler.this.getSkipTemplates());
        }

        @Override
        public org.jreleaser.model.api.common.Executable getExecutable() {
            return executable.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.common.Artifact getMainJar() {
            return mainJar.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.assemble.JavaArchiveAssembler.Java getJava() {
            return java.asImmutable();
        }

        @Override
        public Set<? extends org.jreleaser.model.api.common.Artifact> getArtifacts() {
            if (null == artifacts) {
                artifacts = JavaArchiveAssembler.this.getArtifacts().stream()
                    .map(Artifact::asImmutable)
                    .collect(toSet());
            }
            return artifacts;
        }

        @Override
        public List<? extends org.jreleaser.model.api.common.FileSet> getFileSets() {
            if (null == fileSets) {
                fileSets = JavaArchiveAssembler.this.getFileSets().stream()
                    .map(FileSet::asImmutable)
                    .collect(toList());
            }
            return fileSets;
        }

        @Override
        public Set<? extends org.jreleaser.model.api.common.Artifact> getOutputs() {
            if (null == outputs) {
                outputs = JavaArchiveAssembler.this.getOutputs().stream()
                    .map(Artifact::asImmutable)
                    .collect(toSet());
            }
            return outputs;
        }

        @Override
        public List<? extends org.jreleaser.model.api.common.Glob> getJars() {
            if (null == jars) {
                jars = JavaArchiveAssembler.this.jars.stream()
                    .map(Glob::asImmutable)
                    .collect(toList());
            }
            return jars;
        }

        @Override
        public List<? extends org.jreleaser.model.api.common.Glob> getFiles() {
            if (null == files) {
                files = JavaArchiveAssembler.this.getFiles().stream()
                    .map(Glob::asImmutable)
                    .collect(toList());
            }
            return files;
        }

        @Override
        public Active getActive() {
            return JavaArchiveAssembler.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return JavaArchiveAssembler.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(JavaArchiveAssembler.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return JavaArchiveAssembler.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(JavaArchiveAssembler.this.getExtraProperties());
        }
    };

    public JavaArchiveAssembler() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.assemble.JavaArchiveAssembler asImmutable() {
        return immutable;
    }

    @Override
    public Distribution.DistributionType getDistributionType() {
        return Distribution.DistributionType.JAVA_BINARY;
    }

    @Override
    public void merge(JavaArchiveAssembler source) {
        super.merge(source);
        this.archiveName = merge(this.archiveName, source.archiveName);
        setFormats(merge(this.formats, source.formats));
        setOptions(source.options);
        setExecutable(source.executable);
        setJava(source.java);
        setMainJar(source.mainJar);
        setJars(merge(this.jars, source.jars));
    }

    public String getResolvedArchiveName(JReleaserContext context) {
        TemplateContext props = context.fullProps();
        props.setAll(props());
        return resolveTemplate(archiveName, props);
    }

    public String getArchiveName() {
        return archiveName;
    }

    public void setArchiveName(String archiveName) {
        this.archiveName = archiveName;
    }

    public Java getJava() {
        return java;
    }

    public void setJava(Java java) {
        this.java.merge(java);
    }

    public Executable getExecutable() {
        return executable;
    }

    public void setExecutable(Executable executable) {
        this.executable.merge(executable);
    }

    public Artifact getMainJar() {
        return mainJar;
    }

    public void setMainJar(Artifact mainJar) {
        this.mainJar.merge(mainJar);
    }

    public Set<Archive.Format> getFormats() {
        return formats;
    }

    public void setFormats(Set<Archive.Format> formats) {
        this.formats.clear();
        this.formats.addAll(formats);
    }

    public void addFormat(Archive.Format format) {
        this.formats.add(format);
    }

    public void addFormat(String str) {
        this.formats.add(Archive.Format.of(str));
    }

    public ArchiveOptions getOptions() {
        return options;
    }

    public void setOptions(ArchiveOptions options) {
        this.options.merge(options);
    }

    public List<Glob> getJars() {
        return jars;
    }

    public void setJars(List<Glob> jars) {
        this.jars.clear();
        this.jars.addAll(jars);
    }

    public void addJars(List<Glob> jars) {
        this.jars.addAll(jars);
    }

    public void addJar(Glob jar) {
        if (null != jar) {
            this.jars.add(jar);
        }
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("archiveName", archiveName);
        props.put("formats", formats);
        props.put("options", options.asMap(full));
        props.put("executable", executable.asMap(full));
        props.put("mainJar", mainJar.asMap(full));
        Map<String, Object> javaMap = java.asMap(full);
        if (!javaMap.isEmpty()) props.put("java", javaMap);
        Map<String, Map<String, Object>> mappedJars = new LinkedHashMap<>();
        for (int i = 0; i < jars.size(); i++) {
            mappedJars.put("glob " + i, jars.get(i).asMap(full));
        }
        props.put("jars", mappedJars);
    }

    public static final class Java extends AbstractModelObject<org.jreleaser.model.internal.assemble.JavaArchiveAssembler.Java> implements Domain {
        private static final long serialVersionUID = -4021694164319773392L;

        private final JvmOptions jvmOptions = new JvmOptions();
        private final EnvironmentVariables environmentVariables = new EnvironmentVariables();
        private final Set<String> options = new LinkedHashSet<>();
        private String mainModule;
        private String mainClass;

        @JsonIgnore
        private final org.jreleaser.model.api.assemble.JavaArchiveAssembler.Java immutable = new org.jreleaser.model.api.assemble.JavaArchiveAssembler.Java() {
            private static final long serialVersionUID = 1449677856982211221L;

            @Override
            public String getMainClass() {
                return mainClass;
            }

            @Override
            public String getMainModule() {
                return mainModule;
            }

            @Override
            public Set<String> getOptions() {
                return unmodifiableSet(options);
            }

            @Override
            public org.jreleaser.model.api.common.JvmOptions getJvmOptions() {
                return jvmOptions.asImmutable();
            }

            @Override
            public org.jreleaser.model.api.common.EnvironmentVariables getEnvironmentVariables() {
                return environmentVariables.asImmutable();
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(org.jreleaser.model.internal.assemble.JavaArchiveAssembler.Java.this.asMap(full));
            }
        };

        public org.jreleaser.model.api.assemble.JavaArchiveAssembler.Java asImmutable() {
            return immutable;
        }

        @Override
        public void merge(org.jreleaser.model.internal.assemble.JavaArchiveAssembler.Java source) {
            this.mainModule = merge(this.mainModule, source.mainModule);
            this.mainClass = merge(this.mainClass, source.mainClass);
            setOptions(merge(this.options, source.options));
            setJvmOptions(source.jvmOptions);
            setEnvironmentVariables(source.environmentVariables);
        }

        public String getMainClass() {
            return mainClass;
        }

        public void setMainClass(String mainClass) {
            this.mainClass = mainClass;
        }

        public String getMainModule() {
            return mainModule;
        }

        public void setMainModule(String mainModule) {
            this.mainModule = mainModule;
        }

        public Set<String> getOptions() {
            return options;
        }

        @JsonPropertyDescription("java.options is deprecated since 1.13.0 and will be removed in 2.0.0")
        public void setOptions(Set<String> options) {
            if (options.isEmpty()) return;
            nag("java.options is deprecated since 1.13.0 and will be removed in 2.0.0");
            this.options.clear();
            this.options.addAll(options);
        }

        public void addOptions(Set<String> options) {
            this.options.addAll(options);
        }

        public JvmOptions getJvmOptions() {
            return jvmOptions;
        }

        public void setJvmOptions(JvmOptions jvmOptions) {
            this.jvmOptions.merge(jvmOptions);
        }

        public EnvironmentVariables getEnvironmentVariables() {
            return environmentVariables;
        }

        public void setEnvironmentVariables(EnvironmentVariables environmentVariables) {
            this.environmentVariables.merge(environmentVariables);
        }

        public boolean isSet() {
            return isNotBlank(mainModule) ||
                isNotBlank(mainClass) ||
                !options.isEmpty() ||
                jvmOptions.isSet() ||
                environmentVariables.isSet();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> map = new LinkedHashMap<>();
            if (isNotBlank(mainModule)) map.put("mainModule", mainModule);
            if (isNotBlank(mainClass)) map.put("mainClass", mainClass);
            map.put("jvmOptions", jvmOptions.asMap(full));
            map.put("environmentVariables", environmentVariables.asMap(full));
            return map;
        }
    }
}
