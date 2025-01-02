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
package org.jreleaser.model.internal.announce;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.Active;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.common.CommitAuthor;
import org.jreleaser.model.internal.common.CommitAuthorAware;
import org.jreleaser.model.internal.packagers.AbstractRepositoryTap;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toSet;
import static org.jreleaser.model.api.announce.ArticleAnnouncer.TYPE;

/**
 * @author Andres Almiray
 * @since 0.6.0
 */
public final class ArticleAnnouncer extends AbstractAnnouncer<ArticleAnnouncer, org.jreleaser.model.api.announce.ArticleAnnouncer> implements CommitAuthorAware {
    private static final long serialVersionUID = 8118441310808540594L;

    private final Set<Artifact> files = new LinkedHashSet<>();
    private final CommitAuthor commitAuthor = new CommitAuthor();
    private final Repository repository = new Repository();

    private String templateDirectory;

    @JsonIgnore
    private final org.jreleaser.model.api.announce.ArticleAnnouncer immutable = new org.jreleaser.model.api.announce.ArticleAnnouncer() {
        private static final long serialVersionUID = 6971332126023008307L;

        private Set<? extends org.jreleaser.model.api.common.Artifact> files;

        @Override
        public String getType() {
            return org.jreleaser.model.api.announce.ArticleAnnouncer.TYPE;
        }

        @Override
        public Set<? extends org.jreleaser.model.api.common.Artifact> getFiles() {
            if (null == files) {
                files = ArticleAnnouncer.this.files.stream()
                    .map(Artifact::asImmutable)
                    .collect(toSet());
            }
            return files;
        }

        @Override
        public Repository getRepository() {
            return repository.asImmutable();
        }

        @Override
        public String getTemplateDirectory() {
            return templateDirectory;
        }

        @Override
        public String getName() {
            return ArticleAnnouncer.this.getName();
        }

        @Override
        public boolean isSnapshotSupported() {
            return ArticleAnnouncer.this.isSnapshotSupported();
        }

        @Override
        public Active getActive() {
            return ArticleAnnouncer.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return ArticleAnnouncer.this.isEnabled();
        }

        @Override
        public org.jreleaser.model.api.common.CommitAuthor getCommitAuthor() {
            return commitAuthor.asImmutable();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(ArticleAnnouncer.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return ArticleAnnouncer.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(ArticleAnnouncer.this.getExtraProperties());
        }

        @Override
        public Integer getConnectTimeout() {
            return ArticleAnnouncer.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return ArticleAnnouncer.this.getReadTimeout();
        }
    };

    public ArticleAnnouncer() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.announce.ArticleAnnouncer asImmutable() {
        return immutable;
    }

    @Override
    public void merge(ArticleAnnouncer source) {
        super.merge(source);
        this.templateDirectory = merge(this.templateDirectory, source.templateDirectory);
        setFiles(merge(this.files, source.files));
        setCommitAuthor(source.commitAuthor);
        setRepository(source.repository);
    }

    public Set<Artifact> getFiles() {
        return Artifact.sortArtifacts(files);
    }

    public void setFiles(Set<Artifact> files) {
        this.files.clear();
        this.files.addAll(files);
    }

    public void addFiles(Set<Artifact> files) {
        this.files.addAll(files);
    }

    public void addFile(Artifact artifact) {
        if (null != artifact) {
            this.files.add(artifact);
        }
    }

    @Override
    public CommitAuthor getCommitAuthor() {
        return commitAuthor;
    }

    @Override
    public void setCommitAuthor(CommitAuthor commitAuthor) {
        this.commitAuthor.merge(commitAuthor);
    }

    public Repository getRepository() {
        return repository;
    }

    public void setRepository(Repository repository) {
        this.repository.merge(repository);
    }

    public String getTemplateDirectory() {
        return templateDirectory;
    }

    public void setTemplateDirectory(String templateDirectory) {
        this.templateDirectory = templateDirectory;
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("commitAuthor", commitAuthor.asMap(full));
        props.put("repository", repository.asMap(full));

        Map<String, Map<String, Object>> mappedArtifacts = new LinkedHashMap<>();
        int i = 0;
        for (Artifact artifact : getFiles()) {
            mappedArtifacts.put("files " + (i++), artifact.asMap(full));
        }
        props.put("files", mappedArtifacts);
        props.put("templateDirectory", templateDirectory);
    }

    public static final class Repository extends AbstractRepositoryTap<Repository> {
        private static final long serialVersionUID = -5607361534766759517L;

        @JsonIgnore
        private final org.jreleaser.model.api.announce.ArticleAnnouncer.Repository immutable = new org.jreleaser.model.api.announce.ArticleAnnouncer.Repository() {
            private static final long serialVersionUID = -5856312198018701678L;

            @Override
            public String getBasename() {
                return Repository.this.getBasename();
            }

            @Override
            public String getCanonicalRepoName() {
                return Repository.this.getCanonicalRepoName();
            }

            @Override
            public String getName() {
                return Repository.this.getName();
            }

            @Override
            public String getTagName() {
                return Repository.this.getTagName();
            }

            @Override
            public String getBranch() {
                return Repository.this.getBranch();
            }

            @Override
            public String getBranchPush() {
                return Repository.this.getBranchPush();
            }

            @Override
            public String getUsername() {
                return Repository.this.getUsername();
            }

            @Override
            public String getToken() {
                return Repository.this.getToken();
            }

            @Override
            public String getCommitMessage() {
                return Repository.this.getCommitMessage();
            }

            @Override
            public Active getActive() {
                return Repository.this.getActive();
            }

            @Override
            public boolean isEnabled() {
                return Repository.this.isEnabled();
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(Repository.this.asMap(full));
            }

            @Override
            public String getOwner() {
                return Repository.this.getOwner();
            }

            @Override
            public String getPrefix() {
                return Repository.this.prefix();
            }

            @Override
            public Map<String, Object> getExtraProperties() {
                return unmodifiableMap(Repository.this.getExtraProperties());
            }
        };

        public Repository() {
            super("article", "article");
        }

        public org.jreleaser.model.api.announce.ArticleAnnouncer.Repository asImmutable() {
            return immutable;
        }

        @Override
        public String prefix() {
            return "repository";
        }
    }
}
