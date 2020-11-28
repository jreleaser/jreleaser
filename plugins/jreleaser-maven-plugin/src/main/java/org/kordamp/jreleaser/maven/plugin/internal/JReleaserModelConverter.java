/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020 Andres Almiray.
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
package org.kordamp.jreleaser.maven.plugin.internal;

import org.kordamp.jreleaser.maven.plugin.Artifact;
import org.kordamp.jreleaser.maven.plugin.Brew;
import org.kordamp.jreleaser.maven.plugin.Chocolatey;
import org.kordamp.jreleaser.maven.plugin.Distribution;
import org.kordamp.jreleaser.maven.plugin.Jreleaser;
import org.kordamp.jreleaser.maven.plugin.Packagers;
import org.kordamp.jreleaser.maven.plugin.Plug;
import org.kordamp.jreleaser.maven.plugin.Project;
import org.kordamp.jreleaser.maven.plugin.Release;
import org.kordamp.jreleaser.maven.plugin.Scoop;
import org.kordamp.jreleaser.maven.plugin.Slot;
import org.kordamp.jreleaser.maven.plugin.Snap;
import org.kordamp.jreleaser.model.JReleaserModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class JReleaserModelConverter {
    private JReleaserModelConverter() {
        // noop
    }

    public static JReleaserModel convert(Jreleaser jreleaser) {
        JReleaserModel jReleaserModel = new JReleaserModel();
        jReleaserModel.setProject(convertProject(jreleaser.getProject()));
        jReleaserModel.setRelease(convertRelease(jreleaser.getRelease()));
        jReleaserModel.setPackagers(convertPackagers(jreleaser.getPackagers()));
        jReleaserModel.setDistributions(convertDistributions(jReleaserModel, jreleaser.getDistributions()));
        return jReleaserModel;
    }

    private static org.kordamp.jreleaser.model.Project convertProject(Project project) {
        org.kordamp.jreleaser.model.Project p = new org.kordamp.jreleaser.model.Project();
        p.setName(project.getName());
        p.setVersion(project.getVersion());
        p.setDescription(project.getDescription());
        p.setLongDescription(project.getLongDescription());
        p.setWebsite(project.getWebsite());
        p.setLicense(project.getLicense());
        p.setJavaVersion(project.getJavaVersion());
        p.setTags(project.getTags());
        p.setAuthors(project.getAuthors());
        p.setExtraProperties(project.getExtraProperties());
        return p;
    }

    private static org.kordamp.jreleaser.model.Release convertRelease(Release release) {
        org.kordamp.jreleaser.model.Release r = new org.kordamp.jreleaser.model.Release();
        if (null != release.getRepoType()) r.setRepoType(release.getRepoType().name());
        r.setRepoOwner(release.getRepoOwner());
        r.setRepoName(release.getReleaseNotesUrlFormat());
        r.setDownloadUrlFormat(release.getDownloadUrlFormat());
        r.setReleaseNotesUrlFormat(release.getReleaseNotesUrlFormat());
        r.setLatestReleaseUrlFormat(release.getLatestReleaseUrlFormat());
        r.setIssueTrackerUrlFormat(release.getIssueTrackerUrlFormat());
        r.setAuthorization(release.getAuthorization());
        r.setTagName(release.getTagName());
        r.setTargetCommitish(release.getTargetCommitish());
        r.setReleaseName(release.getReleaseName());
        r.setBody(release.getBody());
        r.setDraft(release.isDraft());
        r.setPrerelease(release.isPrerelease());
        r.setOverwrite(release.isOverwrite());
        r.setAllowUploadToExisting(release.isAllowUploadToExisting());
        r.setApiEndpoint(release.getApiEndpoint());
        return r;
    }

    private static org.kordamp.jreleaser.model.Packagers convertPackagers(Packagers packagers) {
        org.kordamp.jreleaser.model.Packagers p = new org.kordamp.jreleaser.model.Packagers();
        if (packagers.getBrew().isSet()) p.setBrew(convertBrew(packagers.getBrew()));
        if (packagers.getChocolatey().isSet()) p.setChocolatey(convertChocolatey(packagers.getChocolatey()));
        if (packagers.getScoop().isSet()) p.setScoop(convertScoop(packagers.getScoop()));
        if (packagers.getSnap().isSet()) p.setSnap(convertSnap(packagers.getSnap()));
        return p;
    }

    private static Map<String, org.kordamp.jreleaser.model.Distribution> convertDistributions(JReleaserModel model, List<Distribution> distributions) {
        Map<String, org.kordamp.jreleaser.model.Distribution> ds = new LinkedHashMap<>();
        for (Distribution distribution : distributions) {
            ds.put(distribution.getName(), convertDistribution(model, distribution));
        }
        return ds;
    }

    private static org.kordamp.jreleaser.model.Distribution convertDistribution(JReleaserModel model, Distribution distribution) {
        org.kordamp.jreleaser.model.Distribution d = new org.kordamp.jreleaser.model.Distribution();
        d.setName(distribution.getName());
        d.setType(distribution.getType().name());
        d.setExecutable(distribution.getExecutable());
        d.setJavaVersion(distribution.getJavaVersion());
        d.setTags(distribution.getTags());
        d.setExtraProperties(distribution.getExtraProperties());
        d.setArtifacts(convertArtifacts(distribution.getArtifacts()));

        if (distribution.getBrew().isSet()) d.setBrew(convertBrew(distribution.getBrew()));
        if (distribution.getChocolatey().isSet()) d.setChocolatey(convertChocolatey(distribution.getChocolatey()));
        if (distribution.getScoop().isSet()) d.setScoop(convertScoop(distribution.getScoop()));
        if (distribution.getSnap().isSet()) d.setSnap(convertSnap(distribution.getSnap()));

        return d;
    }

    private static List<org.kordamp.jreleaser.model.Artifact> convertArtifacts(List<Artifact> artifacts) {
        List<org.kordamp.jreleaser.model.Artifact> as = new ArrayList<>();
        for (Artifact artifact : artifacts) {
            as.add(convertPlug(artifact));
        }
        return as;
    }

    private static org.kordamp.jreleaser.model.Artifact convertPlug(Artifact artifact) {
        org.kordamp.jreleaser.model.Artifact a = new org.kordamp.jreleaser.model.Artifact();
        a.setPath(artifact.getPath());
        a.setHash(artifact.getHash());
        a.setOsClassifier(artifact.getOsClassifier());
        a.setJavaVersion(artifact.getJavaVersion());
        return a;
    }

    private static org.kordamp.jreleaser.model.Brew convertBrew(Brew brew) {
        org.kordamp.jreleaser.model.Brew t = new org.kordamp.jreleaser.model.Brew();
        if (brew.isEnabledSet()) t.setEnabled(brew.isEnabled());
        t.setTemplateDirectory(brew.getTemplateDirectory());
        t.setExtraProperties(brew.getExtraProperties());
        t.setDependencies(brew.getDependencies());
        return t;
    }

    private static org.kordamp.jreleaser.model.Chocolatey convertChocolatey(Chocolatey chocolatey) {
        org.kordamp.jreleaser.model.Chocolatey t = new org.kordamp.jreleaser.model.Chocolatey();
        if (chocolatey.isEnabledSet()) t.setEnabled(chocolatey.isEnabled());
        t.setTemplateDirectory(chocolatey.getTemplateDirectory());
        t.setExtraProperties(chocolatey.getExtraProperties());
        return t;
    }

    private static org.kordamp.jreleaser.model.Scoop convertScoop(Scoop scoop) {
        org.kordamp.jreleaser.model.Scoop t = new org.kordamp.jreleaser.model.Scoop();
        if (scoop.isEnabledSet()) t.setEnabled(scoop.isEnabled());
        t.setTemplateDirectory(scoop.getTemplateDirectory());
        t.setExtraProperties(scoop.getExtraProperties());
        t.setCheckverUrl(scoop.getCheckverUrl());
        t.setAutoupdateUrl(scoop.getAutoupdateUrl());
        return t;
    }

    private static org.kordamp.jreleaser.model.Snap convertSnap(Snap snap) {
        org.kordamp.jreleaser.model.Snap t = new org.kordamp.jreleaser.model.Snap();
        if (snap.isEnabledSet()) t.setEnabled(snap.isEnabled());
        t.setTemplateDirectory(snap.getTemplateDirectory());
        t.setExtraProperties(snap.getExtraProperties());
        t.setBase(snap.getBase());
        t.setGrade(snap.getGrade());
        t.setConfinement(snap.getConfinement());
        t.setExportedLogin(snap.getExportedLogin().getAbsolutePath());
        t.setLocalPlugs(snap.getLocalPlugs());
        t.setPlugs(convertPlugs(snap.getPlugs()));
        t.setSlots(convertSlots(snap.getSlots()));
        return t;
    }

    private static List<org.kordamp.jreleaser.model.Plug> convertPlugs(List<Plug> plugs) {
        List<org.kordamp.jreleaser.model.Plug> ps = new ArrayList<>();
        for (Plug plug : plugs) {
            ps.add(convertPlug(plug));
        }
        return ps;
    }

    private static org.kordamp.jreleaser.model.Plug convertPlug(Plug plug) {
        org.kordamp.jreleaser.model.Plug p = new org.kordamp.jreleaser.model.Plug();
        p.setName(plug.getName());
        p.setAttributes(plug.getAttributes());
        return p;
    }

    private static List<org.kordamp.jreleaser.model.Slot> convertSlots(List<Slot> slots) {
        List<org.kordamp.jreleaser.model.Slot> ps = new ArrayList<>();
        for (Slot slot : slots) {
            ps.add(convertSlot(slot));
        }
        return ps;
    }

    private static org.kordamp.jreleaser.model.Slot convertSlot(Slot slot) {
        org.kordamp.jreleaser.model.Slot p = new org.kordamp.jreleaser.model.Slot();
        p.setName(slot.getName());
        p.setAttributes(slot.getAttributes());
        p.setReads(slot.getReads());
        p.setWrites(slot.getWrites());
        return p;
    }
}