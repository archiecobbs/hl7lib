
/*
 * Copyright (C) 2008 Archie L. Cobbs. All rights reserved.
 *
 * $Id$
 */

package org.dellroad.hl7;

/**
 * Utility methods.
 */
public final class HL7Util {

    private HL7Util() {
    }

    /**
     * Convenience method. Equivalent to:
     *  <blockquote>
     *  <code>find(value, sep, 0, sep.length())</code>
     *  </blockquote>
     */
    public static int[] find(String value, char sep) {
        return find(value, sep, 0, value.length());
    }

    /**
     * Convenience method. Equivalent to:
     *  <blockquote>
     *  <code>find(value, sep, start, sep.length())</code>
     *  </blockquote>
     */
    public static int[] find(String value, char sep, int start) {
        return find(value, sep, start, value.length());
    }

    /**
     * Find all occurrences of the separator character in the sub-string.
     *
     * @param value entire string
     * @param sep separator character to search for
     * @param start starting index of sub-string to search (inclusive)
     * @param end ending index of sub-string to search (exclusive)
     * @return indexes of all occurrences of <code>sep</code>, in order, plus one extra index equal to <code>end</code>
     * @throws StringIndexOutOfBoundsException if <code>start</code> is less than zero or <code>end</code> is
     *  greater than the length of <code>value</code>
     */
    public static int[] find(String value, char sep, int start, int end) {

        // Count occurrences
        int count = 0;
        for (int i = start; i < end; i++) {
            if (value.charAt(i) == sep)
                count++;
        }

        // Allocate array and record them
        int[] positions = new int[count + 1];
        if (count > 0) {
            int j = 0;
            for (int i = start; i < end; i++) {
                if (value.charAt(i) == sep) {
                    positions[j++] = i;
                    if (j == count)
                        break;
                }
            }
        }

        // Done
        positions[count] = end;
        return positions;
    }
}

