
/*
 * Copyright (C) 2008 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.hl7.llp;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.dellroad.hl7.HL7Message;
import org.dellroad.hl7.HL7Segment;
import org.dellroad.hl7.HL7Seps;
import org.dellroad.hl7.HL7Writer;

/**
 * Writes out HL7 messages using the "lower layer protocol".
 *
 * <p>
 * Instances are not thread safe.
 */
public class LLPOutputStream implements HL7Writer, Closeable {

    private final BufferedOutputStream outputStream;
    private final CharsetDecoder charsetDecoder;

    /**
     * Constructor for when {@link StandardCharsets#ISO_8859_1} character encoding is to be used for all messages.
     *
     * <p>
     * Equivalent to: {@link #LLPOutputStream(OutputStream, Charset)
     *  LLPOutputStream}{@code (output, }{@link StandardCharsets#ISO_8859_1 StandardCharsets.ISO_8859_1}{@code )}.
     *
     * @param output underlying output stream
     * @throws IllegalArgumentException if {@code output} is null
     */
    public LLPOutputStream(OutputStream output) {
        this(output, StandardCharsets.ISO_8859_1);
    }

    /**
     * Constructor for when a fixed character encoding is to be used for all messages.
     *
     * @param output underlying output stream
     * @param charset character encoding for all messages
     * @throws IllegalArgumentException if either parameter is null
     */
    public LLPOutputStream(OutputStream output, Charset charset) {
        this(output, CharsetDecoder.fixed(charset));
    }

    /**
     * Primary constructor.
     *
     * @param output underlying output stream
     * @param charsetDecoder determines the character encoding to use for each outgoing message
     * @throws IllegalArgumentException if either parameter is null
     */
    public LLPOutputStream(OutputStream output, CharsetDecoder charsetDecoder) {
        if (output == null)
            throw new IllegalArgumentException("null output");
        if (charsetDecoder == null)
            throw new IllegalArgumentException("null charsetDecoder");
        this.outputStream = new BufferedOutputStream(output);
        this.charsetDecoder = charsetDecoder;
    }

    /**
     * Write a message using HL7 LLP framing and flush the underlying output.
     */
    public void writeMessage(HL7Message message) throws IOException {

        // Write start byte
        this.outputStream.write(LLPConstants.LEADING_BYTE);

        // Get character encoding
        final Charset charset = this.charsetDecoder.charsetForOutgoingMessage(message);
        if (charset == null)
            throw new LLPException("null character encoding returned by CharsetDecoder");

        // Write message segments
        final OutputStreamWriter writer = new OutputStreamWriter(this.outputStream, charset);
        final HL7Seps seps = message.getMSHSegment().getHL7Seps();
        final StringBuilder buf = new StringBuilder();
        for (HL7Segment segment : message.getSegments()) {
            segment.append(buf, seps);
            buf.append(HL7Message.SEGMENT_TERMINATOR);
            writer.write(buf.toString());
            buf.setLength(0);
        }
        writer.flush();

        // Write stop bytes
        this.outputStream.write(LLPConstants.TRAILING_BYTE_0);
        this.outputStream.write(LLPConstants.TRAILING_BYTE_1);
        this.outputStream.flush();
    }

    /**
     * Close this output stream.
     */
    @Override
    public void close() throws IOException {
        this.outputStream.close();
    }
}
