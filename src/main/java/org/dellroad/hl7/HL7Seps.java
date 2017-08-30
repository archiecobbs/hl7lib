
/*
 * Copyright (C) 2008 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.hl7;

import java.io.Serializable;

/**
 * Container for the HL7 separator and escape characters for an HL7 message.
 */
@SuppressWarnings("serial")
public final class HL7Seps implements Serializable {

    public static final char DEFAULT_FIELD_SEPARATOR = '|';
    public static final char DEFAULT_COMPONENT_SEPARATOR = '^';
    public static final char DEFAULT_REPEAT_SEPARATOR = '~';
    public static final char DEFAULT_SUBCOMPONENT_SEPARATOR = '&';
    public static final char DEFAULT_ESCAPE_CHARACTER = '\\';

    public static final char FIELD_SEPARATOR_ESCAPE = 'F';
    public static final char COMPONENT_SEPARATOR_ESCAPE = 'S';
    public static final char REPEAT_SEPARATOR_ESCAPE = 'R';
    public static final char ESCAPE_CHARACTER_ESCAPE = 'E';
    public static final char SUBCOMPONENT_SEPARATOR_ESCAPE = 'T';
    public static final char HEX_DATA_ESCAPE = 'X';

    /**
     * Separator using the default HL7 separator and escape characters.
     */
    public static final HL7Seps DEFAULT;
    static {
        try {
            DEFAULT = new HL7Seps(DEFAULT_FIELD_SEPARATOR, DEFAULT_COMPONENT_SEPARATOR,
              DEFAULT_REPEAT_SEPARATOR, DEFAULT_ESCAPE_CHARACTER, DEFAULT_SUBCOMPONENT_SEPARATOR);
        } catch (HL7ContentException e) {
            throw new RuntimeException("impossible", e);
        }
    }

    private final char fieldSep;
    private final char compSep;
    private final char repSep;
    private final char escChar;
    private final char subSep;

    /**
     * Constructor.
     *
     * <p>
     * The sub-component separator and escape characters are optional and may be equal to <code>(char)0</code>
     * to indicate not defined. However, if a sub-component separator is defined, then so must be an escape
     * character.
     *
     * @param fieldSep field separator
     * @param compSep component separator
     * @param repSep repetition separator
     * @param escChar escape character, or <code>(char)0</code> for none
     * @param subSep subcomponent separator, or <code>(char)0</code> for none
     * @throws HL7ContentException if the characters are invalid according to {@link #validate}
     */
    public HL7Seps(char fieldSep, char compSep, char repSep, char escChar, char subSep) throws HL7ContentException {
        validate(fieldSep, compSep, repSep, escChar, subSep);
        this.fieldSep = fieldSep;
        this.compSep = compSep;
        this.repSep = repSep;
        this.escChar = escChar;
        this.subSep = subSep;
    }

    /**
     * Convenience constructor for when there is no sub-component separator.
     * Equivalent to:
     *  <blockquote>
     *  <code>HL7Seps(fieldSep, compSep, repSep, escChar, (char)0)</code>
     *  </blockquote>
     *
     * @param fieldSep field separator
     * @param compSep component separator
     * @param repSep repetition separator
     * @param escChar escape character, or <code>(char)0</code> for none
     * @throws HL7ContentException if the characters are invalid according to {@link #validate}
     */
    public HL7Seps(char fieldSep, char compSep, char repSep, char escChar) throws HL7ContentException {
        this(fieldSep, compSep, repSep, escChar, '\u0000');
    }

    /**
     * Convenience constructor for when there is no sub-component separator and no escape character.
     * Equivalent to:
     *  <blockquote>
     *  <code>HL7Seps(fieldSep, compSep, repSep, (char)0, (char)0)</code>
     *  </blockquote>
     *
     * @param fieldSep field separator
     * @param compSep component separator
     * @param repSep repetition separator
     * @throws HL7ContentException if the characters are invalid according to {@link #validate}
     */
    public HL7Seps(char fieldSep, char compSep, char repSep) throws HL7ContentException {
        this(fieldSep, compSep, repSep, '\u0000', '\u0000');
    }

    /**
     * Get the field separator character.
     *
     * @return field separator character
     */
    public char getFieldSep() {
        return this.fieldSep;
    }

    /**
     * Get the component separator character.
     *
     * @return component separator character
     */
    public char getCompSep() {
        return this.compSep;
    }

    /**
     * Get the repeat separator character.
     *
     * @return repeat separator character
     */
    public char getRepSep() {
        return this.repSep;
    }

    /**
     * Get the escape character.
     *
     * @return escape character, or <code>(char)0</code> for none
     * @see #hasEscapeCharacter
     */
    public char getEscChar() {
        return this.escChar;
    }

    /**
     * Get the sub-component separator character.
     *
     * @return sub-component separator character, or <code>(char)0</code> for none
     * @see #hasSubcomponentSeparator
     */
    public char getSubSep() {
        return this.subSep;
    }

    /**
     * Escape instances of any separator or escape character within the given string.
     *
     * <p>
     * If some character needs to be escaped but there is no escape character defined, the character is silently elided.
     *
     * @param value the String to escape
     * @return the escaped string
     */
    public String escape(String value) {
        StringBuilder buf = new StringBuilder(value.length());
        this.escape(value, buf);
        return buf.toString();
    }

    /**
     * Escape instances of any separator or escape character within the given string, and add the result to the given buffer.
     *
     * <p>
     * If some character needs to be escaped but there is no escape character defined, the character is silently elided.
     *
     * @param value the String to escape
     * @param buf buffer to append to
     */
    public void escape(String value, StringBuilder buf) {
        final int length = value.length();
        final StringBuilder escapeCode = new StringBuilder();
        for (int i = 0; i < length; i++) {
            final char ch = value.charAt(i);
            escapeCode.setLength(0);
            if (ch == this.fieldSep)
                escapeCode.append(FIELD_SEPARATOR_ESCAPE);
            else if (ch == this.repSep)
                escapeCode.append(REPEAT_SEPARATOR_ESCAPE);
            else if (ch == this.compSep)
                escapeCode.append(COMPONENT_SEPARATOR_ESCAPE);
            else if (ch == this.subSep && this.subSep != '\u0000')
                escapeCode.append(SUBCOMPONENT_SEPARATOR_ESCAPE);
            else if (ch == this.escChar && this.escChar != '\u0000')
                escapeCode.append(ESCAPE_CHARACTER_ESCAPE);
            else if (ch < 0x0020)
                escapeCode.append(String.format("%c%02x", HEX_DATA_ESCAPE, (int)ch));
            else {
                buf.append(ch);
                continue;
            }
            if (this.escChar != '\u0000' && escapeCode.length() > 0) {
                buf.append(this.escChar);
                buf.append(escapeCode);
                buf.append(this.escChar);
            }
        }
    }

    /**
     * Determine if there is a sub-component character defined.
     *
     * @return true if a sub-component character is defined
     */
    public boolean hasSubcomponentSeparator() {
        return this.subSep != '\u0000';
    }

    /**
     * Determine if there is an escape character defined.
     *
     * @return true if an escape character is defined
     */
    public boolean hasEscapeCharacter() {
        return this.escChar != '\u0000';
    }

    /**
     * Parse the escaped string back into its unescaped form.
     *
     * <p>
     * The string must have been previously escaped using the same escape character as is defined for this instance.
     *
     * <p>
     * If no escape character is defined for this instance, the unaltered value is returned.
     *
     * <p>
     * Any "custom" or invalid escapes are silently removed.
     *
     * <p>
     * An unclosed escape is returned unaltered.
     *
     * @param value escaped string value
     * @return original unescaped value
     */
    public String unescape(String value) {

        // Optimize for the common case
        if (this.escChar == '\u0000' || value.indexOf(this.escChar) == -1)
            return value;

        // Parse out escapes
        StringBuilder buf = new StringBuilder(value.length());
        int[] escapes = HL7Util.find(value, this.escChar);
        int posn = 0;
        for (int i = 0; i < escapes.length; i++) {

            // Append substring prior to escape
            buf.append(value.substring(posn, escapes[i]));

            // No more escapes? We're done
            if (++i == escapes.length)
                break;

            // Check for unclosed escape (ignore it)
            if (i == escapes.length - 1) {
                buf.append(value.substring(escapes[i - 1]));
                break;
            }

            // Decode escape
            final int escStart = escapes[i - 1] + 1;
            final int escEnd = escapes[i];
            switch (escEnd - escStart) {
            case 0:                                                                 // empty escape - ignore
                break;
            case 1:                                                                 // single character escapes
                switch (value.charAt(escStart)) {
                case FIELD_SEPARATOR_ESCAPE:
                    buf.append(this.fieldSep);
                    break;
                case COMPONENT_SEPARATOR_ESCAPE:
                    buf.append(this.compSep);
                    break;
                case REPEAT_SEPARATOR_ESCAPE:
                    buf.append(this.repSep);
                    break;
                case ESCAPE_CHARACTER_ESCAPE:
                    buf.append(this.escChar);   // we know escChar != '\u0000'
                    break;
                case SUBCOMPONENT_SEPARATOR_ESCAPE:
                    if (this.subSep != '\u0000') {
                        buf.append(this.subSep);
                        break;
                    }
                    // elide unknown escape
                    break;
                default:
                    // elide unknown escape
                    break;
                }
                break;
            default:                                                                // multi-character escapes
                final String remainder = value.substring(escStart + 1, escEnd);
            multiswitch:
                switch (value.charAt(escStart)) {
                case HEX_DATA_ESCAPE:
                    final char[] chars = new char[remainder.length() / 2];
                    for (int j = 0; j < chars.length; j++) {
                        final String digits = remainder.substring(j * 2, j * 2 + 2);
                        try {
                            chars[j] = (char)Integer.parseInt(digits, 16);
                        } catch (NumberFormatException e) {
                            break multiswitch;
                        }
                    }
                    buf.append(chars);
                    break;
                default:
                    // elide unknown escape
                    break;
                }
                break;
            }

            // Update next starting position
            posn = escEnd + 1;
        }

        // Done
        return buf.toString();
    }

    /**
     * Returns string representation as well-formed MSH.1 and MSH.2 fields.
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(5);
        buf.append(this.fieldSep);
        buf.append(this.compSep);
        buf.append(this.repSep);
        if (this.escChar != '\u0000') {
            buf.append(this.escChar);
            if (this.subSep != '\u0000')
                buf.append(this.subSep);
        }
        return buf.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || obj.getClass() != getClass())
            return false;
        final HL7Seps that = (HL7Seps)obj;
        return this.fieldSep == that.fieldSep
          && this.compSep == that.compSep
          && this.repSep == that.repSep
          && this.escChar == that.escChar
          && this.subSep == that.subSep;
    }

    @Override
    public int hashCode() {
        return this.fieldSep << 6
          ^ this.compSep << 6
          ^ this.repSep << 6
          ^ this.escChar << 6
          ^ this.subSep << 6;
    }

    /**
     * Verify that the given combination of separator and escape characters is valid.
     *
     * <p>
     * This method performs the following checks:
     * <ul>
     *  <li>All defined characters are plain ASCII.</li>
     *  <li>All defined characters are unique.</li>
     *  <li>If a sub-component separator is defined, then so is an escape character.</li>
     * </ul>
     *
     * @param fieldSep field separator
     * @param compSep component separator
     * @param repSep repetition separator
     * @param escChar escape character, or <code>(char)0</code> for none
     * @param subSep subcomponent separator, or <code>(char)0</code> for none
     * @throws HL7ContentException if the characters are invalid
     */
    public static void validate(char fieldSep, char compSep, char repSep, char escChar, char subSep) throws HL7ContentException {

        // Line them up
        char[] chars = new char[] { fieldSep, compSep, repSep, escChar, subSep };
        String[] names = new String[] {
          "field separator",
          "component separator",
          "repeat separator",
          "escape character",
          "sub-component separator"
        };

        // Check for character validity
        for (int i = 0; i < chars.length; i++) {
            if (i >= 3 && chars[i] == '\u0000')
                continue;
            if (chars[i] <= ' ' || chars[i] > '~')
                throw new HL7ContentException("illegal " + names[i] + " `" + chars[i] + "'");
        }

        // Sub-component defined implies escape defined
        if (escChar == '\u0000' && subSep != '\u0000')
            throw new HL7ContentException("escape character must be defined when sub-component separator is defined");

        // Check for duplicates
        for (int i = 0; i < chars.length - 1; i++) {
            if (i >= 3 && chars[i] == '\u0000')
                continue;
            for (int j = i + 1; j < chars.length; j++) {
                if (chars[i] == chars[j])
                    throw new HL7ContentException("duplicate " + names[i] + " and " + names[j] + " `" + chars[i] + "'");
            }
        }
    }
}

