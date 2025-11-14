
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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
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
public class XMLConverter {

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

    protected final DocumentBuilderFactory documentBuilderFactory;

    /**
     * Default constructor.
     */
    public XMLConverter() {
        this(DocumentBuilderFactory.newInstance());
        this.documentBuilderFactory.setNamespaceAware(true);
    }

    /**
     * Constructor.
     *
     * @param documentBuilderFactory factory for creating new XML documents
     * @throws IllegalArgumentException if {@code documentBuilderFactory} is null
     */
    public XMLConverter(DocumentBuilderFactory documentBuilderFactory) {
        if (documentBuilderFactory == null)
            throw new IllegalArgumentException("null documentBuilderFactory");
        this.documentBuilderFactory = documentBuilderFactory;
    }

    /**
     * Create an empty XML document for HL7 messages.
     *
     * @return XML document consisting of one empty {@code &lt;HL7/&gt;} tag
     */
    public Document createDocument() {
        DocumentBuilder documentBuilder;
        try {
            documentBuilder = this.documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        Document document = documentBuilder.getDOMImplementation().createDocument(HL7_NAMESPACE_URI, HL7_TAG, null);
        document.setXmlStandalone(true);
        return document;
    }

// OUTPUT

    /**
     * Convert an HL7 message message to an XML document. The document
     * element will be &lt;HL7&gt; and contain one child which is the
     * XML encoding of the message.
     *
     * @param message HL7 message
     * @param omitEmpty Omit empty tags (other than the last one)
     * @return HL7 message encoded as an XML document
     */
    public Document toXML(HL7Message message, boolean omitEmpty) {
        final Document doc = this.createDocument();
        this.appendMessage(doc.getDocumentElement(), message, omitEmpty);
        return doc;
    }

    /**
     * Convert a message to XML and append it to the given element.
     *
     * @param parent an XML {@link Element} or {@link Document} node
     * @param message HL7 message to append
     * @param omitEmpty Omit empty tags (other than the last one)
     */
    public void appendMessage(Node parent, HL7Message message, boolean omitEmpty) {
        final Element messageTag = parent.getOwnerDocument().createElementNS(HL7_NAMESPACE_URI, MESSAGE_TAG);
        for (HL7Segment segment : message.getSegments())
            this.appendSegment(messageTag, segment, omitEmpty);
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
    public void appendMessage(XMLStreamWriter writer, HL7Message message, boolean omitEmpty) throws XMLStreamException {
        writer.writeStartElement(MESSAGE_TAG);
        writer.setDefaultNamespace(HL7_NAMESPACE_URI);
        for (HL7Segment segment : message.getSegments())
            this.appendSegment(writer, segment, omitEmpty);
        writer.writeEndElement();
    }

    /**
     * Convert the given messages to XML and append them to given XML output, enclosed in an {@code <HL7>} element.
     *
     * @param writer XML output
     * @param list list of messages
     * @param omitEmpty Omit empty tags (other than the last one)
     * @throws XMLStreamException if an XML error occurs
     */
    public void appendMessages(XMLStreamWriter writer, List<HL7Message> list, boolean omitEmpty) throws XMLStreamException {
        writer.writeStartElement(HL7_TAG);
        for (HL7Message message : list)
            this.appendMessage(writer, message, omitEmpty);
        writer.writeEndElement();
    }

    /**
     * Convert a segment to XML and append it to the given element.
     *
     * @param parent an XML {@link Element} or {@link Document} node
     * @param segment HL7 segment to append
     * @param omitEmpty Omit empty tags (other than the last one)
     */
    public void appendSegment(Node parent, HL7Segment segment, boolean omitEmpty) {
        final String segName = segment.getName();
        final Document doc = parent.getOwnerDocument();
        final Element segXML = doc.createElementNS(HL7_NAMESPACE_URI, segName);
        final HL7Field[] fields = segment.getFields();
        for (int i = 1; i < fields.length; i++) {
            final HL7Field field = fields[i];
            if (omitEmpty && i < fields.length - 1 && field.isEmpty())
                continue;
            final String fieldTag = segName + "." + i;
            for (String[][] repeat : field.getValue()) {
                final Element repeatXML = doc.createElementNS(HL7_NAMESPACE_URI, fieldTag);
                segXML.appendChild(repeatXML);
                if (repeat.length == 1 && repeat[0].length == 1) {
                    if (repeat[0][0].length() > 0)
                        repeatXML.appendChild(doc.createTextNode(repeat[0][0]));
                    continue;
                }
                for (int j = 0; j < repeat.length; j++) {
                    final String[] comp = repeat[j];
                    if (omitEmpty
                      && j < repeat.length - 1
                      && comp.length == 1 && comp[0].length() == 0)
                        continue;
                    final String compTag = fieldTag + "." + (j + 1);
                    final Element compXML = doc.createElementNS(HL7_NAMESPACE_URI, compTag);
                    repeatXML.appendChild(compXML);
                    if (comp.length == 1) {
                        if (comp[0].length() > 0)
                            compXML.appendChild(doc.createTextNode(comp[0]));
                        continue;
                    }
                    for (int k = 0; k < comp.length; k++) {
                        final String subcomp = comp[k];
                        if (omitEmpty
                          && k < comp.length - 1 && subcomp.length() == 0)
                            continue;
                        final String subcompTag = compTag + "." + (k + 1);
                        final Element subcompXML = doc.createElementNS(HL7_NAMESPACE_URI, subcompTag);
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
    public void appendSegment(XMLStreamWriter writer, HL7Segment segment, boolean omitEmpty) throws XMLStreamException {
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
    public void stream(Document doc, OutputStream out) throws IOException {
        TransformerFactory transformFactory = TransformerFactory.newInstance();
        try {
            transformFactory.setAttribute("indent-number", 2);
        } catch (IllegalArgumentException e) {
            // ignore
        }
        this.stream(transformFactory, doc, out);
    }

    /**
     * Serialize the XML document and write it to the given output.
     *
     * @param transformerFactory factory for XSL transformers
     * @param doc XML document to output
     * @param out output desination
     * @throws IOException if an I/O error occurs
     */
    public void stream(TransformerFactory transformerFactory, Document doc, OutputStream out) throws IOException {

        // Create and configure Transformer
        final Transformer transformer;
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
        final OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
        try {
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
        } catch (TransformerException e) {
            if (e.getCause() instanceof IOException)
                throw (IOException)e.getCause();
            throw new RuntimeException(e);
        }
        writer.flush();
    }

// INPUT

    /**
     * Parse HL7 messages as XML from the given input stream.
     *
     * This method expects to see a {@code <HL7>} tag containing zero or more {@code <MESSAGE>} tags.
     * Parsing stops after encountering a closing {@code <HL7>}.
     *
     * @param reader XML input stream
     * @return list of zero or more HL7 messages parsed from stream
     * @throws XMLStreamException if an XML error occurs
     */
    public List<HL7Message> readMessages(XMLStreamReader reader) throws XMLStreamException {

        // Read opening <HL7> element
        this.expectTag(reader, HL7_TAG);

        // Read zero or more <MESSAGE> tags
        final ArrayList<HL7Message> list = new ArrayList<>();
        HL7Message message;
        while ((message = this.readMessage(reader)) != null)
            list.add(message);

        // Done
        return list;
    }

    /**
     * Parse an HL7 message as XML from the given input stream.
     *
     * This method expects to see an opening {@code <MESSAGE>} tag as the next event (not counting whitespace, comments, etc.).
     * It will be consumed up through the closing {@code </MESSAGE>} event. Therefore it could be part of a larger XML document.
     * If, instead, any closing XML tag is seen, null is returned.
     *
     * @param reader XML input
     * @return next HL7 message from stream, or null if a closing tag is encountered
     * @throws XMLStreamException if an XML error occurs
     */
    public HL7Message readMessage(XMLStreamReader reader) throws XMLStreamException {

        // Some tag names
        String name;
        String value;

        // Read opening <MESSAGE> element
        if ((name = this.nextTag(reader, MESSAGE_TAG, true)) == null)
            return null;

        // Read MSH.1 and MSH.2
        this.expectTag(reader, "MSH");
        this.expectTag(reader, "MSH.1");
        if ((value = this.readText(reader)).length() != 1)
            throw this.newInvalidInputException(reader, "<MSH.1> must contain a single character (field separator)");
        final char fieldSep = value.charAt(0);
        this.expectTag(reader, "MSH.2");
        if ((value = this.readText(reader)).length() < 2 || value.length() > 4)
            throw this.newInvalidInputException(reader, "<MSH.2> must contain a 2-4 characters");

        // Create MSH segment
        final char compSep = value.charAt(0);
        final char repSep = value.charAt(1);
        final char escChar = value.length() > 2 ? value.charAt(2) : (char)0;
        final char subSep = value.length() > 3 ? value.charAt(3) : (char)0;
        final HL7Seps seps;
        try {
            seps = new HL7Seps(fieldSep, compSep, repSep, escChar, subSep);
        } catch (HL7ContentException e) {
            throw this.newInvalidInputException(reader, e, "Invalid characters in MSH.2");
        }
        final MSHSegment msh = new MSHSegment(seps);

        // Read the rest of the MSH segment
        this.readFields(reader, msh);

        // Initialize message
        final HL7Message message = new HL7Message(msh);

        // Read additional segments
        HL7Segment segment;
        while ((segment = this.readSegment(reader)) != null)
            message.getSegments().add(segment);

        // Done
        return message;
    }

    protected HL7Segment readSegment(XMLStreamReader reader) throws XMLStreamException {

        // Read segment opening tag, if any
        final String name = this.nextTag(reader, this.segmentNamePattern(), true);
        if (name == null)
            return null;

        // Create segment
        HL7Segment segment;
        try {
            segment = new HL7Segment(name);
        } catch (HL7ContentException e) {
            throw this.newInvalidInputException(reader, e, "invalid segment name \"%s\"", name);
        }

        // Read fields
        this.readFields(reader, segment);

        // Done
        return segment;
    }

    protected void readFields(XMLStreamReader reader, HL7Segment segment) throws XMLStreamException {

        // Read field content
        Content content;
        while ((content = this.readFieldTag(reader, segment.getName(), false)) != null) {
            assert !content.isText();
            final String[][] repeat = this.readFieldRepeat(reader, reader.getName().getLocalPart());
            final int index = content.index;
            HL7Field field = segment.getField(index);
            final String[][][] repeats = Optional.ofNullable(segment.getField(index))
              .map(HL7Field::getValue)
              .map(value -> this.append(value, repeat))
              .orElseGet(() -> new String[][][] { repeat });
            segment.setField(index, new HL7Field(repeats));
        }
    }

    protected String[][] readFieldRepeat(XMLStreamReader reader, String parentName) throws XMLStreamException {
        Content content;
        String[][] array = new String[0][];
        while ((content = this.readFieldTag(reader, parentName, true)) != null) {
            if (content.isText())
                return new String[][] { { content.text } };
            final String[] component = this.readComponent(reader, reader.getName().getLocalPart());
            array = this.extend(array, content.index, () -> new String[] { "" });
            array[content.index - 1] = component;
        }
        return array;
    }

    protected String[] readComponent(XMLStreamReader reader, String parentName) throws XMLStreamException {
        Content content;
        String[] array = new String[0];
        while ((content = this.readFieldTag(reader, parentName, true)) != null) {
            if (content.isText())
                return new String[] { content.text };
            final String subComponent = this.readSubComponent(reader, reader.getName().getLocalPart());
            array = this.extend(array, content.index, () -> "");
            array[content.index - 1] = subComponent;
        }
        return array;
    }

    protected String readSubComponent(XMLStreamReader reader, String parentName) throws XMLStreamException {
        final Content content = this.readFieldTag(reader, parentName, true);
        if (!content.isText())
            throw this.newInvalidInputException(reader, "encountered <%s> but expected text content instead", reader.getName());
        return content.text;
    }

    private static class Content {

        final String text;
        final int index;

        Content(String text, int index) {
            this.text = text;
            this.index = index;
        }
        Content(String text) {
            this(text, -1);
        }
        Content(int index) {
            this(null, index);
        }
        boolean isText() {
            return this.text != null;
        }
    }

    /**
     * Scan the content of the current field tag, which is either text or a nested field tag.
     * In the latter case, the tag name must be be the current tag's name plus {@code ".N"} for some {@code N},
     * and on return the current event will be that nested opening tag. In the former case, the
     * text content is returned and the current event will be the closing curent field tag.
     *
     * @param reader input
     * @param parentName parent tag name
     * @param allowText whether to allow text as an option
     * @return scanned content, or null if text not allowed and closing tag encountered
     */
    protected Content readFieldTag(XMLStreamReader reader, String parentName, boolean allowText) throws XMLStreamException {

        // Sanity check
        final int previousType = reader.getEventType();
        switch (previousType) {
        case XMLStreamConstants.START_ELEMENT:
        case XMLStreamConstants.END_ELEMENT:
            break;
        default:
            throw this.newInvalidInputException(reader, "internal error: wrong current event type: " + reader.getEventType());
        }
        final Pattern subTagPattern = this.subTagPattern(parentName);

        // Scan
        final StringBuilder cdata = new StringBuilder();
        for (int eventType = reader.next(); true; eventType = reader.next()) {
            switch (eventType) {
            case XMLStreamConstants.CHARACTERS:
            case XMLStreamConstants.CDATA:
            case XMLStreamConstants.SPACE:
            case XMLStreamConstants.ENTITY_REFERENCE:
                cdata.append(reader.getText());
                break;
            case XMLStreamConstants.PROCESSING_INSTRUCTION:
            case XMLStreamConstants.COMMENT:
                break;
            case XMLStreamConstants.START_ELEMENT:
                final QName qname = reader.getName();
                final String name = qname.getLocalPart();
                if ((qname.getNamespaceURI() != null && !qname.getNamespaceURI().isEmpty())
                  || !subTagPattern.matcher(name).matches())
                    throw this.newInvalidInputException(reader, "unexpected tag <%s> within <%s>", qname, parentName);
                final int index = Integer.parseInt(name.substring(parentName.length() + 1), 10);
                return new Content(index);
            case XMLStreamConstants.END_ELEMENT:
                return allowText && previousType == XMLStreamConstants.START_ELEMENT ? new Content(cdata.toString()) : null;
            default:
                throw this.newInvalidInputException(reader, "unexpected XML content (type " + eventType + ")");
            }
        }
    }

    protected void expectTag(XMLStreamReader reader, String expect) throws XMLStreamException {
        this.nextTag(reader, expect, false);
    }

    protected String nextTag(XMLStreamReader reader, String expect, boolean closingOK) throws XMLStreamException {
        return this.nextTag(reader, Pattern.compile(Pattern.quote(expect)), closingOK);
    }

    /**
     * Scan forward until we see an opening or closing tag.
     * If opening tag is seen, its name is returned. If a closing tag is seen, null is returned
     * if {@code closingOK}, else an exception thrown.
     *
     * @param reader XML input
     * @param expect expected name pattern
     * @param closingOK true if a closing tag is OK, otherwise false
     * @return opening tag name, or null if closing tag found
     * @throws XMLStreamException if something unexpected is encountered
     */
    protected String nextTag(XMLStreamReader reader, Pattern expect, boolean closingOK) throws XMLStreamException {
        while (true) {
            if (!reader.hasNext())
                throw this.newInvalidInputException(reader, "unexpected end of input");
            final int eventType = reader.next();
            switch (eventType) {
            case XMLStreamConstants.START_ELEMENT:
                final QName qname = reader.getName();
                if (qname.getNamespaceURI() != null && !qname.getNamespaceURI().isEmpty())
                    throw this.newInvalidInputException(reader, "encountered <%s> with non-null namespace", qname);
                final String name = qname.getLocalPart();
                if (!expect.matcher(name).matches()) {
                    throw this.newInvalidInputException(reader,
                      "encountered unexpected element <%s> (expecting \"%s\")", name, expect);
                }
                return name;
            case XMLStreamConstants.END_ELEMENT:
                if (!closingOK)
                    throw this.newInvalidInputException(reader, "found unexpected closing <%s> tag", reader.getName());
                return null;
            default:
                break;
            }
        }
    }

    protected String readText(XMLStreamReader reader) throws XMLStreamException {
        try {
            return reader.getElementText();
        } catch (Exception e) {
            throw this.newInvalidInputException(reader, e, "invalid content in <%s> element: %s", reader.getName(), e.getMessage());
        }
    }

    private Pattern subTagPattern(String name) {
        return Pattern.compile(Pattern.quote(name) + "\\.[1-9][0-9]{0,8}");
    }

    private Pattern segmentNamePattern() {
        return Pattern.compile("[A-Z][A-Z0-9]{2}");
    }

    protected XMLStreamException newInvalidInputException(XMLStreamReader reader, String format, Object... args) {
        return this.newInvalidInputException(reader, null, format, args);
    }

    protected XMLStreamException newInvalidInputException(XMLStreamReader reader, Throwable cause, String format, Object... args) {
        final XMLStreamException e = new XMLStreamException(String.format(format, args), reader.getLocation());
        if (cause != null)
            e.initCause(cause);
        return e;
    }

    @SuppressWarnings("unchecked")
    private <T> T[] append(T[] array, T element) {
        final Class<?> elementType = array.getClass().getComponentType();
        final int arrayLength = Array.getLength(array);
        Object array2 = Array.newInstance(elementType, arrayLength + 1);
        System.arraycopy(array, 0, array2, 0, arrayLength);
        Array.set(array2, arrayLength, element);
        return (T[])array2;
    }

    @SuppressWarnings("unchecked")
    private <T> T[] extend(T[] array, int minLength, Supplier<T> filler) {
        final int arrayLength = Array.getLength(array);
        if (arrayLength >= minLength)
            return array;
        final Class<?> elementType = array.getClass().getComponentType();
        Object array2 = Array.newInstance(elementType, minLength);
        System.arraycopy(array, 0, array2, 0, arrayLength);
        for (int i = arrayLength; i < minLength; i++)
            Array.set(array2, i, filler.get());
        return (T[])array2;
    }

    /**
     * Test routine. Reads HL7 messages in "file format" and outputs them in an XML document.
     *
     * @param args command line arguments
     * @throws Exception if an error occurs
     */
    @SuppressWarnings("fallthrough")
    public static void main(String[] args) throws Exception {
        boolean verbose = false;
        if (args.length > 0 && args[0].equals("-v")) {
            verbose = true;
            final String[] args2 = new String[args.length - 1];
            System.arraycopy(args, 1, args2, 0, args2.length);
            args = args2;
        }
        final InputStream in;
        switch (args.length) {
        case 1:
            in = new FileInputStream(args[0]);
            break;
        case 0:
            in = System.in;
            break;
        default:
            System.err.println("Usage: XMLConverter [-v] [filename]");
            System.exit(1);
            throw new RuntimeException();
        }
        try (HL7FileReader reader = new HL7FileReader(in)) {
            final XMLConverter xmlConverter = new XMLConverter();
            final Document doc = xmlConverter.createDocument();
            while (true) {
                try {
                    xmlConverter.appendMessage(doc.getDocumentElement(), reader.readMessage(), !verbose);
                } catch (EOFException e) {
                    break;
                }
            }
            xmlConverter.stream(doc, System.out);
        }
    }
}
