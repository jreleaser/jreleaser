/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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
package org.jreleaser.sdk.mail;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.jreleaser.model.Mail;
import org.jreleaser.util.SimpleJReleaserLoggerAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.mail.MessagingException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.mail.Message.RecipientType.CC;
import static javax.mail.Message.RecipientType.TO;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class MessageMailCommandTest {
    @RegisterExtension
    GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP);

    @Test
    public void testMessage() throws MailException, MessagingException {
        // given:
        String sender = "test@acme.com";
        String receiver = "jreleaser@acme.com";
        String cc = "copy@acme.com";
        String bcc = "hidden@acme.com";
        String message = "<html><body>Test<body></html>";

        MessageMailCommand command = MessageMailCommand
            .builder(new SimpleJReleaserLoggerAdapter(SimpleJReleaserLoggerAdapter.Level.DEBUG))
            .transport(Mail.Transport.SMTP)
            .host("localhost")
            .port(3025)
            .auth(false)
            .from(sender)
            .to(receiver)
            .cc(cc)
            .bcc(bcc)
            .subject("Test")
            .message(message)
            .mimeType(Mail.MimeType.HTML)
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
}
