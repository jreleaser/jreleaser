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
package org.jreleaser.sdk.smtp;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.MessagingException;
import org.jreleaser.logging.SimpleJReleaserLoggerAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static jakarta.mail.Message.RecipientType.CC;
import static jakarta.mail.Message.RecipientType.TO;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class MessageSmtpCommandTest {
    @RegisterExtension
    GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP);

    @Test
    void testMessage() throws SmtpException, MessagingException {
        // given:
        String sender = "test@acme.com";
        String receiver = "jreleaser@acme.com";
        String cc = "copy@acme.com";
        String bcc = "hidden@acme.com";
        String message = "<html><body>Test<body></html>";

        MessageSmtpCommand command = MessageSmtpCommand
            .builder(new SimpleJReleaserLoggerAdapter(SimpleJReleaserLoggerAdapter.Level.DEBUG))
            .transport(org.jreleaser.model.Mail.Transport.SMTP)
            .host("localhost")
            .port(3025)
            .auth(false)
            .from(sender)
            .to(receiver)
            .cc(cc)
            .bcc(bcc)
            .subject("Test")
            .message(message)
            .mimeType(org.jreleaser.model.Mail.MimeType.HTML)
            .dryrun(false)
            .build();

        // when:
        command.execute();
        await().timeout(3, SECONDS)
            .until(() -> greenMail.getReceivedMessages().length == 3);

        // then:
        assertThat(sender, equalTo(greenMail.getReceivedMessages()[0].getFrom()[0].toString()));
        assertThat(receiver, equalTo(greenMail.getReceivedMessages()[0].getRecipients(TO)[0].toString()));
        assertThat(sender, equalTo(greenMail.getReceivedMessages()[0].getFrom()[0].toString()));
        assertThat(cc, equalTo(greenMail.getReceivedMessages()[0].getRecipients(CC)[0].toString()));
        assertThat(sender, equalTo(greenMail.getReceivedMessages()[0].getFrom()[0].toString()));
    }

    @Test
    void testDryRun() throws SmtpException, MessagingException {
        // given:
        String sender = "test@acme.com";
        String receiver = "jreleaser@acme.com";
        String cc = "copy@acme.com";
        String bcc = "hidden@acme.com";
        String message = "<html><body>Test<body></html>";

        MessageSmtpCommand command = MessageSmtpCommand
            .builder(new SimpleJReleaserLoggerAdapter(SimpleJReleaserLoggerAdapter.Level.DEBUG))
            .transport(org.jreleaser.model.Mail.Transport.SMTP)
            .host("localhost")
            .port(3025)
            .auth(false)
            .from(sender)
            .to(receiver)
            .cc(cc)
            .bcc(bcc)
            .subject("Test")
            .message(message)
            .mimeType(org.jreleaser.model.Mail.MimeType.HTML)
            .dryrun(true)
            .build();

        // when:
        command.execute();
        await().atMost(3, SECONDS);

        // then:
        assertThat(greenMail.getReceivedMessages().length, equalTo(0));
    }
}
