
/*
 * Copyright (C) 2008 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.hl7;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents an HL7 message.
 */
@SuppressWarnings("serial")
public class HL7Message implements Serializable {

    /**
     * The character that separates segments in an HL7 message.
     */
    public static final char SEGMENT_TERMINATOR = '\r';

    private static final Pattern FIELD_NAME_PATTERN = Pattern.compile("([\\p{Alnum}]{3})\\.[0-9]+");

    private static final Pattern VALUE_NAME_PATTERN = Pattern.compile("(" + FIELD_NAME_PATTERN + ")(\\.([0-9]+)(\\.([0-9]+))?)?");

    /**
     * The segments in this message. This list always contains at least
     * one element, namely the {@link MSHSegment}.
     */
    protected final HL7SegmentList segments;

    /**
     * Construct a new HL7 message containing only the given MSH segment.
     *
     * @param msh MSH segment
     */
    public HL7Message(MSHSegment msh) {
        this.segments = new HL7SegmentList(msh);
    }

    /**
     * Construct an empty HL7 message.
     *
     * <p>
     * A MSH segment will be automatically added.
     *
     * @param seps separator and escape characters to use for this message
     */
    public HL7Message(HL7Seps seps) {
        this.segments = new HL7SegmentList(new MSHSegment(seps));
    }

    /**
     * Convenience constructor. Equivalent to:
     *  <blockquote>
     *  <code>HL7Message(HL7Seps.DEFAULT)</code>
     *  <blockquote>
     */
    public HL7Message() {
        this(HL7Seps.DEFAULT);
    }

    /**
     * Parsing constructor. Constructs an HL7 message by parsing the given string.
     * Segments must be separated with a carriage return character.
     *
     * @throws HL7ContentException if the string is invalid
     */
    public HL7Message(String msg) throws HL7ContentException {

        // Eliminate trailing CR's
        int len = msg.length();
        while (len > 0 && msg.charAt(len - 1) == SEGMENT_TERMINATOR)
            msg = msg.substring(0, --len);

        // Split message into segments
        int[] segs = HL7Util.find(msg, SEGMENT_TERMINATOR);

        // Get MSH segment with message-specific separator characters
        MSHSegment msh = new MSHSegment(msg.substring(0, segs[0]));
        this.segments = new HL7SegmentList(msh);
        HL7Seps seps = msh.getHL7Seps();

        // Workaround McKesson HBOC bug (extra mid-segment carriage returns)
        for (int i = 1; i < segs.length - 1; ) {
            if (segs[i] + 3 >= msg.length()
              || msg.charAt(segs[i] + 1) == seps.getFieldSep()
              || msg.charAt(segs[i] + 2) == seps.getFieldSep()
              || msg.charAt(segs[i] + 3) == seps.getFieldSep()) {
                msg = msg.substring(0, segs[i]) + msg.substring(segs[i] + 1);
                for (int j = i + 1; j < segs.length; j++)
                    segs[j]--;
                int[] segs2 = new int[segs.length - 1];
                System.arraycopy(segs, 0, segs2, 0, i);
                System.arraycopy(segs, i + 1, segs2, i, segs2.length - i);
                segs = segs2;
            } else
                i++;
        }

        // Add subsequent segments
        for (int i = 0; i < segs.length - 1; i++)
            this.segments.add(new HL7Segment(msg.substring(segs[i] + 1, segs[i + 1]), seps));
    }

    /**
     * Get the MSH segment of this message.
     *
     * @return this message's MSH segment (not a copy; changes are reflected back)
     */
    public MSHSegment getMSHSegment() {
        return (MSHSegment)this.segments.get(0);
    }

    /**
     * Get all of the segments in this message.
     *
     * <p>
     * The returned list does not allow removing the first element (the MSH segment) or changing it to anything other than an
     * {@link MSHSegment}.
     *
     * @return list of all message segments including MSH
     */
    public HL7SegmentList getSegments() {
        return this.segments;
    }

    /**
     * Get the first occurrence of a <code>segname</code> segment in this message.
     *
     * @param segname segment name, e.g., "PV1"
     * @param segnum starting segment index to start search;
     *  index zero refers to the MSH segment
     * @return first matching segment found, or null if not found
     * @throws IllegalArgumentException if segnum is negative
     */
    public HL7Segment findSegment(String segname, int segnum) {
        if (segnum < 0)
            throw new IllegalArgumentException("segnum=" + segnum);
        for (int i = segnum; i < this.segments.size(); i++) {
            HL7Segment seg = this.segments.get(i);
            if (seg.getName().equals(segname))
                return seg;
        }
        return null;
    }

    /**
     * Convenience method. Equivalent to:
     *  <blockquote>
     *  <code>findSegment(name, 0)</code>
     *  <blockquote>
     */
    public HL7Segment findSegment(String segname) {
        return this.findSegment(segname, 0);
    }

    /**
     * Find a field by HL7 name. The name is of the form XYZ.N where
     * "XYZ" is the segment name and "N" is the field number.
     *
     * @param name field name, e.g., "PV1.3"
     * @param segnum starting segment index (zero to start from MSH segment)
     * @return named HL7 field, or <code>null</code> if the named field doesn't exist in this message
     * @throws IllegalArgumentException if name is not properly formatted
     * @throws IllegalArgumentException if segnum is negative
     */
    public HL7Field getField(String name, int segnum) {

        // Check parameters
        Matcher matcher = FIELD_NAME_PATTERN.matcher(name);
        if (!matcher.matches())
            throw new IllegalArgumentException("invalid name `" + name + "'");
        int fieldIndex;
        try {
            fieldIndex = Integer.parseInt(name.substring(4), 10);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("invalid name `" + name + "'");
        }

        // Find segment, then field
        HL7Segment seg = this.findSegment(name.substring(0, 3), segnum);
        return seg != null ? seg.getField(fieldIndex) : null;
    }

    /**
     * Convenience method. Equivalent to:
     *  <blockquote>
     *  <code>getField(name, 0)</code>
     *  <blockquote>
     */
    public HL7Field getField(String name) {
        return this.getField(name, 0);
    }

    /**
     * Find a string value by HL7 name. The name is of the form {@code XYZ.N[.M[.L]]}
     * where {@code XYZ} is the segment name, {@code N} is the field number, {@code M} is the
     * optional component number, and {@code L} is the optional subcomponent number.
     *
     * <p>
     * If either of {@code M} or {@code L} are not given, they are assumed to be {@code 1}
     * so an unambiguous string value can always be returned.
     *
     * @param name HL7 value name, e.g., "PV1.3", "MSH.9.1", "ZZZ.3.2.1"
     * @param segnum starting segment index
     * @param repeat repeat index (starting from zero)
     * @return named HL7 value, or <code>null</code> if the named value doesn't exist in this message
     * @throws IllegalArgumentException if name is not properly formatted
     * @throws IllegalArgumentException if "M" or "L" is negative
     * @throws IllegalArgumentException if segnum or repeat is negative
     */
    public String get(String name, int segnum, int repeat) {

        // Find field first
        Matcher matcher = VALUE_NAME_PATTERN.matcher(name);
        if (!matcher.matches())
            throw new IllegalArgumentException("invalid name `" + name + "'");
        HL7Field field = this.getField(matcher.group(1), segnum);
        if (field == null)
            return null;

        // Get component and subcomponent indicies
        int component = 0;
        String value = matcher.group(4);
        if (value != null) {
            try {
                component = Integer.parseInt(value, 10) - 1;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("invalid component index `" + value + "'");
            }
        }
        int subcomponent = 0;
        value = matcher.group(6);
        if (value != null) {
            try {
                subcomponent = Integer.parseInt(value, 10) - 1;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("invalid subcomponent index `" + value + "'");
            }
        }

        // Return value (if any)
        return field.get(repeat, component, subcomponent);
    }

    /**
     * Convenience method. Equivalent to:
     *  <blockquote>
     *  <code>getField(name, 0, 0)</code>
     *  <blockquote>
     */
    public String get(String name) {
        return this.get(name, 0, 0);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null && obj.getClass() != getClass())
            return false;
        HL7Message that = (HL7Message)obj;
        return this.segments.equals(that.segments);
    }

    @Override
    public int hashCode() {
        return this.segments.hashCode();
    }

    /**
     * Convert this message into a string by concatenating the segments in string form, each terminated with a carriage return.
     */
    @Override
    public String toString() {
        return this.toString(getMSHSegment().getHL7Seps());
    }

    /**
     * Format this message using the supplied separators instead of the ones defined by the MSH segment. If <code>seps</code>
     * does not define an escape character, then characters that need to be escaped are silently elided.
     */
    public String toString(HL7Seps seps) {
        StringBuilder buf = new StringBuilder();
        for (HL7Segment segment : this.segments) {
            segment.append(buf, seps);
            buf.append(SEGMENT_TERMINATOR);
        }
        return buf.toString();
    }
}

