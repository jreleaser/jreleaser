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
import org.jreleaser.model.Active;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.Stereotype;
import org.jreleaser.model.internal.common.AbstractModelObject;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.common.Domain;
import org.jreleaser.model.internal.common.FileSet;
import org.jreleaser.model.internal.common.Glob;
import org.jreleaser.mustache.TemplateContext;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.jreleaser.model.api.assemble.DebAssembler.TYPE;
import static org.jreleaser.mustache.Templates.resolveTemplate;

/**
 * @author Andres Almiray
 * @since 1.16.0
 */
public final class DebAssembler extends AbstractAssembler<org.jreleaser.model.internal.assemble.DebAssembler, org.jreleaser.model.api.assemble.DebAssembler> {
    private static final long serialVersionUID = 8479646443910628265L;

    private final Control control = new Control();
    private final Distribution.DistributionType distributionType = Distribution.DistributionType.NATIVE_PACKAGE;

    private String executable;
    private String installationPath;
    private String architecture;
    private String assemblerRef;

    @JsonIgnore
    private final org.jreleaser.model.api.assemble.DebAssembler immutable = new org.jreleaser.model.api.assemble.DebAssembler() {
        private static final long serialVersionUID = -3178284422300452517L;

        private Set<? extends org.jreleaser.model.api.common.Artifact> artifacts;
        private List<? extends org.jreleaser.model.api.common.FileSet> fileSets;
        private List<? extends org.jreleaser.model.api.common.Glob> files;
        private Set<? extends org.jreleaser.model.api.common.Artifact> outputs;

        @Override
        public String getExecutable() {
            return executable;
        }

        @Override
        public String getInstallationPath() {
            return installationPath;
        }

        @Override
        public String getArchitecture() {
            return architecture;
        }

        @Override
        public String getAssemblerRef() {
            return assemblerRef;
        }

        @Override
        public Control getControl() {
            return control.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.platform.Platform getPlatform() {
            return DebAssembler.this.getPlatform().asImmutable();
        }

        @Override
        public Distribution.DistributionType getDistributionType() {
            return DebAssembler.this.getDistributionType();
        }

        @Override
        public String getType() {
            return DebAssembler.this.getType();
        }

        @Override
        public Stereotype getStereotype() {
            return DebAssembler.this.getStereotype();
        }

        @Override
        public boolean isExported() {
            return DebAssembler.this.isExported();
        }

        @Override
        public String getName() {
            return DebAssembler.this.getName();
        }

        @Override
        public String getTemplateDirectory() {
            return DebAssembler.this.getTemplateDirectory();
        }

        @Override
        public Set<String> getSkipTemplates() {
            return unmodifiableSet(DebAssembler.this.getSkipTemplates());
        }

        @Override
        public Set<? extends org.jreleaser.model.api.common.Artifact> getArtifacts() {
            if (null == artifacts) {
                artifacts = DebAssembler.this.getArtifacts().stream()
                    .map(Artifact::asImmutable)
                    .collect(toSet());
            }
            return artifacts;
        }

        @Override
        public List<? extends org.jreleaser.model.api.common.FileSet> getFileSets() {
            if (null == fileSets) {
                fileSets = DebAssembler.this.getFileSets().stream()
                    .map(FileSet::asImmutable)
                    .collect(toList());
            }
            return fileSets;
        }

        @Override
        public List<? extends org.jreleaser.model.api.common.Glob> getFiles() {
            if (null == files) {
                files = DebAssembler.this.getFiles().stream()
                    .map(Glob::asImmutable)
                    .collect(toList());
            }
            return files;
        }

        @Override
        public Set<? extends org.jreleaser.model.api.common.Artifact> getOutputs() {
            if (null == outputs) {
                outputs = DebAssembler.this.getOutputs().stream()
                    .map(Artifact::asImmutable)
                    .collect(toSet());
            }
            return outputs;
        }

        @Override
        public Active getActive() {
            return DebAssembler.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return DebAssembler.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(DebAssembler.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return DebAssembler.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(DebAssembler.this.getExtraProperties());
        }
    };

    public DebAssembler() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.assemble.DebAssembler asImmutable() {
        return immutable;
    }

    @Override
    public void merge(DebAssembler source) {
        super.merge(source);
        this.executable = merge(executable, source.executable);
        this.installationPath = merge(installationPath, source.installationPath);
        this.architecture = merge(architecture, source.architecture);
        this.assemblerRef = merge(assemblerRef, source.assemblerRef);
        setControl(source.control);
    }

    @Override
    public Distribution.DistributionType getDistributionType() {
        return distributionType;
    }

    public String getExecutable() {
        return executable;
    }

    public void setExecutable(String executable) {
        this.executable = executable;
    }

    public String getInstallationPath() {
        return installationPath;
    }

    public void setInstallationPath(String installationPath) {
        this.installationPath = installationPath;
    }

    public String getArchitecture() {
        return architecture;
    }

    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }

    public String getAssemblerRef() {
        return assemblerRef;
    }

    public void setAssemblerRef(String assemblerRef) {
        this.assemblerRef = assemblerRef;
    }

    public Control getControl() {
        return control;
    }

    public void setControl(Control control) {
        this.control.merge(control);
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("installationPath", installationPath);
        props.put("architecture", architecture);
        props.put("assemblerRef", assemblerRef);
        props.put("executable", executable);
        props.put("control", control.asMap(full));
    }

    public String getResolvedInstallationPath(TemplateContext props) {
        TemplateContext newProps = new TemplateContext(props);
        newProps.set("packageName", control.getPackageName());
        newProps.set("packageVersion", control.getPackageVersion());
        newProps.set("packageRevision", control.getPackageRevision());
        return resolveTemplate(installationPath, newProps);
    }

    public static final class Control extends AbstractModelObject<DebAssembler.Control> implements Domain {
        private static final long serialVersionUID = -8048356548178456591L;

        private final Set<String> depends = new LinkedHashSet<>();
        private final Set<String> preDepends = new LinkedHashSet<>();
        private final Set<String> recommends = new LinkedHashSet<>();
        private final Set<String> suggests = new LinkedHashSet<>();
        private final Set<String> enhances = new LinkedHashSet<>();
        private final Set<String> breaks = new LinkedHashSet<>();
        private final Set<String> conflicts = new LinkedHashSet<>();

        private String packageName;
        private String packageVersion;
        private Integer packageRevision;
        private String maintainer;
        private String provides;
        private org.jreleaser.model.api.assemble.DebAssembler.Section section;
        private org.jreleaser.model.api.assemble.DebAssembler.Priority priority;
        private Boolean essential;
        private String description;
        private String homepage;
        private String builtUsing;

        @JsonIgnore
        private final org.jreleaser.model.api.assemble.DebAssembler.Control immutable = new org.jreleaser.model.api.assemble.DebAssembler.Control() {
            private static final long serialVersionUID = -2200047957145966911L;

            @Override
            public String getPackageName() {
                return packageName;
            }

            @Override
            public String getPackageVersion() {
                return packageVersion;
            }

            @Override
            public Integer getPackageRevision() {
                return packageRevision;
            }

            @Override
            public String getMaintainer() {
                return maintainer;
            }

            @Override
            public String getProvides() {
                return provides;
            }

            @Override
            public org.jreleaser.model.api.assemble.DebAssembler.Section getSection() {
                return section;
            }

            @Override
            public org.jreleaser.model.api.assemble.DebAssembler.Priority getPriority() {
                return priority;
            }

            @Override
            public boolean isEssential() {
                return Control.this.isEssential();
            }

            @Override
            public String getDescription() {
                return description;
            }

            @Override
            public String getHomepage() {
                return homepage;
            }

            @Override
            public String getBuiltUsing() {
                return builtUsing;
            }

            @Override
            public Set<String> getDepends() {
                return unmodifiableSet(depends);
            }

            @Override
            public Set<String> getPreDepends() {
                return unmodifiableSet(preDepends);
            }

            @Override
            public Set<String> getRecommends() {
                return unmodifiableSet(recommends);
            }

            @Override
            public Set<String> getSuggests() {
                return unmodifiableSet(suggests);
            }

            @Override
            public Set<String> getEnhances() {
                return unmodifiableSet(enhances);
            }

            @Override
            public Set<String> getBreaks() {
                return unmodifiableSet(breaks);
            }

            @Override
            public Set<String> getConflicts() {
                return unmodifiableSet(conflicts);
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(DebAssembler.Control.this.asMap(full));
            }
        };

        public org.jreleaser.model.api.assemble.DebAssembler.Control asImmutable() {
            return immutable;
        }

        @Override
        public void merge(DebAssembler.Control source) {
            this.packageName = merge(packageName, source.packageName);
            this.packageVersion = merge(packageVersion, source.packageVersion);
            this.packageRevision = merge(packageRevision, source.packageRevision);
            this.maintainer = merge(maintainer, source.maintainer);
            this.provides = merge(provides, source.provides);
            this.section = merge(section, source.section);
            this.priority = merge(priority, source.priority);
            this.essential = merge(essential, source.essential);
            this.description = merge(description, source.description);
            this.homepage = merge(homepage, source.homepage);
            this.builtUsing = merge(builtUsing, source.builtUsing);
            setDepends(merge(depends, source.depends));
            setPreDepends(merge(preDepends, source.preDepends));
            setRecommends(merge(recommends, source.recommends));
            setSuggests(merge(suggests, source.suggests));
            setEnhances(merge(enhances, source.enhances));
            setBreaks(merge(breaks, source.breaks));
            setConflicts(merge(conflicts, source.conflicts));
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public String getPackageVersion() {
            return packageVersion;
        }

        public void setPackageVersion(String packageVersion) {
            this.packageVersion = packageVersion;
        }

        public Integer getPackageRevision() {
            return null != packageRevision ? packageRevision : 1;
        }

        public void setPackageRevision(Integer packageRevision) {
            this.packageRevision = packageRevision;
        }

        public String getMaintainer() {
            return maintainer;
        }

        public void setMaintainer(String maintainer) {
            this.maintainer = maintainer;
        }

        public String getProvides() {
            return provides;
        }

        public void setProvides(String provides) {
            this.provides = provides;
        }

        public org.jreleaser.model.api.assemble.DebAssembler.Section getSection() {
            return section;
        }

        public void setSection(org.jreleaser.model.api.assemble.DebAssembler.Section section) {
            this.section = section;
        }

        public void setSection(String section) {
            setSection(org.jreleaser.model.api.assemble.DebAssembler.Section.of(section));
        }

        public org.jreleaser.model.api.assemble.DebAssembler.Priority getPriority() {
            return priority;
        }

        public void setPriority(org.jreleaser.model.api.assemble.DebAssembler.Priority priority) {
            this.priority = priority;
        }

        public void setPriority(String priority) {
            setPriority(org.jreleaser.model.api.assemble.DebAssembler.Priority.of(priority));
        }

        public Boolean isEssential() {
            return null != essential && essential;
        }

        public Boolean isEssentialSet() {
            return null != essential;
        }

        public void setEssential(Boolean essential) {
            this.essential = essential;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getHomepage() {
            return homepage;
        }

        public void setHomepage(String homepage) {
            this.homepage = homepage;
        }

        public String getBuiltUsing() {
            return builtUsing;
        }

        public void setBuiltUsing(String builtUsing) {
            this.builtUsing = builtUsing;
        }

        public Set<String> getDepends() {
            return depends;
        }

        public void setDepends(Set<String> depends) {
            this.depends.clear();
            this.depends.addAll(depends);
        }

        public Set<String> getPreDepends() {
            return preDepends;
        }

        public void setPreDepends(Set<String> preDepends) {
            this.preDepends.clear();
            this.preDepends.addAll(preDepends);
        }

        public Set<String> getRecommends() {
            return recommends;
        }

        public void setRecommends(Set<String> recommends) {
            this.recommends.clear();
            this.recommends.addAll(recommends);
        }

        public Set<String> getSuggests() {
            return suggests;
        }

        public void setSuggests(Set<String> suggests) {
            this.suggests.clear();
            this.suggests.addAll(suggests);
        }

        public Set<String> getEnhances() {
            return enhances;
        }

        public void setEnhances(Set<String> enhances) {
            this.enhances.clear();
            this.enhances.addAll(enhances);
        }

        public Set<String> getBreaks() {
            return breaks;
        }

        public void setBreaks(Set<String> breaks) {
            this.breaks.clear();
            this.breaks.addAll(breaks);
        }

        public Set<String> getConflicts() {
            return conflicts;
        }

        public void setConflicts(Set<String> conflicts) {
            this.conflicts.clear();
            this.conflicts.addAll(conflicts);
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("packageName", packageName);
            map.put("packageVersion", packageVersion);
            map.put("packageRevision", getPackageRevision());
            map.put("provides", provides);
            map.put("maintainer", maintainer);
            map.put("section", section);
            map.put("priority", priority);
            map.put("essential", isEssential());
            map.put("description", description);
            map.put("homepage", homepage);
            map.put("builtUsing", builtUsing);
            map.put("depends", depends);
            map.put("preDepends", preDepends);
            map.put("recommends", recommends);
            map.put("suggests", suggests);
            map.put("enhances", enhances);
            map.put("breaks", breaks);
            map.put("conflicts", conflicts);
            return map;
        }
    }
}
