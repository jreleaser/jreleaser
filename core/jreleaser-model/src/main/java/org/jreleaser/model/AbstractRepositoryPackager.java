/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
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
package org.jreleaser.model;

import java.util.Map;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class AbstractRepositoryPackager<S extends AbstractRepositoryPackager<S>> extends AbstractTemplatePackager<S> implements RepositoryPackager {
    protected final CommitAuthor commitAuthor = new CommitAuthor();

    protected AbstractRepositoryPackager(String type) {
        super(type);
    }

    @Override
    public void freeze() {
        super.freeze();
        commitAuthor.freeze();
    }

    @Override
    public void merge(S packager) {
        freezeCheck();
        super.merge(packager);
        setCommitAuthor(packager.commitAuthor);
    }

    @Override
    public CommitAuthor getCommitAuthor() {
        return commitAuthor;
    }

    @Override
    public void setCommitAuthor(CommitAuthor commitAuthor) {
        this.commitAuthor.merge(commitAuthor);
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        super.asMap(full, props);
        props.put("commitAuthor", commitAuthor.asMap(full));
    }
}
