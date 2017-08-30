
/*
 * Copyright (C) 2008 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.hl7;

/**
 * Exception thrown to indicate invalid HL7 content was encountered.
 */
@SuppressWarnings("serial")
public class HL7ContentException extends Exception {

    private String content;

    public HL7ContentException() {
    }

    public HL7ContentException(String message) {
        super(message);
    }

    public HL7ContentException(String message, Throwable cause) {
        super(message, cause);
    }

    public HL7ContentException(Throwable cause) {
        super(cause);
    }

    /**
     * Get the offending content.
     *
     * @return offending text, or {@code null} if none was associated with this instance
     */
    public String getContent() {
        return this.content;
    }

    /**
     * Record the offending text.
     *
     * @param content offending text
     * @return this instance
     */
    public HL7ContentException setContent(String content) {
        this.content = content;
        return this;
    }
}

