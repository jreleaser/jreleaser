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
package org.jreleaser.util.io;

import java.io.IOException;
import java.io.InputStream;

import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 * @since 1.0.0
 */
public class RepeatableInputStream extends InputStream {
    private final InputStream delegate;
    private final int bufferSize;
    private int bufferOffset = 0;
    private long bytesReadPastMark = 0;
    private byte[] buffer;

    public RepeatableInputStream(InputStream inputStream, int bufferSize) {
        this.delegate = requireNonNull(inputStream, "'inputStream' must not be null");
        this.bufferSize = bufferSize;
        this.buffer = new byte[this.bufferSize];
    }

    public RepeatableInputStream(InputStream inputStream) {
        this(inputStream, 131072);
    }

    @Override
    public void reset() throws IOException {
        if (bytesReadPastMark <= bufferSize) {
            bufferOffset = 0;
        } else {
            throw new IOException("Cannot reset as " + this.bytesReadPastMark +
                "bytes have been written, more than current buffer size of" + this.bufferSize);
        }
    }

    @Override
    public synchronized void mark(int readlimit) {
        if (bytesReadPastMark <= bufferSize && buffer != null) {
            byte[] newBuffer = new byte[this.bufferSize];
            System.arraycopy(buffer, bufferOffset, newBuffer, 0, (int) (bytesReadPastMark - bufferOffset));
            this.buffer = newBuffer;
            this.bytesReadPastMark -= bufferOffset;
            this.bufferOffset = 0;
        } else {
            this.bufferOffset = 0;
            this.bytesReadPastMark = 0;
            this.buffer = new byte[this.bufferSize];
        }
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public int available() throws IOException {
        return delegate.available();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        byte[] tmp = new byte[len];

        if (bufferOffset < bytesReadPastMark && buffer != null) {
            int bytesFromBuffer = tmp.length;
            if (bufferOffset + bytesFromBuffer > bytesReadPastMark) {
                bytesFromBuffer = (int) bytesReadPastMark - bufferOffset;
            }

            System.arraycopy(buffer, bufferOffset, b, off, bytesFromBuffer);
            bufferOffset += bytesFromBuffer;
            return bytesFromBuffer;
        }

        int count = delegate.read(tmp);

        if (count <= 0) {
            return count;
        }

        if (bytesReadPastMark + count <= bufferSize) {
            System.arraycopy(tmp, 0, buffer, (int) bytesReadPastMark, count);
            bufferOffset += count;
        } else if (buffer != null) {
            buffer = null;
        }

        System.arraycopy(tmp, 0, b, off, count);
        bytesReadPastMark += count;

        return count;
    }

    @Override
    public int read() throws IOException {
        byte[] tmp = new byte[1];
        int count = read(tmp);
        if (count != -1) {
            return tmp[0];
        } else {
            return count;
        }
    }
}