
/*
 * Copyright (C) 2008 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.hl7;

import java.io.Closeable;
import java.io.IOException;

/**
 * Input source for HL7 messages.
 */
public interface HL7Reader extends Closeable {

    /**
     * Read the next HL7 message.
     *
     * @return next message read
     * @throws HL7ContentException if an invalid HL7 message is received, decoded, etc. by this instance
     * @throws java.io.EOFException if there is no more input
     * @throws IOException if some other I/O error occurs
     * @throws IOException if {@link #close} has already been invoked
     */
    HL7Message readMessage() throws IOException, HL7ContentException;
}

