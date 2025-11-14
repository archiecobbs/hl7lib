
/*
 * Copyright (C) 2008 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.hl7.io;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.dellroad.hl7.HL7Message;
import org.dellroad.hl7.HL7Segment;
import org.dellroad.hl7.HL7Seps;
import org.dellroad.hl7.HL7Writer;

/**
 * Writes out HL7 messages in "file format".
 *
 * <p>
 * Each segment is terminated with LF, and a blank line is added between messages.
 */
public class HL7FileWriter implements HL7Writer, Closeable {

    /**
     * The underlying writer.
     */
    protected final BufferedWriter writer;

    /**
     * Constructor.
     *
     * @param out underlying writer
     */
    public HL7FileWriter(Writer out) {
        if (out == null)
            throw new IllegalArgumentException("null out");
        this.writer = new BufferedWriter(out);
    }

    /**
     * Convenience constructor. Equivalent to:
     *  <blockquote>
     *  <code>HL7FileWriter(new OutputStreamWriter(out, StandardCharsets.ISO_8859_1))</code>
     *  </blockquote>
     *
     * @param out underlying input stream
     */
    public HL7FileWriter(OutputStream out) {
        this(new OutputStreamWriter(out, StandardCharsets.ISO_8859_1));
    }

    /**
     * Write the given message to the underlying output (and then flush it).
     */
    public void writeMessage(HL7Message message) throws IOException {
        HL7Seps seps = message.getMSHSegment().getHL7Seps();
        for (HL7Segment segment : message.getSegments()) {
            this.writer.write(segment.toString(seps));
            this.writer.write('\n');
        }
        this.writer.write('\n');
        this.writer.flush();
    }

    /**
     * Closes the underlying writer.
     */
    @Override
    public void close() throws IOException {
        this.writer.close();
    }
}

