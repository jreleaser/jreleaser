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
package org.jreleaser.gradle.plugin.internal.dsl

import groovy.transform.CompileStatic
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.Mail

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class MailImpl extends AbstractAnnouncer implements Mail {
    final Property<org.jreleaser.model.Mail.Transport> transport
    final Property<org.jreleaser.model.Mail.MimeType> mimeType
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
    MailImpl(ObjectFactory objects) {
        super(objects)
        transport = objects.property(org.jreleaser.model.Mail.Transport).convention(org.jreleaser.model.Mail.Transport.SMTP)
        mimeType = objects.property(org.jreleaser.model.Mail.MimeType).convention(org.jreleaser.model.Mail.MimeType.TEXT)
        port = objects.property(Integer).convention(Providers.notDefined())
        auth = objects.property(Boolean).convention(Providers.notDefined())
        host = objects.property(String).convention(Providers.notDefined())
        username = objects.property(String).convention(Providers.notDefined())
        password = objects.property(String).convention(Providers.notDefined())
        from = objects.property(String).convention(Providers.notDefined())
        to = objects.property(String).convention(Providers.notDefined())
        cc = objects.property(String).convention(Providers.notDefined())
        bcc = objects.property(String).convention(Providers.notDefined())
        subject = objects.property(String).convention(Providers.notDefined())
        message = objects.property(String).convention(Providers.notDefined())
        messageTemplate = objects.fileProperty().convention(Providers.notDefined())
        properties = objects.mapProperty(String, String).convention(Providers.notDefined())
    }

    @Override
    void setMessageTemplate(String messageTemplate) {
        this.messageTemplate.set(new File(messageTemplate))
    }

    @Override
    void setTransport(String transport) {
        this.transport.set(org.jreleaser.model.Mail.Transport.valueOf(transport.toUpperCase(Locale.ENGLISH)))
    }

    @Override
    void setMimeType(String mimeType) {
        this.mimeType.set(org.jreleaser.model.Mail.MimeType.valueOf(mimeType.toUpperCase(Locale.ENGLISH)))
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

    org.jreleaser.model.Mail toModel() {
        org.jreleaser.model.Mail mail = new org.jreleaser.model.Mail()
        fillProperties(mail)
        if (transport.present) mail.transport = transport.get()
        if (mimeType.present) mail.mimeType = mimeType.get()
        if (port.present) mail.port = port.get()
        if (auth.present) mail.auth = auth.get()
        if (host.present) mail.host = host.get()
        if (username.present) mail.username = username.get()
        if (password.present) mail.password = password.get()
        if (from.present) mail.from = from.get()
        if (to.present) mail.to = to.get()
        if (cc.present) mail.cc = cc.get()
        if (bcc.present) mail.bcc = bcc.get()
        if (subject.present) mail.subject = subject.get()
        if (message.present) mail.message = message.get()
        if (subject.present) mail.subject = subject.get()
        if (message.present) mail.message = message.get()
        if (messageTemplate.present) {
            mail.messageTemplate = messageTemplate.asFile.get().absolutePath
        }
        if (properties.present) mail.properties.putAll(properties.get())
        mail
    }
}
