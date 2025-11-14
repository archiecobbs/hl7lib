
/*
 * Copyright (C) 2008 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.hl7;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Represents one field in an HL7 message, possibly with repeats.
 *
 * <p>
 * Instances of this class are "immutable"; however, for this to be
 * true the calling application must not alter the arrays passed to
 * the constructor or returned from any of the methods.
 */
@SuppressWarnings("serial")
public final class HL7Field implements Serializable {

    /**
     * The empty field.
     */
    public static final HL7Field EMPTY = new HL7Field("");

    /**
     * Field value, with dimensions being: repeats, components, and sub-components.
     */
    protected final String[][][] value;

    /**
     * Constructor.
     *
     * @param value simple string value for this field; null is treated like the empty string
     */
    public HL7Field(String value) {
        this(new String[][][] { { { value } } });
    }

    /**
     * Constructor used to create a field containing zero or more repeated values, each a simple string value.
     *
     * <p>
     * Note: the {@code repeats} array is not copied by this constructor, so the caller should not modify the array.
     *
     * @param repeats repeat values for this field; null is treated like the empty string
     * @throws IllegalArgumentException if <code>repeats</code> array has length zero
     */
    public HL7Field(String[] repeats) {
        this.value = new String[repeats.length][1][1];
        for (int i = 0; i < repeats.length; i++)
            this.value[i][0][0] = repeats[i];
        this.checkLengthsAndReplaceNulls();
    }

    /**
     * Constructor used to create a field containing zero or more repeated values, each containing zero or more components,
     * each component containing zero or more sub-components.
     *
     * <p>
     * Note: the {@code repeats} array is not copied by this constructor, so the caller should not modify the array.
     *
     * @param repeats repeat array, each containing a component array, each containing a sub-component array; null in any
     *  String position is treated like the empty string
     * @throws NullPointerException if any intermediate array element (i.e., not of type String) is null
     * @throws IllegalArgumentException if <code>repeats</code> array or any sub-array has length zero
     */
    public HL7Field(String[][][] repeats) {
        this.value = repeats;
        this.checkLengthsAndReplaceNulls();
    }

    /**
     * Parsing constructor.
     *
     * <p>
     * Parses the encoded HL7 field. Non-custom escapes will be decoded, while custom escapes will be silently removed.
     *
     * @param field encoded HL7 field contents
     * @param seps HL7 separator characters
     */
    public HL7Field(String field, HL7Seps seps) {
        int[] repSeps = HL7Util.find(field, seps.getRepSep());
        this.value = new String[repSeps.length][][];
        int repPosn = 0;
        for (int i = 0; i < repSeps.length; i++) {
            int[] compSeps = HL7Util.find(field, seps.getCompSep(), repPosn, repSeps[i]);
            this.value[i] = new String[compSeps.length][];
            int compPosn = repPosn;
            for (int j = 0; j < compSeps.length; j++) {
                if (!seps.hasSubcomponentSeparator()) {
                    this.value[i][j] = new String[] { seps.unescape(field.substring(compPosn, compSeps[j])) };
                    continue;
                }
                int[] subSeps = HL7Util.find(field, seps.getSubSep(), compPosn, compSeps[j]);
                this.value[i][j] = new String[subSeps.length];
                int subPosn = compPosn;
                for (int k = 0; k < subSeps.length; k++) {
                    this.value[i][j][k] = seps.unescape(field.substring(subPosn, subSeps[k]));
                    subPosn = subSeps[k] + 1;
                }
                compPosn = compSeps[j] + 1;
            }
            repPosn = repSeps[i] + 1;
        }
    }

    /**
     * Copy constructor.
     *
     * <p>
     * This constructor performs a deep copy of <code>field</code>, so that subsequent changes to it do not affect this instance.
     *
     * @param field field to copy
     */
    public HL7Field(HL7Field field) {
        this.value = field.value.clone();
        for (int i = 0; i < this.value.length; i++) {
            this.value[i] = this.value[i].clone();
            for (int j = 0; j < this.value.length; j++)
                this.value[i][j] = this.value[i][j].clone();
        }
    }

    /**
     * Get field contents.
     *
     * <p>
     * The length of the returned array, each of its array elements, and each of their sub-array elements,
     * is guaranteed to be at least one.
     *
     * <p>
     * Note: the returned array is not a copy, so the caller should not modify it.
     *
     * @return array of repeats, each an array of components, each an array of sub-components, each a non-null String.
     */
    public String[][][] getValue() {
        return this.value;
    }

    /**
     * Determine whether this field is empty.
     *
     * @return true if this field is empty, otherwise false
     */
    public boolean isEmpty() {
        return EMPTY.equals(this);
    }

    /**
     * Get a specific field sub-component.
     *
     * @param repnum repeat number (zero-based)
     * @param compnum component number (zero-based)
     * @param subnum sub-component number (zero-based)
     * @return specified sub-component, or null if it does not exist
     * @throws IllegalArgumentException if any parameter is negative
     */
    public String get(int repnum, int compnum, int subnum) {
        if (repnum < 0 || compnum < 0 || subnum < 0)
            throw new IllegalArgumentException("negative index");
        try {
            return this.value[repnum][compnum][subnum];
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * Append properly separated and escaped version of this field
     * to the given buffer.
     *
     * @param buf string buffer
     * @param seps HL7 separator and escape characters
     */
    public void append(StringBuilder buf, HL7Seps seps) {
        for (int i = 0; i < this.value.length; i++) {
            if (i > 0)
                buf.append(seps.getRepSep());
            for (int j = 0; j < this.value[i].length; j++) {
                if (j > 0)
                    buf.append(seps.getCompSep());
                for (int k = 0; k < this.value[i][j].length; k++) {
                    if (k > 0)
                        buf.append(seps.getSubSep());
                    seps.escape(this.value[i][j][k], buf);
                }
            }
        }
    }

    /**
     * Convert to a string using the provided separators.
     *
     * @param seps HL7 separator and escape characters
     * @return this field properly separated and escaped with <code>seps</code>
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
        if (!(obj instanceof HL7Field))
            return false;
        HL7Field that = (HL7Field)obj;
        return Arrays.deepEquals(this.value, that.value);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(this.value);
    }

    private void checkLengthsAndReplaceNulls() {
        if (this.value.length == 0)
            throw new IllegalArgumentException("zero length array");
        for (int i = 0; i < this.value.length; i++) {
            if (this.value[i].length == 0)
                throw new IllegalArgumentException("zero length sub-array");
            for (int j = 0; j < this.value[i].length; j++) {
                if (this.value[i][j].length == 0)
                    throw new IllegalArgumentException("zero length sub-array");
                for (int k = 0; k < this.value[i][j].length; k++) {
                    String s = this.value[i][j][k];
                    this.value[i][j][k] = s != null ? s : "";
                }
            }
        }
    }
}

