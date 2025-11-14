
/*
 * Copyright (C) 2008 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.hl7.llp;

import java.io.IOException;

/**
 * Exception generated due to improper LLC framing.
 */
@SuppressWarnings("serial")
public class LLPException extends IOException {

    /**
     * Default constructor.
     */
    public LLPException() {
    }

    /**
     * Constructor.
     *
     * @param message detail message
     */
    public LLPException(String message) {
        super(message);
    }
}

