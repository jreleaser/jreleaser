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
public class Post {
    private int id;
    private String name;
    private String username;
    private String avatarTemplate;
    private String createdAt;
    private String raw;
    private String cooked;
    private int postNumber;
    private int postType;
    private String updatedAt;
    private int replyCount;
    private String replyToPostNumber;
    private int quoteCount;
    private int incomingLinkCount;
    private int reads;
    private int readersCount;
    private int score;
    private boolean yours;
    private int topicId;
    private String topicSlug;
    private String displayUsername;
    private String primaryGroupName;
    private String flairName;
    private String flairUrl;
    private String flairBgColor;
    private String flairColor;
    private int version;
    private boolean canEdit;
    private boolean canDelete;
    private boolean canRecover;
    private boolean canWiki;
    private String userTitle;
    private boolean bookmarked;
    private List<ActionsSummary> actionsSummary;
    private boolean moderator;
    private boolean admin;
    private boolean staff;
    private int userId;
    private int draftSequence;
    private boolean hidden;
    private int trustLevel;
    private String deletedAt;
    private boolean userDeleted;
    private String editReason;
    private boolean canViewEditHistory;
    private boolean wiki;
    private String reviewableId;
    private int reviewableScoreCount;
    private int reviewableScorePendingCount;

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatarTemplate() {
        return avatarTemplate;
    }

    public void setAvatarTemplate(String avatarTemplate) {
        this.avatarTemplate = avatarTemplate;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getRaw() {
        return raw;
    }

    public void setRaw(String raw) {
        this.raw = raw;
    }

    public String getCooked() {
        return cooked;
    }

    public void setCooked(String cooked) {
        this.cooked = cooked;
    }

    public int getPostNumber() {
        return postNumber;
    }

    public void setPostNumber(int postNumber) {
        this.postNumber = postNumber;
    }

    public int getPostType() {
        return postType;
    }

    public void setPostType(int postType) {
        this.postType = postType;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(int replyCount) {
        this.replyCount = replyCount;
    }

    public String getReplyToPostNumber() {
        return replyToPostNumber;
    }

    public void setReplyToPostNumber(String replyToPostNumber) {
        this.replyToPostNumber = replyToPostNumber;
    }

    public int getQuoteCount() {
        return quoteCount;
    }

    public void setQuoteCount(int quoteCount) {
        this.quoteCount = quoteCount;
    }

    public int getIncomingLinkCount() {
        return incomingLinkCount;
    }

    public void setIncomingLinkCount(int incomingLinkCount) {
        this.incomingLinkCount = incomingLinkCount;
    }

    public int getReads() {
        return reads;
    }

    public void setReads(int reads) {
        this.reads = reads;
    }

    public int getReadersCount() {
        return readersCount;
    }

    public void setReadersCount(int readersCount) {
        this.readersCount = readersCount;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean isYours() {
        return yours;
    }

    public void setYours(boolean yours) {
        this.yours = yours;
    }

    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    public String getTopicSlug() {
        return topicSlug;
    }

    public void setTopicSlug(String topicSlug) {
        this.topicSlug = topicSlug;
    }

    public String getDisplayUsername() {
        return displayUsername;
    }

    public void setDisplayUsername(String displayUsername) {
        this.displayUsername = displayUsername;
    }

    public String getPrimaryGroupName() {
        return primaryGroupName;
    }

    public void setPrimaryGroupName(String primaryGroupName) {
        this.primaryGroupName = primaryGroupName;
    }

    public String getFlairName() {
        return flairName;
    }

    public void setFlairName(String flairName) {
        this.flairName = flairName;
    }

    public String getFlairUrl() {
        return flairUrl;
    }

    public void setFlairUrl(String flairUrl) {
        this.flairUrl = flairUrl;
    }

    public String getFlairBgColor() {
        return flairBgColor;
    }

    public void setFlairBgColor(String flairBgColor) {
        this.flairBgColor = flairBgColor;
    }

    public String getFlairColor() {
        return flairColor;
    }

    public void setFlairColor(String flairColor) {
        this.flairColor = flairColor;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    public boolean isCanDelete() {
        return canDelete;
    }

    public void setCanDelete(boolean canDelete) {
        this.canDelete = canDelete;
    }

    public boolean isCanRecover() {
        return canRecover;
    }

    public void setCanRecover(boolean canRecover) {
        this.canRecover = canRecover;
    }

    public boolean isCanWiki() {
        return canWiki;
    }

    public void setCanWiki(boolean canWiki) {
        this.canWiki = canWiki;
    }

    public String getUserTitle() {
        return userTitle;
    }

    public void setUserTitle(String userTitle) {
        this.userTitle = userTitle;
    }

    public boolean isBookmarked() {
        return bookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        this.bookmarked = bookmarked;
    }

    public List<ActionsSummary> getActionsSummary() {
        return actionsSummary;
    }

    public void setActionsSummary(List<ActionsSummary> actionsSummary) {
        this.actionsSummary = actionsSummary;
    }

    public boolean isModerator() {
        return moderator;
    }

    public void setModerator(boolean moderator) {
        this.moderator = moderator;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean isStaff() {
        return staff;
    }

    public void setStaff(boolean staff) {
        this.staff = staff;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getDraftSequence() {
        return draftSequence;
    }

    public void setDraftSequence(int draftSequence) {
        this.draftSequence = draftSequence;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public int getTrustLevel() {
        return trustLevel;
    }

    public void setTrustLevel(int trustLevel) {
        this.trustLevel = trustLevel;
    }

    public String getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(String deletedAt) {
        this.deletedAt = deletedAt;
    }

    public boolean isUserDeleted() {
        return userDeleted;
    }

    public void setUserDeleted(boolean userDeleted) {
        this.userDeleted = userDeleted;
    }

    public String getEditReason() {
        return editReason;
    }

    public void setEditReason(String editReason) {
        this.editReason = editReason;
    }

    public boolean isCanViewEditHistory() {
        return canViewEditHistory;
    }

    public void setCanViewEditHistory(boolean canViewEditHistory) {
        this.canViewEditHistory = canViewEditHistory;
    }

    public boolean isWiki() {
        return wiki;
    }

    public void setWiki(boolean wiki) {
        this.wiki = wiki;
    }

    public String getReviewableId() {
        return reviewableId;
    }

    public void setReviewableId(String reviewableId) {
        this.reviewableId = reviewableId;
    }

    public int getReviewableScoreCount() {
        return reviewableScoreCount;
    }

    public void setReviewableScoreCount(int reviewableScoreCount) {
        this.reviewableScoreCount = reviewableScoreCount;
    }

    public int getReviewableScorePendingCount() {
        return reviewableScorePendingCount;
    }

    public void setReviewableScorePendingCount(int reviewableScorePendingCount) {
        this.reviewableScorePendingCount = reviewableScorePendingCount;
    }
}
