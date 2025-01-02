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
package org.jreleaser.gradle.plugin.internal.dsl.announce

import groovy.transform.CompileStatic
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.announce.SmtpAnnouncer
import org.jreleaser.model.Mail

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class SmtpAnnouncerImpl extends AbstractAnnouncer implements SmtpAnnouncer {
    final Property<Mail.Transport> transport
    final Property<Mail.MimeType> mimeType
    final Property<Integer> port
    final Property<Boolean> auth
    final Property<String> host
    final Property<String> username
    final Property<String> password
    final Property<String> from
    final Property<String> to
    final Property<String> cc
    final Property<String> bcc
    final Property<String> subject
    final Property<String> message
    final RegularFileProperty messageTemplate
    final MapProperty<String, String> properties

    @Inject
    SmtpAnnouncerImpl(ObjectFactory objects) {
        super(objects)
        transport = objects.property(Mail.Transport).convention(Mail.Transport.SMTP)
        mimeType = objects.property(Mail.MimeType).convention(Mail.MimeType.TEXT)
        port = objects.property(Integer).convention(Providers.<Integer> notDefined())
        auth = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        host = objects.property(String).convention(Providers.<String> notDefined())
        username = objects.property(String).convention(Providers.<String> notDefined())
        password = objects.property(String).convention(Providers.<String> notDefined())
        from = objects.property(String).convention(Providers.<String> notDefined())
        to = objects.property(String).convention(Providers.<String> notDefined())
        cc = objects.property(String).convention(Providers.<String> notDefined())
        bcc = objects.property(String).convention(Providers.<String> notDefined())
        subject = objects.property(String).convention(Providers.<String> notDefined())
        message = objects.property(String).convention(Providers.<String> notDefined())
        messageTemplate = objects.fileProperty().convention(Providers.notDefined())
        properties = objects.mapProperty(String, String).convention(Providers.notDefined())
    }

    @Override
    void setMessageTemplate(String messageTemplate) {
        this.messageTemplate.set(new File(messageTemplate))
    }

    @Override
    void setTransport(String transport) {
        this.transport.set(Mail.Transport.valueOf(transport.toUpperCase(Locale.ENGLISH)))
    }

    @Override
    void setMimeType(String mimeType) {
        this.mimeType.set(Mail.MimeType.valueOf(mimeType.toUpperCase(Locale.ENGLISH)))
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            port.present ||
            auth.present ||
            host.present ||
            username.present ||
            password.present ||
            from.present ||
            to.present ||
            cc.present ||
            bcc.present ||
            subject.present ||
            message.present ||
            messageTemplate.present ||
            properties.present
    }

    org.jreleaser.model.internal.announce.SmtpAnnouncer toModel() {
        org.jreleaser.model.internal.announce.SmtpAnnouncer announcer = new org.jreleaser.model.internal.announce.SmtpAnnouncer()
        fillProperties(announcer)
        if (transport.present) announcer.transport = transport.get()
        if (mimeType.present) announcer.mimeType = mimeType.get()
        if (port.present) announcer.port = port.get()
        if (auth.present) announcer.auth = auth.get()
        if (host.present) announcer.host = host.get()
        if (username.present) announcer.username = username.get()
        if (password.present) announcer.password = password.get()
        if (from.present) announcer.from = from.get()
        if (to.present) announcer.to = to.get()
        if (cc.present) announcer.cc = cc.get()
        if (bcc.present) announcer.bcc = bcc.get()
        if (subject.present) announcer.subject = subject.get()
        if (message.present) announcer.message = message.get()
        if (subject.present) announcer.subject = subject.get()
        if (message.present) announcer.message = message.get()
        if (messageTemplate.present) {
            announcer.messageTemplate = messageTemplate.asFile.get().absolutePath
        }
        if (properties.present) announcer.properties.putAll(properties.get())
        announcer
    }
}
