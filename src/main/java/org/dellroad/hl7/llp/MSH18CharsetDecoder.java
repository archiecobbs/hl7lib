
/*
 * Copyright (C) 2008 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.hl7.llp;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dellroad.hl7.HL7ContentException;
import org.dellroad.hl7.HL7Message;
import org.dellroad.hl7.MSHSegment;

/**
 * Attempts to determine HL7 message character encoding by decoding header field {@code MSH.18},
 * if present, or falling back to a configured default as a last resort.
 *
 * <p>
 * Instances are thread safe.
 */
public class MSH18CharsetDecoder implements CharsetDecoder {

    protected final Charset defaultCharset;

    /**
     * Constructor using {@code ISO-8859-1} as the default charset.
     */
    public MSH18CharsetDecoder() {
        this(StandardCharsets.ISO_8859_1);
    }

    /**
     * Primary constructor.
     *
     * @param defaultCharset default character encoding to use if unable to determine from {@code MSH.18}
     * @throws IllegalArgumentException if {@code defaultCharset} is null
     */
    public MSH18CharsetDecoder(Charset defaultCharset) {
        if (defaultCharset == null)
            throw new IllegalArgumentException("null defaultCharset");
        this.defaultCharset = defaultCharset;
    }

// CharsetDecoder

    @Override
    public Charset charsetForIncomingMessage(byte[] buf, int off, int len) {

        // Sanity check
        if (buf == null)
            throw new IllegalArgumentException("null buf");

        // Find the end of the MSH segment
        int end = off;
        while (true) {
            if (end == off + len)
                return this.defaultCharset;
            if (buf[end] == (byte)HL7Message.SEGMENT_TERMINATOR)
                break;
            end++;
        }

        // Try to parse MSH header as plain ASCII
        final MSHSegment msh;
        try {
            msh = new MSHSegment(new String(buf, off, end - off, StandardCharsets.US_ASCII));
        } catch (HL7ContentException e) {
            return this.defaultCharset;
        }

        // Decode MSH.18, if possible
        return this.decodeMSH18(msh);
    }

    @Override
    public Charset charsetForOutgoingMessage(HL7Message msg) {

        // Sanity check
        if (msg == null)
            throw new IllegalArgumentException("null msg");

        // Decode MSH.18, if possible
        return this.decodeMSH18(msg.getMSHSegment());
    }

// Other Methods

    /**
     * Read and decode the character encoding name from MSH.18, if possible.
     *
     * @param msh MSH segment
     * @return decoded character encoding, or the default if unable
     */
    protected Charset decodeMSH18(MSHSegment msh) {
        return Optional.ofNullable(msh)
          .map(m -> m.getField(18))
          .map(f -> f.get(0, 0, 0))
          .map(this::decodeCharsetName)
          .orElse(this.defaultCharset);
    }

    /**
     * Decode a character encoding name found in {@code MSH.18}.
     *
     * @param name character set name from {@code MSH.18}
     * @return decoded {@link Charset}, or null if unable to decode
     * @throws IllegalArgumentException if {@code label} is null
     */
    public Charset decodeCharsetName(String name) {

        // ASCII?
        if (name.equalsIgnoreCase("ASCII"))
            return StandardCharsets.US_ASCII;

        // Try 8859 variants
        if (name.startsWith("8859/")) {
            try {
                return Charset.forName("ISO-8859-" + name.substring(5));
            } catch (UnsupportedCharsetException e) {
                // ignore
            }
        }

        // Try Unicode variants
        final Matcher matcher = Pattern.compile("(?i)(UNICODE )?(UTF-(8|16|32).*)").matcher(name);
        if (matcher.matches()) {
            try {
                return Charset.forName(matcher.group(2));
            } catch (UnsupportedCharsetException e) {
                // ignore
            }
        }

        // Try whatever
        try {
            return Charset.forName(name.replaceAll("[-/_]", "-"));
        } catch (UnsupportedCharsetException e) {
            // ignore
        }

        // We give up
        return null;
    }
}
