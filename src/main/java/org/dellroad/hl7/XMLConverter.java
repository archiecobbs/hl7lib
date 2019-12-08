
/*
 * Copyright (C) 2008 Archie L. Cobbs. All rights reserved.
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
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
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
 * Converts {@link HL7Message}(s) to and from XML.
 *
 * <p>
 * Example of what the XML looks like:
 * <blockquote><pre>
 * &lt;HL7&gt;
 *  &lt;MESSAGE&gt;
 *    &lt;MSH&gt;
 *      &lt;MSH.1&gt;|&lt;/MSH.1&gt;
 *      &lt;MSH.2&gt;^~\&amp;amp;&lt;/MSH.2&gt;
 *      &lt;MSH.3&gt;ST01&lt;/MSH.3&gt;
 *      &lt;MSH.4&gt;B&lt;/MSH.4&gt;
 *      &lt;MSH.5&gt;IM&lt;/MSH.5&gt;
 *      &lt;MSH.6&gt;B&lt;/MSH.6&gt;
 *      &lt;MSH.7&gt;20191203131805&lt;/MSH.7&gt;
 *      &lt;MSH.9&gt;
 *        &lt;MSH.9.1&gt;ADT&lt;/MSH.9.1&gt;
 *        &lt;MSH.9.2&gt;A04&lt;/MSH.9.2&gt;
 *      &lt;/MSH.9&gt;
 *      &lt;MSH.10&gt;99479783&lt;/MSH.10&gt;
 *      &lt;MSH.11&gt;P&lt;/MSH.11&gt;
 *      &lt;MSH.12&gt;2.2&lt;/MSH.12&gt;
 *      &lt;MSH.13&gt;99479783&lt;/MSH.13&gt;
 *      &lt;MSH.15&gt;AL&lt;/MSH.15&gt;
 *      &lt;MSH.22&gt;2.2b&lt;/MSH.22&gt;
 *    &lt;/MSH&gt;
 *    &lt;EVN&gt;
 *      &lt;EVN.1&gt;A04&lt;/EVN.1&gt;
 *      &lt;EVN.2&gt;20160815131805&lt;/EVN.2&gt;
 *      &lt;EVN.5&gt;
 *        &lt;EVN.5.1&gt;DMB&lt;/EVN.5.1&gt;
 *        &lt;EVN.5.2&gt;WASHINGTON&lt;/EVN.5.2&gt;
 *        &lt;EVN.5.3&gt;GEORGE&lt;/EVN.5.3&gt;
 *        &lt;EVN.5.4&gt;M&lt;/EVN.5.4&gt;
 *      &lt;/EVN.5&gt;
 *    &lt;/EVN&gt;
 *    &lt;PID&gt;
 *      &lt;PID.1&gt;1&lt;/PID.1&gt;
 *      &lt;PID.2&gt;344356467&lt;/PID.2&gt;
 *      &lt;PID.3&gt;345345456&lt;/PID.3&gt;
 *      &lt;PID.4&gt;123132423&lt;/PID.4&gt;
 *      &lt;PID.5&gt;
 *        &lt;PID.5.1&gt;HAMILTON&lt;/PID.5.1&gt;
 *        &lt;PID.5.2&gt;ALEXANDER&lt;/PID.5.2&gt;
 *      &lt;/PID.5&gt;
 *    ...
 * </pre></blockquote>
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
     *
     * @return XML document consisting of one empty {@code &lt;HL7/&gt;} tag
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
     * Convert a message to XML and append it to the given {@link XMLStreamWriter}.
     *
     * @param writer XML output
     * @param message HL7 message to append
     * @param omitEmpty Omit empty tags (other than the last one)
     * @throws XMLStreamException if an XML error occurs
     */
    public static void appendMessage(XMLStreamWriter writer, HL7Message message, boolean omitEmpty) throws XMLStreamException {
        writer.writeStartElement(MESSAGE_TAG);
        writer.setDefaultNamespace(HL7_NAMESPACE_URI);
        for (HL7Segment segment : message.getSegments())
            XMLConverter.appendSegment(writer, segment, omitEmpty);
        writer.writeEndElement();
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
     * Convert a segment to XML and write it to the given {@link XMLStreamWriter}.
     *
     * @param writer XML output
     * @param segment HL7 segment to append
     * @param omitEmpty Omit empty tags (other than the last one)
     * @throws XMLStreamException if an XML error occurs
     */
    public static void appendSegment(XMLStreamWriter writer, HL7Segment segment, boolean omitEmpty) throws XMLStreamException {
        final String segName = segment.getName();
        final HL7Field[] fields = segment.getFields();
        writer.writeStartElement(segName);
        writer.setDefaultNamespace(HL7_NAMESPACE_URI);
        for (int i = 1; i < fields.length; i++) {
            final HL7Field field = fields[i];
            if (omitEmpty && i < fields.length - 1 && field.isEmpty())
                continue;
            final String fieldTag = segName + "." + i;
            for (String[][] repeat : field.getValue()) {
                writer.writeStartElement(fieldTag);
                if (repeat.length == 1 && repeat[0].length == 1) {
                    if (repeat[0][0].length() > 0)
                        writer.writeCharacters(repeat[0][0]);
                    writer.writeEndElement();
                    continue;
                }
                for (int j = 0; j < repeat.length; j++) {
                    final String[] comp = repeat[j];
                    if (omitEmpty
                      && j < repeat.length - 1
                      && comp.length == 1 && comp[0].length() == 0)
                        continue;
                    final String compTag = fieldTag + "." + (j + 1);
                    writer.writeStartElement(compTag);
                    if (comp.length == 1) {
                        if (comp[0].length() > 0)
                            writer.writeCharacters(comp[0]);
                        writer.writeEndElement();
                        continue;
                    }
                    for (int k = 0; k < comp.length; k++) {
                        final String subcomp = comp[k];
                        if (omitEmpty
                          && k < comp.length - 1 && subcomp.length() == 0)
                            continue;
                        final String subcompTag = compTag + "." + (k + 1);
                        writer.writeStartElement(subcompTag);
                        if (subcomp.length() > 0)
                            writer.writeCharacters(subcomp);
                        writer.writeEndElement();
                    }
                    writer.writeEndElement();
                }
                writer.writeEndElement();
            }
        }
        writer.writeEndElement();
    }

    /**
     * Serialize the XML document and write it to the given output.
     *
     * @param doc XML document to output
     * @param out output desination
     * @throws IOException if an I/O error occurs
     */
    public static void stream(Document doc, OutputStream out) throws IOException {

        // Create and configure Transformer
        TransformerFactory transformFactory = TransformerFactory.newInstance();
        try {
            transformFactory.setAttribute("indent-number", 2);
        } catch (IllegalArgumentException e) {
            // ignore
        }
        XMLConverter.stream(transformFactory, doc, out);
    }

    /**
     * Serialize the XML document and write it to the given output.
     *
     * @param transformerFactory factory for XSL transformers
     * @param doc XML document to output
     * @param out output desination
     * @throws IOException if an I/O error occurs
     */
    public static void stream(TransformerFactory transformerFactory, Document doc, OutputStream out) throws IOException {

        // Create and configure Transformer
        Transformer transformer;
        try {
            transformer = transformerFactory.newTransformer();
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
     *
     * @param args command line arguments
     * @throws Exception if an error occurs
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
            break;
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

