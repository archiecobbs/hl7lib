
/*
 * Copyright (C) 2008 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.hl7.llp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.dellroad.hl7.HL7ContentException;
import org.dellroad.hl7.HL7Message;
import org.dellroad.hl7.Input1Test;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

public class LLPStreamTest extends Input1Test {

    private File tempFile;

    @BeforeClass
    public void createTempFile() throws IOException {
        this.tempFile = File.createTempFile(getClass().getSimpleName(), null);
    }

    @AfterClass
    public void deleteTempFile() {
        this.tempFile.delete();
    }

    @Test
    public void testOutput() throws IOException, HL7ContentException {
        LLPOutputStream writer = new LLPOutputStream(
          new FileOutputStream(this.tempFile));
        writer.writeMessage(this.msg1);
        writer.writeMessage(this.msg2);
        writer.close();
    }

    @Test(dependsOnMethods = { "testOutput" })
    public void testInput() throws IOException, HL7ContentException {
        List<HL7Message> list = readMessages(
          new LLPInputStream(new FileInputStream(this.tempFile), 65536));
        assert list.size() == 2;
        assertEquals(list.get(0), this.msg1);
        assertEquals(list.get(1), this.msg2);
    }

    @Test(dependsOnMethods = { "testOutput" })
    public void testMaxLength() throws IOException, HL7ContentException {
        LLPInputStream in = new LLPInputStream(
          new FileInputStream(this.tempFile), 37);
        try {
            readMessages(in);
            assert false;
        } catch (LLPException e) {
            // success
        }
    }
}

