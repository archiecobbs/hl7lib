
/*
 * Copyright (C) 2008 Archie L. Cobbs. All rights reserved.
 *
 * $Id$
 */

package org.dellroad.hl7;

import java.io.IOException;

/**
 * Input source for HL7 messages.
 */
public interface HL7Reader {

    /**
     * Read the next HL7 message.
     *
     * @throws HL7ContentException if an invalid HL7 message is received, decoded, etc. by this instance
     * @throws java.io.EOFException if there is no more input
     * @throws IOException if some other error occurs
     * @throws IOException if {@link #close} has already been invoked
     */
    HL7Message readMessage() throws IOException, HL7ContentException;

    /**
     * Close this instance.
     */
    void close() throws IOException;
}

