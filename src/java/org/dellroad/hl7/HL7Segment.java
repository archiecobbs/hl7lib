
/*
 * Copyright (C) 2008 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.hl7;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Represents one segment in an HL7 message.
 */
@SuppressWarnings("serial")
public class HL7Segment implements Serializable {

    protected final ArrayList<HL7Field> fields = new ArrayList<HL7Field>();

    /**
     * Constructor for {@link MSHSegment} use only.
     */
    HL7Segment() {
    }

    /**
     * Constructor that initializes the name of the segment only.
     *
     * @param name segment name
     * @throws HL7ContentException if the name is an invalid segment name
     */
    public HL7Segment(String name) throws HL7ContentException {
        this.setName(name);
    }

    /**
     * Constructor taking segment name and explicit fields.
     *
     * @param name segment name
     * @param fields zero or more segment fields
     * @throws HL7ContentException if the name is an invalid segment name
     * @throws IllegalArgumentException if any field in the array is null
     */
    public HL7Segment(String name, HL7Field[] fields) throws HL7ContentException {
        this(name);
        for (HL7Field field : fields) {
            if (field == null)
                throw new IllegalArgumentException("null field");
            this.fields.add(field);
        }
    }

    /**
     * Constructor that parses a segment line.
     *
     * @param line segment line
     * @param seps separator and escape characters
     * @throws HL7ContentException if the segment name is invalid
     */
    public HL7Segment(String line, HL7Seps seps) throws HL7ContentException {
        this.parseAndAddFields(line, seps);
    }

    /**
     * Copy constructor.
     */
    public HL7Segment(HL7Segment segment) {
        for (HL7Field field : segment.getFields())
            this.appendField(new HL7Field(field));
    }

    /**
     * Get segment name, e.g., <code>MSH</code>, <code>PV1</code>, etc.
     * Equivalent to:
     * <blockquote>
     * <code>getField(0).get(0, 0, 0)</code>
     * </blockquote>
     */
    public String getName() {
        return this.fields.get(0).get(0, 0, 0);
    }

    /**
     * Set segment name.
     *
     * @param name new segment name
     * @throws HL7ContentException if the name is an invalid segment name
     */
    public void setName(String name) throws HL7ContentException {
        this.checkSegmentName(name);
        HL7Field field = new HL7Field(name);
        if (this.fields.isEmpty())
            this.fields.add(field);
        else
            this.fields.set(0, field);
    }

    /**
     * Get number of fields (including segment name).
     */
    public int getNumFields() {
        return this.fields.size();
    }

    /**
     * Add a field to the end of this segment.
     *
     * @param field field to add
     * @throws IllegalArgumentException if field is null
     */
    public void appendField(HL7Field field) {
        if (field == null)
            throw new IllegalArgumentException("field is null");
        this.fields.add(field);
    }

    /**
     * Set a field in this segment. If the segment doesn't have <code>index</code> fields, intermediate empty fields will
     * be added.
     *
     * <p>
     * This method cannot be used to set the segment name. Use {@link #setName} for that.
     *
     * @param field field to set
     * @param index index of the field in the segment (where zero is the segment name); must be at least one
     * @throws IllegalArgumentException if field is null
     * @throws IllegalArgumentException if index is zero or less
     */
    public void setField(int index, HL7Field field) {
        if (field == null)
            throw new IllegalArgumentException("field is null");
        if (index < 1)
            throw new IllegalArgumentException("index=" + index);
        while (this.fields.size() < index + 1)
            this.fields.add(HL7Field.EMPTY);
        this.fields.set(index, field);
    }

    /**
     * Convenience method. Equivalent to:
     * <blockquote>
     * <code>setField(index, new HL7Field(value))</code>
     * </blockquote>
     *
     * @param value
     */
    public void setField(int index, String value) {
        this.setField(index, new HL7Field(value));
    }

    /**
     * Get a field by "HL7 index". E.g., if this message is "ABC", an index of 3 would return ABC.3. If index is zero, the name
     * of this segment is returned.
     *
     * @param index of the requested field
     * @return the requested field, or null if not present in this segment
     * @throws IllegalArgumentException if <code>index</code> is negative
     */
    public HL7Field getField(int index) {
        if (index < 0)
            throw new IllegalArgumentException("index=" + index);
        if (index >= this.fields.size())
            return null;
        return this.fields.get(index);
    }

    /**
     * Get all of the fields, starting with the segment name.
     */
    public HL7Field[] getFields() {
        return this.fields.toArray(new HL7Field[this.fields.size()]);
    }

    /**
     * Trim off all but the first <code>size</code> fields. If the current number of fields is already
     * less than or equal to <code>size</code> then nothing happens.
     *
     * @throws IllegalArgumentException if <code>size</code> is less than one
     */
    public void trimTo(int size) {
        if (size < 1)
            throw new IllegalArgumentException("size < 1");
        while (size < this.fields.size())
            this.fields.remove(this.fields.size() - 1);
        this.fields.trimToSize();
    }

    /**
     * Append string encoding of this segment to the provided buffer.
     *
     * @param seps HL7 separator and escape characters
     */
    public void append(StringBuilder buf, HL7Seps seps) {
        boolean first = true;
        for (HL7Field field : this.fields) {
            if (first)
                first = false;
            else
                buf.append(seps.getFieldSep());
            field.append(buf, seps);
        }
    }

    /**
     * Convert to a string using the provided separators.
     *
     * @param seps HL7 separator and escape characters
     */
    public String toString(HL7Seps seps) {
        StringBuilder buf = new StringBuilder();
        this.append(buf, seps);
        return buf.toString();
    }

    /**
     * Convert to a string using {@link HL7Seps#DEFAULT}.
     */
    @Override
    public String toString() {
        return this.toString(HL7Seps.DEFAULT);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass())
            return false;
        HL7Segment that = (HL7Segment)obj;
        return this.fields.equals(that.fields);
    }

    @Override
    public int hashCode() {
        return this.fields.hashCode();
    }

    /**
     * Parse fields from given line and add them.
     *
     * @param line segment or segment fragment
     * @param seps separator and escape characters
     * @throws HL7ContentException if this segment contains zero fields and the first field is not a valid segment name
     */
    protected void parseAndAddFields(String line, HL7Seps seps) throws HL7ContentException {
        int[] fieldSeps = HL7Util.find(line, seps.getFieldSep());
        int posn = 0;
        for (int i = 0; i < fieldSeps.length; i++) {
            String string = line.substring(posn, fieldSeps[i]);
            if (this.fields.isEmpty())
                this.checkSegmentName(string);
            this.fields.add(new HL7Field(string, seps));
            posn = fieldSeps[i] + 1;
        }
    }

    /**
     * Verify segment name is legit.
     */
    private void checkSegmentName(String name) throws HL7ContentException {
        if (name.length() != 3)
            throw new HL7ContentException("invalid segment name `" + name + "'");
    }
}

