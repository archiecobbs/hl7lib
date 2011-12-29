
/*
 * Copyright (C) 2008 Archie L. Cobbs. All rights reserved.
 *
 * $Id$
 */

package org.dellroad.hl7.llp;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.dellroad.hl7.HL7ContentException;
import org.dellroad.hl7.HL7Message;
import org.dellroad.hl7.HL7Reader;

/**
 * Reads HL7 messages framed in the "lower layer protocol" (LLP).
 */
public class LLPInputStream implements HL7Reader {

    private static final int MIN_BUFLEN = 1024;
    private static final int MAX_BUFLEN = 16 * 1024;

    private final BufferedInputStream inputStream;
    private final int maxLength;

    private byte[] buf = new byte[MIN_BUFLEN];

    /**
     * Constructor.
     *
     * @param in underlying input stream
     * @param maxLength maximum allowed message length
     * @throws IllegalArgumentException if out is null
     * @throws IllegalArgumentException if maxLength is negative
     */
    public LLPInputStream(InputStream in, int maxLength) {
        if (in == null)
            throw new IllegalArgumentException("in is null");
        if (maxLength < 0)
            throw new IllegalArgumentException("maxLength is negative");
        this.inputStream = new BufferedInputStream(in);
        this.maxLength = maxLength;
    }

    /**
     * Read next message from the underlying stream.
     *
     * @throws EOFException if there is no more input
     * @throws HL7ContentException if a malformed message is read
     * @throws LLPException if illegal framing byte(s) are read from the underlying stream, or the message is too long
     * @throws IOException if an error occurs on the underlying stream
     */
    public HL7Message readMessage() throws IOException, HL7ContentException {

        // Read leading byte
        this.readByte(LLPConstants.LEADING_BYTE);

        // Read message until first trailing byte
        int len = 0;
        while (true) {
            int ch;
            if ((ch = this.inputStream.read()) == -1)
                throw new EOFException();
            if (ch == LLPConstants.TRAILING_BYTE_0)
                break;
            if (len >= this.maxLength)
                throw new LLPException("message is too long (greater than " + this.maxLength + " bytes)");
            if (len == this.buf.length) {
                byte[] newbuf = new byte[this.buf.length * 2];
                System.arraycopy(this.buf, 0, newbuf, 0, len);
                this.buf = newbuf;
            }
            this.buf[len++] = (byte)ch;
        }

        // Read second trailing byte
        this.readByte(LLPConstants.TRAILING_BYTE_1);

        // Extract message text
        String text;
        try {
            text = new String(buf, 0, len, LLPConstants.CHARACTER_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } finally {
            if (this.buf.length > MAX_BUFLEN)
                this.buf = new byte[MIN_BUFLEN];
        }

        // Return parsed message
        return new HL7Message(text);
    }

    private void readByte(int value) throws IOException {
        int ch;
        if ((ch = this.inputStream.read()) == -1)
            throw new EOFException();
        if (ch != value) {
            throw new LLPException("expected to read 0x"
              + Integer.toHexString(value) + " but read 0x"
              + Integer.toHexString(ch) + " instead");
        }
    }

    /**
     * Close the underlying stream.
     */
    public void close() throws IOException {
        this.inputStream.close();
    }
}

