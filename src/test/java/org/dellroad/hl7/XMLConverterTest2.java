
/*
 * Copyright (C) 2008 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.hl7;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.dellroad.stuff.xml.EmptyTagXMLStreamWriter;
import org.dellroad.stuff.xml.IndentXMLStreamWriter;
import org.testng.annotations.Test;

public class XMLConverterTest2 extends InputTest {

    private HL7Message msg;

    @Test
    public void testRoundTrip() throws Exception {

        // Read messages
        final List<HL7Message> list1;
        try (InputStream input = this.getClass().getResourceAsStream("input4.txt")) {
            list1 = this.readMessages(input);
        }

        // Convert to XML
        final ByteArrayOutputStream buf = new ByteArrayOutputStream();
        final XMLStreamWriter writer = new IndentXMLStreamWriter(
          new EmptyTagXMLStreamWriter(XMLOutputFactory.newInstance().createXMLStreamWriter(buf, "UTF-8")), 4);
        writer.setDefaultNamespace(XMLConverter.HL7_NAMESPACE_URI);
        writer.writeStartDocument("UTF-8", "1.0");
        new XMLConverter().appendMessages(writer, list1, true);
        writer.close();
        final byte[] xml = buf.toByteArray();

        //this.log.info("XML:\n\n{}", new String(xml, java.nio.charset.StandardCharsets.UTF_8));

        // Read back from XML
        final List<HL7Message> list2;
        try (ByteArrayInputStream input = new ByteArrayInputStream(xml)) {
            final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            final XMLStreamReader reader = inputFactory.createXMLStreamReader(input);
            list2 = new XMLConverter().readMessages(reader);
            reader.close();
        }

        // Compare
        if (list1.size() != list2.size())
            throw new Exception(String.format("wrote %d messages, read back %d messages", list1.size(), list2.size()));
        for (int i = 0; i < list1.size(); i++) {
            final HL7Message msg1 = list1.get(i);
            final HL7Message msg2 = list2.get(i);
            if (!msg1.equals(msg2) || !msg1.toString().equals(msg2.toString())) {
                throw new Exception(String.format(
                  "round-trip mismatch (message index %d):%nMESSAGE 1:%n%s%n%nMESSAGE 2:%n%s",
                  i,
                  msg1.toString().replace('\r', '\n'),
                  msg2.toString().replace('\r', '\n')));
            }
        }
    }
}
