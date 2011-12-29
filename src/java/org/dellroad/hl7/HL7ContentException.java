
/*
 * Copyright (C) 2008 Archie L. Cobbs. All rights reserved.
 *
 * $Id$
 */

package org.dellroad.hl7;

/**
 * Exception thrown to indicate invalid HL7 content was encountered.
 */
@SuppressWarnings("serial")
public class HL7ContentException extends Exception {

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
}

