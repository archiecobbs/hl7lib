
/*
 * Copyright (C) 2008 Archie L. Cobbs. All rights reserved.
 *
 * $Id$
 */

package org.dellroad.hl7;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

public class XMLConverterTest extends InputTest {

    private HL7Message msg;

    @BeforeClass
    public void loadMessage() throws HL7ContentException, IOException {
        this.msg = readMessages(
          getClass().getResourceAsStream("input3.txt")).iterator().next();
    }

    @Test(dataProvider = "xmlTests")
    public void testXML(boolean omit, String resource) throws IOException {
        Document doc = XMLConverter.createDocument();
        XMLConverter.appendMessage(doc.getDocumentElement(), this.msg, omit);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XMLConverter.stream(doc, out);
        InputStream in = new ByteArrayInputStream(out.toByteArray());
        compare(in, getClass().getResourceAsStream(resource));
    }

    @DataProvider(name = "xmlTests")
    public Iterator<Object[]> genXMLTests() {
        ArrayList<Object[]> list = new ArrayList<Object[]>();
        list.add(new Object[] { Boolean.TRUE, "output3.xml" });
        list.add(new Object[] { Boolean.FALSE, "output3-verbose.xml" });
        return list.iterator();
    }

    public void compare(InputStream actual, InputStream expected)
      throws IOException {
        int line = 1;
        int pos = 0;
        while (true) {
            int byte1 = actual.read();
            int byte2 = expected.read();
            if (byte1 != byte2) {
                throw new RuntimeException("difference at " + line + ":"
                  + pos + ": expected " + format(byte1) + " but read "
                  + format(byte2));
            }
            switch (byte1) {
            case -1:
                return;
            case '\n':
                line++;
                pos = 0;
                break;
            default:
                pos++;
                break;
            }
        }
    }

    public String format(int val) {
        if (val == -1)
            return "EOF";
        return String.format("0x%02x", val & 0xff);
    }
}

