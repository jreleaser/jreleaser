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
package org.jreleaser.sdk.nexus2.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Andres Almiray
 * @since 1.11.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class StagingActivity {
    private String name;
    private String startedByUserId;
    private String startedByIpAddress;
    private Instant started;
    private Instant stopped;
    private List<StagingActivityEvent> events = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStartedByUserId() {
        return startedByUserId;
    }

    public void setStartedByUserId(String startedByUserId) {
        this.startedByUserId = startedByUserId;
    }

    public String getStartedByIpAddress() {
        return startedByIpAddress;
    }

    public void setStartedByIpAddress(String startedByIpAddress) {
        this.startedByIpAddress = startedByIpAddress;
    }

    public Instant getStarted() {
        return started;
    }

    public void setStarted(Instant started) {
        this.started = started;
    }

    public Instant getStopped() {
        return stopped;
    }

    public void setStopped(Instant stopped) {
        this.stopped = stopped;
    }

    public List<StagingActivityEvent> getEvents() {
        return events;
    }

    public void setEvents(List<StagingActivityEvent> events) {
        this.events = events;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StagingActivityEvent {
        private String name;
        private Instant timestamp;
        private Integer severity;
        private List<StagingProperty> properties = new ArrayList<>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Instant getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Instant timestamp) {
            this.timestamp = timestamp;
        }

        public Integer getSeverity() {
            return severity;
        }

        public void setSeverity(Integer severity) {
            this.severity = severity;
        }

        public List<StagingProperty> getProperties() {
            return properties;
        }

        public void setProperties(List<StagingProperty> properties) {
            this.properties = properties;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StagingProperty {
        private String name;
        private String value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
