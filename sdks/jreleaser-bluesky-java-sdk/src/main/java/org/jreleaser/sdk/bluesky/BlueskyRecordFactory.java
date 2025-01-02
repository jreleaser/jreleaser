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
package org.jreleaser.sdk.bluesky;

import org.jreleaser.sdk.bluesky.api.BlueskyAPI;
import org.jreleaser.sdk.bluesky.api.CreateRecordResponse;
import org.jreleaser.sdk.bluesky.api.CreateTextRecordRequest;
import org.jreleaser.sdk.bluesky.api.Facet;
import org.jreleaser.sdk.bluesky.api.Index;
import org.jreleaser.sdk.bluesky.api.ResolveHandleResponse;
import org.jreleaser.sdk.bluesky.api.features.Feature;
import org.jreleaser.sdk.bluesky.api.features.LinkFeature;
import org.jreleaser.sdk.bluesky.api.features.MentionFeature;
import org.jreleaser.sdk.bluesky.api.features.TagFeature;
import org.jreleaser.sdk.commons.RestAPIException;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Tom Cools
 * @since 1.12.0
 */
public class BlueskyRecordFactory {

    private static final Pattern URL_PATTERN = Pattern.compile(
        "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
            + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
            + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern TAG_PATTERN = Pattern.compile("(#[a-zA-Z]{2,})");
    private static final Pattern MENTION_PATTERN = Pattern.compile("(^|\\s|\\()(@)([a-zA-Z0-9.-]+)(\\b)");

    private final BlueskyAPI api;

    public BlueskyRecordFactory(BlueskyAPI api) {
        this.api = api;
    }

    public CreateTextRecordRequest textRecord(String repo, String text) {
        CreateTextRecordRequest request = new CreateTextRecordRequest();
        request.setRepo(requireNonBlank(repo, "'repo' must not be blank").trim());

        CreateTextRecordRequest.TextRecord textRecord = new CreateTextRecordRequest.TextRecord();
        textRecord.setText(requireNonBlank(text, "'text' must not be blank").trim());
        textRecord.setCreatedAt(Instant.now().toString());
        request.setRecord(textRecord);

        textRecord.setFacets(determineFacets(text));

        return request;
    }

    public CreateTextRecordRequest textRecord(String repo, String text, CreateRecordResponse root, CreateRecordResponse parent) {
        CreateTextRecordRequest request = textRecord(repo, text);

        CreateTextRecordRequest.ReplyReference reply = new CreateTextRecordRequest.ReplyReference();
        reply.setRoot(root);
        reply.setParent(parent);

        request.getRecord().setReply(reply);

        return request;
    }

    /*
     * Retrieves facets from the given text.
     *
     * Source of information: https://docs.bsky.app/docs/advanced-guides/post-richtext
     */
    public List<Facet> determineFacets(String text) {
        List<Facet> facets = new ArrayList<>();

        facets.addAll(parseFeatures(text, URL_PATTERN, link -> {
            LinkFeature linkFeature = new LinkFeature();
            linkFeature.setUri(link);
            return Optional.of(linkFeature);
        }));

        facets.addAll(parseFeatures(text, TAG_PATTERN, tag -> {
            TagFeature tagFeature = new TagFeature();
            tagFeature.setTag(tag.replace("#", "").trim());
            return Optional.of(tagFeature);
        }));

        facets.addAll(parseFeatures(text, MENTION_PATTERN, mention -> {
            String handle = mention.replace("@", "").trim();
            MentionFeature mentionFeature = new MentionFeature();
            try {
                // MentionFeature requires the DID, this should be resolved against the API.
                ResolveHandleResponse handleResponse = api.resolveHandle(handle);
                mentionFeature.setDid(handleResponse.getDid());
            } catch (RestAPIException e) {
                // Given the DID could not be resolved, for whatever reason, just post without the mention.
                return Optional.empty();
            }
            return Optional.of(mentionFeature);
        }));
        return facets;
    }

    private static List<Facet> parseFeatures(String text, Pattern pattern, Function<String, Optional<Feature>> matchedContentToFeature) {
        BlueskyStringEncodingWrapper wrapper = new BlueskyStringEncodingWrapper(text);
        Matcher matcher = pattern.matcher(wrapper);
        List<Facet> urlFacets = new ArrayList<>();
        while (matcher.find()) {
            int matchStart = matcher.start(1);
            int matchEnd = matcher.end();
            Facet facet = new Facet();
            Index index = new Index();
            index.setByteStart(matchStart);
            index.setByteEnd(matchEnd);
            facet.setIndex(index);
            String matchedSubstring = wrapper.subSequence(matchStart, matchEnd).toString();

            matchedContentToFeature.apply(matchedSubstring).ifPresent(feature -> {
                facet.setFeatures(Collections.singletonList(feature));
                urlFacets.add(facet);
            });
        }
        return urlFacets;
    }

    /**
     * Helper class responsible for encoding a String to a bytebuffer of the correct encoding for Bluesky.
     * Bluesky does everything internally as UTF-8, but Java by default uses Strings in UTF-16.
     * Without this class, indexes calculated with Pattern/Matcher use UTF-16, which leads to incorrect indexes.
     *
     * See: https://docs.bsky.app/docs/advanced-guides/post-richtext#text-encoding-and-indexing
     */
    public static class BlueskyStringEncodingWrapper implements CharSequence {

        private final ByteBuffer buffer;
        private final Charset charset;

        public BlueskyStringEncodingWrapper(String sourceString) {
            this.charset = StandardCharsets.UTF_8;
            this.buffer = ByteBuffer.wrap(sourceString.getBytes(charset));
        }

        private BlueskyStringEncodingWrapper(ByteBuffer sourceBuffer) {
            this.charset = StandardCharsets.UTF_8;
            this.buffer = sourceBuffer;
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            ByteBuffer buffer = this.buffer.duplicate();
            buffer.position(buffer.position() + start);
            buffer.limit(buffer.position() + (end - start));
            return new BlueskyStringEncodingWrapper(buffer);
        }

        @Override
        public int length() {
            return buffer.limit();
        }

        @Override
        public char charAt(int index) {
            return (char) buffer.get(index);
        }

        @Override
        public String toString() {
            return charset.decode(buffer.duplicate()).toString();
        }
    }
}
