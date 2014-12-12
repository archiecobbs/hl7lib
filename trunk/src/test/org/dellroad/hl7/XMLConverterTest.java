
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

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.dellroad.stuff.xml.EmptyTagXMLStreamWriter;
import org.dellroad.stuff.xml.IndentXMLStreamWriter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

public class XMLConverterTest extends InputTest {

    private HL7Message msg;

    @BeforeClass
    public void loadMessage() throws HL7ContentException, IOException {
        this.msg = this.readMessages(this.getClass().getResourceAsStream("input3.txt")).iterator().next();
    }

    @Test(dataProvider = "xmlTests")
    public void testXML(boolean omit, String resource) throws IOException {
        Document doc = XMLConverter.createDocument();
        XMLConverter.appendMessage(doc.getDocumentElement(), this.msg, omit);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XMLConverter.stream(doc, out);
        InputStream in = new ByteArrayInputStream(out.toByteArray());
        try {
            this.compare(in, getClass().getResourceAsStream(resource));
        } catch (RuntimeException e) {
            System.out.println("Mismatched XML: " + e);
            System.out.write(out.toByteArray());
            System.out.flush();
            throw e;
        }
    }

    @Test(dataProvider = "xmlStreamTests")
    public void testStreamXML(boolean omit, String resource) throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final XMLStreamWriter writer = new IndentXMLStreamWriter(new EmptyTagXMLStreamWriter(
          XMLOutputFactory.newInstance().createXMLStreamWriter(out, "UTF-8")), 2);
        writer.setDefaultNamespace(XMLConverter.HL7_NAMESPACE_URI);
        writer.writeStartDocument("UTF-8", "1.0");
        writer.writeStartElement(XMLConverter.HL7_TAG);
        XMLConverter.appendMessage(writer, this.msg, omit);
        writer.writeEndElement();
        writer.close();
        out.write('\n');    // add final newline
        final InputStream in = new ByteArrayInputStream(out.toByteArray());
        try {
            this.compare(in, getClass().getResourceAsStream(resource));
        } catch (RuntimeException e) {
            System.out.println("Mismatched XML: " + e);
            System.out.write(out.toByteArray());
            System.out.flush();
            throw e;
        }
    }

    @DataProvider(name = "xmlTests")
    public Iterator<Object[]> genXMLTests() {
        ArrayList<Object[]> list = new ArrayList<Object[]>();
        list.add(new Object[] { Boolean.TRUE, "output3.xml" });
        list.add(new Object[] { Boolean.FALSE, "output3-verbose.xml" });
        return list.iterator();
    }

    @DataProvider(name = "xmlStreamTests")
    public Iterator<Object[]> genXMLStreamTests() {
        ArrayList<Object[]> list = new ArrayList<Object[]>();
        list.add(new Object[] { Boolean.TRUE, "output3-stream.xml" });
        list.add(new Object[] { Boolean.FALSE, "output3-stream-verbose.xml" });
        return list.iterator();
    }

    public void compare(InputStream actual, InputStream expected) throws IOException {
        int line = 1;
        int pos = 0;
        while (true) {
            int byte1 = actual.read();
            int byte2 = expected.read();
            if (byte1 != byte2) {
                throw new RuntimeException("difference at " + line + ":" + pos
                  + ": expected " + this.format(byte1) + " but read " + this.format(byte2));
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

