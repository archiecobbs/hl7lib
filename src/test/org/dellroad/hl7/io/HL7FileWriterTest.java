
/*
 * Copyright (C) 2008 Archie L. Cobbs. All rights reserved.
 *
 * $Id$
 */

package org.dellroad.hl7.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.dellroad.hl7.HL7ContentException;
import org.dellroad.hl7.HL7Message;
import org.dellroad.hl7.Input1Test;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

public class HL7FileWriterTest extends Input1Test {

    @Test
    public void testOutput() throws IOException, HL7ContentException {
        File tempFile = File.createTempFile("HL7FileWriterTest", null);
        HL7FileWriter writer = new HL7FileWriter(
          new FileOutputStream(tempFile));
        writer.writeMessage(this.msg1);
        writer.writeMessage(this.msg2);
        writer.close();
        List<HL7Message> list = readMessages(new FileInputStream(tempFile));
        assert list.size() == 2;
        assertEquals(list.get(0), this.msg1);
        assertEquals(list.get(1), this.msg2);
        tempFile.delete();
    }
}

