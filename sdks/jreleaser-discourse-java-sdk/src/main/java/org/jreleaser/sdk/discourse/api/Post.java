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

import java.util.ArrayList;

/**
 * @author shblue21
 * @since 1.3.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Post {
    public int id;
    public String name;
    public String username;
    public String avatar_template;
    public String created_at;
    public String raw;
    public String cooked;
    public int post_number;
    public int post_type;
    public String updated_at;
    public int reply_count;
    public String reply_to_post_number;
    public int quote_count;
    public int incoming_link_count;
    public int reads;
    public int readers_count;
    public int score;
    public boolean yours;
    public int topic_id;
    public String topic_slug;
    public String display_username;
    public String primary_group_name;
    public String flair_name;
    public String flair_url;
    public String flair_bg_color;
    public String flair_color;
    public int version;
    public boolean can_edit;
    public boolean can_delete;
    public boolean can_recover;
    public boolean can_wiki;
    public String user_title;
    public boolean bookmarked;
    public ArrayList<ActionsSummary> actions_summary;
    public boolean moderator;
    public boolean admin;
    public boolean staff;
    public int user_id;
    public int draft_sequence;
    public boolean hidden;
    public int trust_level;
    public String deleted_at;
    public boolean user_deleted;
    public String edit_reason;
    public boolean can_view_edit_history;
    public boolean wiki;
    public String reviewable_id;
    public int reviewable_score_count;
    public int reviewable_score_pending_count;

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

    public String getAvatar_template() {
        return avatar_template;
    }

    public void setAvatar_template(String avatar_template) {
        this.avatar_template = avatar_template;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
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

    public int getPost_number() {
        return post_number;
    }

    public void setPost_number(int post_number) {
        this.post_number = post_number;
    }

    public int getPost_type() {
        return post_type;
    }

    public void setPost_type(int post_type) {
        this.post_type = post_type;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public int getReply_count() {
        return reply_count;
    }

    public void setReply_count(int reply_count) {
        this.reply_count = reply_count;
    }

    public String getReply_to_post_number() {
        return reply_to_post_number;
    }

    public void setReply_to_post_number(String reply_to_post_number) {
        this.reply_to_post_number = reply_to_post_number;
    }

    public int getQuote_count() {
        return quote_count;
    }

    public void setQuote_count(int quote_count) {
        this.quote_count = quote_count;
    }

    public int getIncoming_link_count() {
        return incoming_link_count;
    }

    public void setIncoming_link_count(int incoming_link_count) {
        this.incoming_link_count = incoming_link_count;
    }

    public int getReads() {
        return reads;
    }

    public void setReads(int reads) {
        this.reads = reads;
    }

    public int getReaders_count() {
        return readers_count;
    }

    public void setReaders_count(int readers_count) {
        this.readers_count = readers_count;
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

    public int getTopic_id() {
        return topic_id;
    }

    public void setTopic_id(int topic_id) {
        this.topic_id = topic_id;
    }

    public String getTopic_slug() {
        return topic_slug;
    }

    public void setTopic_slug(String topic_slug) {
        this.topic_slug = topic_slug;
    }

    public String getDisplay_username() {
        return display_username;
    }

    public void setDisplay_username(String display_username) {
        this.display_username = display_username;
    }

    public String getPrimary_group_name() {
        return primary_group_name;
    }

    public void setPrimary_group_name(String primary_group_name) {
        this.primary_group_name = primary_group_name;
    }

    public String getFlair_name() {
        return flair_name;
    }

    public void setFlair_name(String flair_name) {
        this.flair_name = flair_name;
    }

    public String getFlair_url() {
        return flair_url;
    }

    public void setFlair_url(String flair_url) {
        this.flair_url = flair_url;
    }

    public String getFlair_bg_color() {
        return flair_bg_color;
    }

    public void setFlair_bg_color(String flair_bg_color) {
        this.flair_bg_color = flair_bg_color;
    }

    public String getFlair_color() {
        return flair_color;
    }

    public void setFlair_color(String flair_color) {
        this.flair_color = flair_color;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public boolean isCan_edit() {
        return can_edit;
    }

    public void setCan_edit(boolean can_edit) {
        this.can_edit = can_edit;
    }

    public boolean isCan_delete() {
        return can_delete;
    }

    public void setCan_delete(boolean can_delete) {
        this.can_delete = can_delete;
    }

    public boolean isCan_recover() {
        return can_recover;
    }

    public void setCan_recover(boolean can_recover) {
        this.can_recover = can_recover;
    }

    public boolean isCan_wiki() {
        return can_wiki;
    }

    public void setCan_wiki(boolean can_wiki) {
        this.can_wiki = can_wiki;
    }

    public String getUser_title() {
        return user_title;
    }

    public void setUser_title(String user_title) {
        this.user_title = user_title;
    }

    public boolean isBookmarked() {
        return bookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        this.bookmarked = bookmarked;
    }

    public ArrayList<ActionsSummary> getActions_summary() {
        return actions_summary;
    }

    public void setActions_summary(ArrayList<ActionsSummary> actions_summary) {
        this.actions_summary = actions_summary;
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

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getDraft_sequence() {
        return draft_sequence;
    }

    public void setDraft_sequence(int draft_sequence) {
        this.draft_sequence = draft_sequence;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public int getTrust_level() {
        return trust_level;
    }

    public void setTrust_level(int trust_level) {
        this.trust_level = trust_level;
    }

    public String getDeleted_at() {
        return deleted_at;
    }

    public void setDeleted_at(String deleted_at) {
        this.deleted_at = deleted_at;
    }

    public boolean isUser_deleted() {
        return user_deleted;
    }

    public void setUser_deleted(boolean user_deleted) {
        this.user_deleted = user_deleted;
    }

    public String getEdit_reason() {
        return edit_reason;
    }

    public void setEdit_reason(String edit_reason) {
        this.edit_reason = edit_reason;
    }

    public boolean isCan_view_edit_history() {
        return can_view_edit_history;
    }

    public void setCan_view_edit_history(boolean can_view_edit_history) {
        this.can_view_edit_history = can_view_edit_history;
    }

    public boolean isWiki() {
        return wiki;
    }

    public void setWiki(boolean wiki) {
        this.wiki = wiki;
    }

    public String getReviewable_id() {
        return reviewable_id;
    }

    public void setReviewable_id(String reviewable_id) {
        this.reviewable_id = reviewable_id;
    }

    public int getReviewable_score_count() {
        return reviewable_score_count;
    }

    public void setReviewable_score_count(int reviewable_score_count) {
        this.reviewable_score_count = reviewable_score_count;
    }

    public int getReviewable_score_pending_count() {
        return reviewable_score_pending_count;
    }

    public void setReviewable_score_pending_count(int reviewable_score_pending_count) {
        this.reviewable_score_pending_count = reviewable_score_pending_count;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", username='" + username + '\'' +
                ", avatar_template='" + avatar_template + '\'' +
                ", created_at='" + created_at + '\'' +
                ", raw='" + raw + '\'' +
                ", cooked='" + cooked + '\'' +
                ", post_number=" + post_number +
                ", post_type=" + post_type +
                ", updated_at='" + updated_at + '\'' +
                ", reply_count=" + reply_count +
                ", reply_to_post_number='" + reply_to_post_number + '\'' +
                ", quote_count=" + quote_count +
                ", incoming_link_count=" + incoming_link_count +
                ", reads=" + reads +
                ", readers_count=" + readers_count +
                ", score=" + score +
                ", yours=" + yours +
                ", topic_id=" + topic_id +
                ", topic_slug='" + topic_slug + '\'' +
                ", display_username='" + display_username + '\'' +
                ", primary_group_name='" + primary_group_name + '\'' +
                ", flair_name='" + flair_name + '\'' +
                ", flair_url='" + flair_url + '\'' +
                ", flair_bg_color='" + flair_bg_color + '\'' +
                ", flair_color='" + flair_color + '\'' +
                ", version=" + version +
                ", can_edit=" + can_edit +
                ", can_delete=" + can_delete +
                ", can_recover=" + can_recover +
                ", can_wiki=" + can_wiki +
                ", user_title='" + user_title + '\'' +
                ", bookmarked=" + bookmarked +
                ", actions_summary=" + actions_summary +
                ", moderator=" + moderator +
                ", admin=" + admin +
                ", staff=" + staff +
                ", user_id=" + user_id +
                ", draft_sequence=" + draft_sequence +
                ", hidden=" + hidden +
                ", trust_level=" + trust_level +
                ", deleted_at='" + deleted_at + '\'' +
                ", user_deleted=" + user_deleted +
                ", edit_reason='" + edit_reason + '\'' +
                ", can_view_edit_history=" + can_view_edit_history +
                ", wiki=" + wiki +
                ", reviewable_id='" + reviewable_id + '\'' +
                ", reviewable_score_count=" + reviewable_score_count +
                ", reviewable_score_pending_count=" + reviewable_score_pending_count +
                '}';
    }
}
