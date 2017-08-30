
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
 */
public class LLPOutputStream implements HL7Writer, Closeable {

    private final BufferedOutputStream outputStream;
    private final OutputStreamWriter outputWriter;

    /**
     * Constructor.
     *
     * <p>
     * Equivalent to: {@link #LLPOutputStream(OutputStream, Charset)}{@code (output, StandardCharsets.ISO_8859_1)}.
     *
     * @param output underlying output stream
     * @throws IllegalArgumentException if {@code output} is null
     */
    public LLPOutputStream(OutputStream output) {
        this(output, StandardCharsets.ISO_8859_1);
    }

    /**
     * Constructor.
     *
     * @param output underlying output stream
     * @param charset character encoding for messages
     * @throws IllegalArgumentException if either parameter is null
     */
    public LLPOutputStream(OutputStream output, Charset charset) {
        if (output == null)
            throw new IllegalArgumentException("null output");
        if (charset == null)
            throw new IllegalArgumentException("null charset");
        this.outputStream = new BufferedOutputStream(output);
        this.outputWriter = new OutputStreamWriter(this.outputStream, charset);
    }

    /**
     * Write a message using HL7 LLP framing and flush the underlying output.
     */
    public void writeMessage(HL7Message message) throws IOException {

        // Write start byte
        this.outputStream.write(LLPConstants.LEADING_BYTE);

        // Write message segments
        HL7Seps seps = message.getMSHSegment().getHL7Seps();
        StringBuilder buf = new StringBuilder();
        for (HL7Segment segment : message.getSegments()) {
            segment.append(buf, seps);
            buf.append(HL7Message.SEGMENT_TERMINATOR);
            this.outputWriter.write(buf.toString());
            buf.setLength(0);
        }
        this.outputWriter.flush();

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
        this.outputWriter.close();
    }
}

