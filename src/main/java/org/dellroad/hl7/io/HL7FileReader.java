
/*
 * Copyright (C) 2008 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.hl7.io;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import org.dellroad.hl7.HL7ContentException;
import org.dellroad.hl7.HL7Message;
import org.dellroad.hl7.HL7Reader;
import org.dellroad.hl7.HL7Segment;
import org.dellroad.hl7.HL7Seps;
import org.dellroad.hl7.MSHSegment;

/**
 * Reads in HL7 messages in "file format".
 *
 * <p>
 * The "file format" is as follows:
 *  <ul>
 *  <li>Each message begins with an {@code MSH} segment</li>
 *  <li>Each segment is terminated by a CR, LF, or CR-LF</li>
 *  <li>Blank and whitespace-only lines are ignored</li>
 *  <li>Lines starting with '#' are ignored</li>
 *  </ul>
 */
public class HL7FileReader implements HL7Reader, Closeable {

    /**
     * The underlying reader.
     */
    protected final BufferedReader reader;

    private String nextLine;
    private boolean closed;

    /**
     * Constructor.
     *
     * @param in underlying reader
     */
    public HL7FileReader(Reader in) {
        if (in == null)
            throw new IllegalArgumentException("null in");
        this.reader = new BufferedReader(in);
    }

    /**
     * Convenience constructor. Equivalent to:
     *  <blockquote>
     *  <code>HL7FileReader(new InputStreamReader(in, StandardCharsets.ISO_8859_1))</code>
     *  </blockquote>
     *
     * @param in underlying input stream
     */
    public HL7FileReader(InputStream in) {
        this(new InputStreamReader(in, StandardCharsets.ISO_8859_1));
    }

    /**
     * Read next message from the underlying stream.
     *
     * @throws EOFException if there is no more input
     * @throws HL7ContentException if a malformed message is read
     * @throws IOException if an error occurs on the underlying stream
     */
    public HL7Message readMessage() throws IOException, HL7ContentException {
        String line = nextLine();
        if (line == null)
            throw new EOFException();
        MSHSegment msh = new MSHSegment(line);
        HL7Seps seps = msh.getHL7Seps();
        HL7Message message = new HL7Message(msh);
        while ((line = nextLine()) != null) {
            if (line.startsWith(MSHSegment.MSH_SEGMENT_NAME)) {
                this.pushback(line);
                break;
            }
            message.getSegments().add(new HL7Segment(line, seps));
        }
        return message;
    }

    /**
     * Save the line for next time.
     *
     * @param line line to push back
     * @throws RuntimeException if there is already one saved
     */
    protected void pushback(String line) {
        if (this.nextLine != null)
            throw new RuntimeException("internal error");
        this.nextLine = line;
    }

    /**
     * Read next non-ignorable line.
     *
     * @return next relevant line, or null for EOF
     * @throws IOException if an I/O error occurs
     */
    protected String nextLine() throws IOException {
        String line = this.nextLine;
        this.nextLine = null;
        if (line != null)
            return line;
        if (this.closed)
            return null;
        while (true) {
            line = this.reader.readLine();
            if (line == null)
                return null;
            if (line.length() == 0 || line.charAt(0) == '#')
                continue;
            if (line.matches("\\s+"))
                continue;
            return line;
        }
    }

    /**
     * Closes the underlying reader.
     */
    @Override
    public void close() throws IOException {
        this.reader.close();
        this.nextLine = null;
        this.closed = true;
    }
}

