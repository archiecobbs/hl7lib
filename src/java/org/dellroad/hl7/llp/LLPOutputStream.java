
/*
 * Copyright (C) 2008 Archie L. Cobbs. All rights reserved.
 *
 * $Id$
 */

package org.dellroad.hl7.llp;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import org.dellroad.hl7.HL7Message;
import org.dellroad.hl7.HL7Segment;
import org.dellroad.hl7.HL7Seps;
import org.dellroad.hl7.HL7Writer;

/**
 * Writes out HL7 messages using the "lower layer protocol".
 */
public class LLPOutputStream implements HL7Writer {

    private final BufferedOutputStream outputStream;
    private final OutputStreamWriter outputWriter;

    /**
     * Constructor.
     *
     * @param out underlying output stream
     * @throws IllegalArgumentException if out is null
     */
    public LLPOutputStream(OutputStream out) {
        if (out == null)
            throw new IllegalArgumentException("out is null");
        this.outputStream = new BufferedOutputStream(out);
        try {
            this.outputWriter = new OutputStreamWriter(this.outputStream, LLPConstants.CHARACTER_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Write a message using HL7 LLP framing.
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
    public void close() throws IOException {
        this.outputWriter.close();
    }
}

