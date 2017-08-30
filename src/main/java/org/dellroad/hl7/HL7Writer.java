
/*
 * Copyright (C) 2008 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.hl7;

import java.io.Closeable;
import java.io.IOException;

/**
 * Implemented by classes whose instances are capable of sending out HL7 messages.
 */
public interface HL7Writer extends Closeable {

    /**
     * Write a message.
     *
     * @param message message to write
     * @throws IOException if an I/O error occurs
     * @throws IOException if {@link #close} has already been invoked
     */
    void writeMessage(HL7Message message) throws IOException;
}

