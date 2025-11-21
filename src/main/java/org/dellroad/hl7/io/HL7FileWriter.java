
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
 * Each segment is terminated by an end-of-segement character (default LF), and each message is terminated
 * by an end-of-message character (default LF).
 */
public class HL7FileWriter implements HL7Writer, Closeable {

    /** The default end-of-segment character (newline) **/
    public static final char DEFAULT_EOS = '\n';
    /** The default end-of-message character (newline) **/
    public static final char DEFAULT_EOM = '\n';

    /**
     * The underlying writer.
     */
    protected final BufferedWriter writer;

    /**
     * The end-of-segment character.
     */
    protected final char eos;

    /**
     * The end-of-message character.
     */
    protected final char eom;

    /**
     * Constructor.
     *
     * @param out underlying writer
     * @throws IllegalArgumentException if {@code out} is null
     */
    public HL7FileWriter(Writer out) {
        this(out, DEFAULT_EOS, DEFAULT_EOM);
    }

    /**
     * Convenience constructor. Equivalent to:
     *  <blockquote>
     *  <code>HL7FileWriter(new OutputStreamWriter(out, StandardCharsets.ISO_8859_1))</code>
     *  </blockquote>
     *
     * @param out underlying output
     * @throws IllegalArgumentException if {@code out} is null
     */
    public HL7FileWriter(OutputStream out) {
        this(new OutputStreamWriter(HL7FileWriter.checkNull(out, "out"), StandardCharsets.ISO_8859_1));
    }

    /**
     * Constructor.
     *
     * @param out underlying writer
     * @param eos end-of-segment character
     * @param eom end-of-message character, or {@code '\u005Cu0000'} for none
     * @throws IllegalArgumentException if {@code eos} or {@code eom} is not a
     *  {@linkplain Character#isISOControl control character}.
     * @throws IllegalArgumentException if {@code out} is null
     */
    public HL7FileWriter(Writer out, char eos, char eom) {
        HL7FileWriter.checkNull(out, "out");
        if (!Character.isISOControl(eos))
            throw new IllegalArgumentException("invalid EOS character");
        if (!Character.isISOControl(eom))
            throw new IllegalArgumentException("invalid EOM character");
        this.writer = new BufferedWriter(out);
        this.eos = eos;
        this.eom = eom;
    }

    /**
     * Convenience constructor. Equivalent to:
     *  <blockquote>
     *  <code>HL7FileWriter(new OutputStreamWriter(out, StandardCharsets.ISO_8859_1), eos, eom)</code>
     *  </blockquote>
     *
     * @param out underlying output
     * @param eos end-of-segment character
     * @param eom end-of-message character, or {@code '\u005Cu0000'} for none
     * @throws IllegalArgumentException if {@code eos} or {@code eom} is not a
     *  {@linkplain Character#isISOControl control character}.
     * @throws IllegalArgumentException if {@code out} is null
     */
    public HL7FileWriter(OutputStream out, char eos, char eom) {
        this(new OutputStreamWriter(HL7FileWriter.checkNull(out, "out"), StandardCharsets.ISO_8859_1), eos, eom);
    }

    private static <T> T checkNull(T obj, String name) {
        if (obj == null)
            throw new IllegalArgumentException("null " + name);
        return obj;
    }

    /**
     * Write the given message to the underlying output (and then flush it).
     */
    public void writeMessage(HL7Message message) throws IOException {
        HL7Seps seps = message.getMSHSegment().getHL7Seps();
        for (HL7Segment segment : message.getSegments()) {
            this.writer.write(segment.toString(seps));
            this.writer.write(this.eos);
        }
        if (this.eom != '\0')
            this.writer.write(this.eom);
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
