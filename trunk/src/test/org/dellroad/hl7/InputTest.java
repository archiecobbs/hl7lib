
/*
 * Copyright (C) 2008 Archie L. Cobbs. All rights reserved.
 *
 * $Id$
 */

package org.dellroad.hl7;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.dellroad.hl7.io.HL7FileReader;

public abstract class InputTest {

    /**
     * Reads all messages from the reader, then closes it.
     */
    protected List<HL7Message> readMessages(HL7Reader reader)
      throws HL7ContentException, IOException {
        ArrayList<HL7Message> list = new ArrayList<HL7Message>();
        while (true) {
            try {
                list.add(reader.readMessage());
            } catch (EOFException e) {
                break;
            }
        }
        reader.close();
        return list;
    }

    /**
     * Reads all messages from the input stream in file format.
     *
     * @see HL7FileReader
     */
    protected List<HL7Message> readMessages(InputStream in)
      throws HL7ContentException, IOException {
        return readMessages(new HL7FileReader(in));
    }
}

