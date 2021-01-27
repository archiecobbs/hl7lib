
/*
 * Copyright (C) 2008 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.hl7.llp;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.dellroad.hl7.HL7ContentException;
import org.dellroad.hl7.HL7Message;
import org.dellroad.hl7.HL7Reader;

/**
 * Reads HL7 messages framed in the "lower layer protocol" (LLP).
 *
 * <p>
 * Instances are not thread safe.
 */
public class LLPInputStream implements HL7Reader, Closeable {

    private static final int MIN_BUFLEN = 1024;
    private static final int MAX_BUFLEN = 16 * 1024;

    private final BufferedInputStream inputStream;
    private final Charset charset;
    private final int maxLength;

    private byte[] buf = new byte[MIN_BUFLEN];

    /**
     * Constructor for when {@link StandardCharsets#ISO_8859_1} character encoding is to be used for all messages.
     *
     * <p>
     * Equivalent to:
     * {@link #LLPInputStream(InputStream, Charset, int)
     *  LLPInputStream}{@code (input, }{@link StandardCharsets#ISO_8859_1 StandardCharsets.ISO_8859_1}{@code , maxLength)}.
     *
     * @param input underlying input stream
     * @param maxLength maximum allowed message length
     * @throws IllegalArgumentException if {@code input} is null
     * @throws IllegalArgumentException if maxLength is negative
     */
    public LLPInputStream(InputStream input, int maxLength) {
        this(input, StandardCharsets.ISO_8859_1, maxLength);
    }

    /**
     * Constructor.
     *
     * @param input underlying input stream
     * @param maxLength maximum allowed message length
     * @param charset character encoding for messages
     * @throws IllegalArgumentException if any parameter is null
     * @throws IllegalArgumentException if maxLength is negative
     */
    public LLPInputStream(InputStream input, Charset charset, int maxLength) {
        if (input == null)
            throw new IllegalArgumentException("null input");
        if (charset == null)
            throw new IllegalArgumentException("null charset");
        if (maxLength < 0)
            throw new IllegalArgumentException("maxLength is negative");
        this.inputStream = new BufferedInputStream(input);
        this.charset = charset;
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
        String text = new String(buf, 0, len, this.charset);
        if (this.buf.length > MAX_BUFLEN)
            this.buf = new byte[MIN_BUFLEN];

        // Return parsed message
        try {
            return new HL7Message(text);
        } catch (HL7ContentException e) {
            throw e.setContent(text);
        }
    }

    /**
     * Advance past the end of the current message. This method can be used (for example) to skip over the remaining portion
     * of a badly framed message that resulted in a {@link LLPException} in an attempt to salvage the connection.
     *
     * <p>
     * This method just reads until the next occurrence of a {@link LLPConstants#TRAILING_BYTE_0} byte
     * followed immediately by a {@link LLPConstants#TRAILING_BYTE_1} byte, then returns.
     *
     * @throws EOFException if there is no more input
     * @throws IOException if an error occurs on the underlying stream
     */
    public void skip() throws IOException {
        int state = 0;
        while (true) {
            int ch = this.inputStream.read();
            if (ch == -1)
                throw new EOFException();
            if (state == 0) {
                if (ch == LLPConstants.TRAILING_BYTE_0)
                    state = 1;
            } else {
                if (ch == LLPConstants.TRAILING_BYTE_1)
                    return;
                state = 0;
            }
        }
    }

    private void readByte(int value) throws IOException {
        int ch;
        if ((ch = this.inputStream.read()) == -1)
            throw new EOFException();
        if (ch != value) {
            String expected = String.format("0x%02x", value);
            String actual = String.format("0x%02x", ch);
            throw new LLPException("expected to read " + expected + " but read " + actual + " instead");
        }
    }

    /**
     * Close the underlying stream.
     */
    @Override
    public void close() throws IOException {
        this.inputStream.close();
    }
}
