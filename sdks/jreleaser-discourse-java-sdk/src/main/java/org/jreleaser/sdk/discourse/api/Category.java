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
public class Category{
    public int id;
    public String name;
    public String color;
    public String text_color;
    public String slug;
    public int topic_count;
    public int post_count;
    public int position;
    public String description;
    public String description_text;
    public String description_excerpt;
    public String topic_url;
    public boolean read_restricted;
    public int permission;
    public int notification_level;
    public boolean can_edit;
    public String topic_template;
    public boolean has_children;
    public String sort_order;
    public String sort_ascending;
    public boolean show_subcategory_list;
    public int num_featured_topics;
    public String default_view;
    public String subcategory_list_style;
    public String default_top_period;
    public String default_list_filter;
    public int minimum_required_tags;
    public boolean navigate_to_first_post_after_read;
    public int topics_day;
    public int topics_week;
    public int topics_month;
    public int topics_year;
    public int topics_all_time;
    public boolean is_uncategorized;
    public ArrayList<Object> subcategory_ids;
    public ArrayList<Object> subcategory_list;
    public String uploaded_logo;
    public String uploaded_background;

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

    public String getText_color() {
        return text_color;
    }

    public void setText_color(String text_color) {
        this.text_color = text_color;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public int getTopic_count() {
        return topic_count;
    }

    public void setTopic_count(int topic_count) {
        this.topic_count = topic_count;
    }

    public int getPost_count() {
        return post_count;
    }

    public void setPost_count(int post_count) {
        this.post_count = post_count;
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

    public String getDescription_text() {
        return description_text;
    }

    public void setDescription_text(String description_text) {
        this.description_text = description_text;
    }

    public String getDescription_excerpt() {
        return description_excerpt;
    }

    public void setDescription_excerpt(String description_excerpt) {
        this.description_excerpt = description_excerpt;
    }

    public String getTopic_url() {
        return topic_url;
    }

    public void setTopic_url(String topic_url) {
        this.topic_url = topic_url;
    }

    public boolean isRead_restricted() {
        return read_restricted;
    }

    public void setRead_restricted(boolean read_restricted) {
        this.read_restricted = read_restricted;
    }

    public int getPermission() {
        return permission;
    }

    public void setPermission(int permission) {
        this.permission = permission;
    }

    public int getNotification_level() {
        return notification_level;
    }

    public void setNotification_level(int notification_level) {
        this.notification_level = notification_level;
    }

    public boolean isCan_edit() {
        return can_edit;
    }

    public void setCan_edit(boolean can_edit) {
        this.can_edit = can_edit;
    }

    public String getTopic_template() {
        return topic_template;
    }

    public void setTopic_template(String topic_template) {
        this.topic_template = topic_template;
    }

    public boolean isHas_children() {
        return has_children;
    }

    public void setHas_children(boolean has_children) {
        this.has_children = has_children;
    }

    public String getSort_order() {
        return sort_order;
    }

    public void setSort_order(String sort_order) {
        this.sort_order = sort_order;
    }

    public String getSort_ascending() {
        return sort_ascending;
    }

    public void setSort_ascending(String sort_ascending) {
        this.sort_ascending = sort_ascending;
    }

    public boolean isShow_subcategory_list() {
        return show_subcategory_list;
    }

    public void setShow_subcategory_list(boolean show_subcategory_list) {
        this.show_subcategory_list = show_subcategory_list;
    }

    public int getNum_featured_topics() {
        return num_featured_topics;
    }

    public void setNum_featured_topics(int num_featured_topics) {
        this.num_featured_topics = num_featured_topics;
    }

    public String getDefault_view() {
        return default_view;
    }

    public void setDefault_view(String default_view) {
        this.default_view = default_view;
    }

    public String getSubcategory_list_style() {
        return subcategory_list_style;
    }

    public void setSubcategory_list_style(String subcategory_list_style) {
        this.subcategory_list_style = subcategory_list_style;
    }

    public String getDefault_top_period() {
        return default_top_period;
    }

    public void setDefault_top_period(String default_top_period) {
        this.default_top_period = default_top_period;
    }

    public String getDefault_list_filter() {
        return default_list_filter;
    }

    public void setDefault_list_filter(String default_list_filter) {
        this.default_list_filter = default_list_filter;
    }

    public int getMinimum_required_tags() {
        return minimum_required_tags;
    }

    public void setMinimum_required_tags(int minimum_required_tags) {
        this.minimum_required_tags = minimum_required_tags;
    }

    public boolean isNavigate_to_first_post_after_read() {
        return navigate_to_first_post_after_read;
    }

    public void setNavigate_to_first_post_after_read(boolean navigate_to_first_post_after_read) {
        this.navigate_to_first_post_after_read = navigate_to_first_post_after_read;
    }

    public int getTopics_day() {
        return topics_day;
    }

    public void setTopics_day(int topics_day) {
        this.topics_day = topics_day;
    }

    public int getTopics_week() {
        return topics_week;
    }

    public void setTopics_week(int topics_week) {
        this.topics_week = topics_week;
    }

    public int getTopics_month() {
        return topics_month;
    }

    public void setTopics_month(int topics_month) {
        this.topics_month = topics_month;
    }

    public int getTopics_year() {
        return topics_year;
    }

    public void setTopics_year(int topics_year) {
        this.topics_year = topics_year;
    }

    public int getTopics_all_time() {
        return topics_all_time;
    }

    public void setTopics_all_time(int topics_all_time) {
        this.topics_all_time = topics_all_time;
    }

    public boolean isIs_uncategorized() {
        return is_uncategorized;
    }

    public void setIs_uncategorized(boolean is_uncategorized) {
        this.is_uncategorized = is_uncategorized;
    }

    public ArrayList<Object> getSubcategory_ids() {
        return subcategory_ids;
    }

    public void setSubcategory_ids(ArrayList<Object> subcategory_ids) {
        this.subcategory_ids = subcategory_ids;
    }

    public ArrayList<Object> getSubcategory_list() {
        return subcategory_list;
    }

    public void setSubcategory_list(ArrayList<Object> subcategory_list) {
        this.subcategory_list = subcategory_list;
    }

    public String getUploaded_logo() {
        return uploaded_logo;
    }

    public void setUploaded_logo(String uploaded_logo) {
        this.uploaded_logo = uploaded_logo;
    }

    public String getUploaded_background() {
        return uploaded_background;
    }

    public void setUploaded_background(String uploaded_background) {
        this.uploaded_background = uploaded_background;
    }
}