
/*
 * Copyright (C) 2008 Archie L. Cobbs. All rights reserved.
 *
 * $Id$
 */

package org.dellroad.hl7;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.dellroad.hl7.io.HL7FileReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Converts {@link HL7Message}s to and from XML.
 */
public final class XMLConverter {

    /**
     * HL7 message XML namespace.
     */
    public static final String HL7_NAMESPACE_URI = null;

    /**
     * XML tag name used by {@link #toXML}.
     */
    public static final String HL7_TAG = "HL7";

    /**
     * XML tag name used for one XML-encoded message.
     */
    public static final String MESSAGE_TAG = "MESSAGE";

    private XMLConverter() {
    }

    /**
     * Create an empty XML document for HL7 messages.
     */
    public static Document createDocument() {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        Document document = documentBuilder.getDOMImplementation().createDocument(HL7_NAMESPACE_URI, HL7_TAG, null);
        document.setXmlStandalone(true);
        return document;
    }

    /**
     * Convert an HL7 message message to an XML document. The document
     * element will be &lt;HL7&gt; and contain one child which is the
     * XML encoding of the message.
     *
     * @param message HL7 message
     * @param omitEmpty Omit empty tags (other than the last one)
     * @return HL7 message encoded as an XML document
     */
    public static Document toXML(HL7Message message, boolean omitEmpty) {
        Document doc = createDocument();
        XMLConverter.appendMessage(doc.getDocumentElement(), message, omitEmpty);
        return doc;
    }

    /**
     * Convert a message to XML and append it to the given element.
     *
     * @param parent an XML {@link Element} or {@link Document} node
     * @param message HL7 message to append
     * @param omitEmpty Omit empty tags (other than the last one)
     */
    public static void appendMessage(Node parent, HL7Message message, boolean omitEmpty) {
        Element messageTag = parent.getOwnerDocument().createElementNS(HL7_NAMESPACE_URI, MESSAGE_TAG);
        for (HL7Segment segment : message.getSegments())
            XMLConverter.appendSegment(messageTag, segment, omitEmpty);
        parent.appendChild(messageTag);
    }

    /**
     * Convert a segment to XML and append it to the given element.
     *
     * @param parent an XML {@link Element} or {@link Document} node
     * @param segment HL7 segment to append
     * @param omitEmpty Omit empty tags (other than the last one)
     */
    public static void appendSegment(Node parent, HL7Segment segment, boolean omitEmpty) {
        String segName = segment.getName();
        Document doc = parent.getOwnerDocument();
        Element segXML = doc.createElementNS(HL7_NAMESPACE_URI, segName);
        HL7Field[] fields = segment.getFields();
        for (int i = 1; i < fields.length; i++) {
            HL7Field field = fields[i];
            if (omitEmpty && i < fields.length - 1 && field.isEmpty())
                continue;
            String fieldTag = segName + "." + i;
            for (String[][] repeat : field.getValue()) {
                Element repeatXML = doc.createElementNS(HL7_NAMESPACE_URI, fieldTag);
                segXML.appendChild(repeatXML);
                if (repeat.length == 1 && repeat[0].length == 1) {
                    if (repeat[0][0].length() > 0)
                        repeatXML.appendChild(doc.createTextNode(repeat[0][0]));
                    continue;
                }
                for (int j = 0; j < repeat.length; j++) {
                    String[] comp = repeat[j];
                    if (omitEmpty
                      && j < repeat.length - 1
                      && comp.length == 1 && comp[0].length() == 0)
                        continue;
                    String compTag = fieldTag + "." + (j + 1);
                    Element compXML = doc.createElementNS(HL7_NAMESPACE_URI, compTag);
                    repeatXML.appendChild(compXML);
                    if (comp.length == 1) {
                        if (comp[0].length() > 0)
                            compXML.appendChild(doc.createTextNode(comp[0]));
                        continue;
                    }
                    for (int k = 0; k < comp.length; k++) {
                        String subcomp = comp[k];
                        if (omitEmpty
                          && k < comp.length - 1 && subcomp.length() == 0)
                            continue;
                        String subcompTag = compTag + "." + (k + 1);
                        Element subcompXML = doc.createElementNS(HL7_NAMESPACE_URI, subcompTag);
                        if (subcomp.length() > 0)
                            subcompXML.appendChild(doc.createTextNode(subcomp));
                        compXML.appendChild(subcompXML);
                    }
                }
            }
        }
        parent.appendChild(segXML);
    }

    /**
     * Serialize the XML document and write it to the given output.
     */
    public static void stream(Document doc, OutputStream out) throws IOException {

        // Create and configure Transformer
        TransformerFactory transformFactory = TransformerFactory.newInstance();
        transformFactory.setAttribute("indent-number", 2);
        Transformer transformer;
        try {
            transformer = transformFactory.newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        // Transform DOM into serialized output stream.
        // Wrap output stream in a writer to work around this bug:
        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6337981
        OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
        try {
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
        } catch (TransformerException e) {
            if (e.getCause() instanceof IOException)
                throw (IOException)e.getCause();
            throw new RuntimeException(e);
        }
        writer.flush();
    }

    /**
     * Test routine. Reads HL7 messages in "file format" and outputs them in an XML document.
     */
    @SuppressWarnings("fallthrough")
    public static void main(String[] args) throws Exception {
        InputStream in = System.in;
        boolean verbose = false;
        if (args.length > 0 && args[0].equals("-v")) {
            verbose = true;
            String[] args2 = new String[args.length - 1];
            System.arraycopy(args, 1, args2, 0, args2.length);
            args = args2;
        }
        switch (args.length) {
        case 1:
            in = new FileInputStream(args[0]);
            // FALLTHROUGH
        case 0:
            break;
        default:
            System.err.println("Usage: XMLConverter [-v] [filename]");
            System.exit(1);
            break;
        }
        HL7FileReader reader = new HL7FileReader(in);
        Document doc = createDocument();
        while (true) {
            try {
                XMLConverter.appendMessage(doc.getDocumentElement(), reader.readMessage(), !verbose);
            } catch (EOFException e) {
                break;
            }
        }
        reader.close();
        XMLConverter.stream(doc, System.out);
    }
}

