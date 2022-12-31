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
package org.jreleaser.sdk.discourse.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * @author shblue21
 * @since 1.3.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Category {
    private int id;
    private String name;
    private String color;
    private String textColor;
    private String slug;
    private int topicCount;
    private int postCount;
    private int position;
    private String description;
    private String descriptionText;
    private String descriptionExcerpt;
    private String topicUrl;
    private boolean readRestricted;
    private int permission;
    private int notificationLevel;
    private boolean canEdit;
    private String topicTemplate;
    private boolean hasChildren;
    private String sortOrder;
    private String sortAscending;
    private boolean showSubcategoryList;
    private int numFeaturedTopics;
    private String defaultView;
    private String subcategoryListStyle;
    private String defaultTopPeriod;
    private String defaultListFilter;
    private int minimumRequiredTags;
    private boolean navigateToFirstPostAfterRead;
    private int topicsDay;
    private int topicsWeek;
    private int topicsMonth;
    private int topicsYear;
    private int topicsAllTime;
    private boolean isUncategorized;
    private List<Object> subcategoryIds;
    private List<Object> subcategoryList;
    private String uploadedLogo;
    private String uploadedBackground;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getTextColor() {
        return textColor;
    }

    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public int getTopicCount() {
        return topicCount;
    }

    public void setTopicCount(int topicCount) {
        this.topicCount = topicCount;
    }

    public int getPostCount() {
        return postCount;
    }

    public void setPostCount(int postCount) {
        this.postCount = postCount;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescriptionText() {
        return descriptionText;
    }

    public void setDescriptionText(String descriptionText) {
        this.descriptionText = descriptionText;
    }

    public String getDescriptionExcerpt() {
        return descriptionExcerpt;
    }

    public void setDescriptionExcerpt(String descriptionExcerpt) {
        this.descriptionExcerpt = descriptionExcerpt;
    }

    public String getTopicUrl() {
        return topicUrl;
    }

    public void setTopicUrl(String topicUrl) {
        this.topicUrl = topicUrl;
    }

    public boolean isReadRestricted() {
        return readRestricted;
    }

    public void setReadRestricted(boolean readRestricted) {
        this.readRestricted = readRestricted;
    }

    public int getPermission() {
        return permission;
    }

    public void setPermission(int permission) {
        this.permission = permission;
    }

    public int getNotificationLevel() {
        return notificationLevel;
    }

    public void setNotificationLevel(int notificationLevel) {
        this.notificationLevel = notificationLevel;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    public String getTopicTemplate() {
        return topicTemplate;
    }

    public void setTopicTemplate(String topicTemplate) {
        this.topicTemplate = topicTemplate;
    }

    public boolean isHasChildren() {
        return hasChildren;
    }

    public void setHasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getSortAscending() {
        return sortAscending;
    }

    public void setSortAscending(String sortAscending) {
        this.sortAscending = sortAscending;
    }

    public boolean isShowSubcategoryList() {
        return showSubcategoryList;
    }

    public void setShowSubcategoryList(boolean showSubcategoryList) {
        this.showSubcategoryList = showSubcategoryList;
    }

    public int getNumFeaturedTopics() {
        return numFeaturedTopics;
    }

    public void setNumFeaturedTopics(int numFeaturedTopics) {
        this.numFeaturedTopics = numFeaturedTopics;
    }

    public String getDefaultView() {
        return defaultView;
    }

    public void setDefaultView(String defaultView) {
        this.defaultView = defaultView;
    }

    public String getSubcategoryListStyle() {
        return subcategoryListStyle;
    }

    public void setSubcategoryListStyle(String subcategoryListStyle) {
        this.subcategoryListStyle = subcategoryListStyle;
    }

    public String getDefaultTopPeriod() {
        return defaultTopPeriod;
    }

    public void setDefaultTopPeriod(String defaultTopPeriod) {
        this.defaultTopPeriod = defaultTopPeriod;
    }

    public String getDefaultListFilter() {
        return defaultListFilter;
    }

    public void setDefaultListFilter(String defaultListFilter) {
        this.defaultListFilter = defaultListFilter;
    }

    public int getMinimumRequiredTags() {
        return minimumRequiredTags;
    }

    public void setMinimumRequiredTags(int minimumRequiredTags) {
        this.minimumRequiredTags = minimumRequiredTags;
    }

    public boolean isNavigateToFirstPostAfterRead() {
        return navigateToFirstPostAfterRead;
    }

    public void setNavigateToFirstPostAfterRead(boolean navigateToFirstPostAfterRead) {
        this.navigateToFirstPostAfterRead = navigateToFirstPostAfterRead;
    }

    public int getTopicsDay() {
        return topicsDay;
    }

    public void setTopicsDay(int topicsDay) {
        this.topicsDay = topicsDay;
    }

    public int getTopicsWeek() {
        return topicsWeek;
    }

    public void setTopicsWeek(int topicsWeek) {
        this.topicsWeek = topicsWeek;
    }

    public int getTopicsMonth() {
        return topicsMonth;
    }

    public void setTopicsMonth(int topicsMonth) {
        this.topicsMonth = topicsMonth;
    }

    public int getTopicsYear() {
        return topicsYear;
    }

    public void setTopicsYear(int topicsYear) {
        this.topicsYear = topicsYear;
    }

    public int getTopicsAllTime() {
        return topicsAllTime;
    }

    public void setTopicsAllTime(int topicsAllTime) {
        this.topicsAllTime = topicsAllTime;
    }

    public boolean isUncategorized() {
        return isUncategorized;
    }

    public void setUncategorized(boolean uncategorized) {
        isUncategorized = uncategorized;
    }

    public List<Object> getSubcategoryIds() {
        return subcategoryIds;
    }

    public void setSubcategoryIds(List<Object> subcategoryIds) {
        this.subcategoryIds = subcategoryIds;
    }

    public List<Object> getSubcategoryList() {
        return subcategoryList;
    }

    public void setSubcategoryList(List<Object> subcategoryList) {
        this.subcategoryList = subcategoryList;
    }

    public String getUploadedLogo() {
        return uploadedLogo;
    }

    public void setUploadedLogo(String uploadedLogo) {
        this.uploadedLogo = uploadedLogo;
    }

    public String getUploadedBackground() {
        return uploadedBackground;
    }

    public void setUploadedBackground(String uploadedBackground) {
        this.uploadedBackground = uploadedBackground;
    }

    public static Category empty() {
        return new Category();
    }
}