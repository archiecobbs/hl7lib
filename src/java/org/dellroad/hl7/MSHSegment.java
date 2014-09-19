
/*
 * Copyright (C) 2008 Archie L. Cobbs. All rights reserved.
 *
 * $Id$
 */

package org.dellroad.hl7;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Represents an MSH segment.
 *
 * <p>
 * This subclass does not allow accessing MSH.1 or MSH.2 except
 * indirectly via {@link #getHL7Seps} and {@link #setHL7Seps setHL7Seps()}.
 */
@SuppressWarnings("serial")
public class MSHSegment extends HL7Segment {

    public static final String DEFAULT_PROCESSING_ID = "P";
    public static final String DEFAULT_VERSION_ID = "2.3";

    public static final String MSH_SEGMENT_NAME = "MSH";
    public static final String MSA_SEGMENT_NAME = "MSA";

    public static final String TIMESTAMP_FORMAT = "yyyyMMddHHmmss.SSSZ";

    private static final String ACK = "ACK";
    private static final HL7Field ACK_MSH_9 = new HL7Field(ACK);
    private static final HL7Field ACK_MSA_1 = new HL7Field("AA");

    /**
     * Parsing constructor.
     *
     * @param line encoded HL7 MSH segment
     * @throws HL7ContentException if segment is invalid
     */
    public MSHSegment(String line) throws HL7ContentException {

        // Parse "MSH", MSH.1, and MSH.2
        if (!line.startsWith(MSH_SEGMENT_NAME))
            throw new HL7ContentException("MSH segment does not start with `" + MSH_SEGMENT_NAME + "'").setContent(line);
        this.fields.add(new HL7Field(MSH_SEGMENT_NAME));
        if (line.length() < 6)
            throw new HL7ContentException("MSH segment is truncated").setContent(line);
        char fieldSep = line.charAt(3);
        char repSep = line.charAt(5);
        char compSep = line.charAt(4);
        char subSep = '\u0000';
        char escChar = '\u0000';
        int fieldSep23 = 6;
        if (line.length() >= 7 && line.charAt(6) != fieldSep) {
            fieldSep23++;
            escChar = line.charAt(6);
            if (line.length() >= 8 && line.charAt(7) != fieldSep) {
                fieldSep23++;
                subSep = line.charAt(7);
            }
        }
        HL7Seps seps = new HL7Seps(fieldSep, compSep, repSep, escChar, subSep);
        this.setHL7Seps(seps);

        // Parse remaining fields (if any)
        if (fieldSep23 == line.length())
            return;
        if (line.charAt(fieldSep23) != fieldSep)
            throw new HL7ContentException("bogus extra characters in MSH.2").setContent(line);
        this.parseAndAddFields(line.substring(fieldSep23 + 1), seps);
    }

    /**
     * Constructor.
     *
     * This constructor initializes only MSH.1 and MSH.2.
     *
     * @param seps separator and escape characters for this message
     */
    public MSHSegment(HL7Seps seps) {
        this.fields.add(new HL7Field(MSH_SEGMENT_NAME));
        this.setHL7Seps(seps);
    }

    /**
     * Convenience constructor. Equivalent to:
     *  <blockquote>
     *  <code>MSHSegment(HL7Seps.DEFAULT)</code>
     *  <blockquote>
     */
    public MSHSegment() {
        this(HL7Seps.DEFAULT);
    }

    /**
     * Copy constructor.
     */
    public MSHSegment(MSHSegment msh) {
        super(msh);
    }

    /**
     * Returns sending application from MSH.3.
     *
     * @return MSH.3, or null if there is none
     */
    public HL7Field getSendingApplication() {
        return getField(3);
    }
    public void setSendingApplication(HL7Field field) {
        this.setField(3, field);
    }

    /**
     * Returns sending facility from MSH.4.
     *
     * @return MSH.4, or null if there is none
     */
    public HL7Field getSendingFacility() {
        return getField(4);
    }
    public void setSendingFacility(HL7Field field) {
        this.setField(4, field);
    }

    /**
     * Returns receiving application from MSH.5.
     *
     * @return MSH.5, or null if there is none
     */
    public HL7Field getReceivingApplication() {
        return getField(5);
    }
    public void setReceivingApplication(HL7Field field) {
        this.setField(5, field);
    }

    /**
     * Returns receiving facility from MSH.6.
     *
     * @return MSH.6, or null if there is none
     */
    public HL7Field getReceivingFacility() {
        return getField(6);
    }
    public void setReceivingFacility(HL7Field field) {
        this.setField(6, field);
    }

    /**
     * Returns message timestamp from MSH.7.
     *
     * @return MSH.7, or null if there is none
     */
    public HL7Field getTimestamp() {
        return getField(7);
    }
    public void setTimestamp(HL7Field field) {
        this.setField(7, field);
    }

    /**
     * Returns message type from MSH.9.
     *
     * @return MSH.9, or null if there is none
     */
    public HL7Field getMessageType() {
        return getField(9);
    }
    public void setMessageType(HL7Field field) {
        this.setField(9, field);
    }

    /**
     * Returns control ID from MSH.10.
     *
     * @return MSH.10, or null if there is none
     */
    public HL7Field getControlID() {
        return getField(10);
    }
    public void setControlID(HL7Field field) {
        this.setField(10, field);
    }

    /**
     * Returns control ID from MSH.11.
     *
     * @return MSH.11, or null if there is none
     */
    public HL7Field getProcessingID() {
        return getField(11);
    }
    public void setProcessingID(HL7Field field) {
        this.setField(11, field);
    }

    /**
     * Returns HL7 version from MSH.12.
     *
     * @return MSH.12, or null if there is none
     */
    public HL7Field getVersionID() {
        return getField(12);
    }
    public void setVersionID(HL7Field field) {
        this.setField(12, field);
    }

    /**
     * Get the separator and escape characters defined by MSH.1 and MSH.2.
     */
    public HL7Seps getHL7Seps() {
        String msh1 = this.getField(1).get(0, 0, 0);
        String msh2 = this.getField(2).get(0, 0, 0);
        try {
            return new HL7Seps(msh1.charAt(0), msh2.charAt(0), msh2.charAt(1),
              msh2.length() > 2 ? msh2.charAt(2) : '\u0000',
              msh2.length() > 3 ? msh2.charAt(3) : '\u0000');
        } catch (HL7ContentException e) {
            throw new RuntimeException("impossible", e);
        }
    }

    /**
     * Set MSH.1 and MSH.2 based on the provided separator and
     * escape characters.
     */
    public void setHL7Seps(HL7Seps seps) {
        super.setField(1, new HL7Field("" + seps.getFieldSep()));
        super.setField(2, new HL7Field("" + seps.getCompSep() + seps.getRepSep()
          + (seps.hasEscapeCharacter() ? "" + seps.getEscChar() : "")
          + (seps.hasSubcomponentSeparator() ? "" + seps.getSubSep() : "")));
    }

    /**
     * This subclass version disallows trimming away MSH.1 or MSH.2.
     *
     * @throws IllegalArgumentException if <code>size</code> is less than three
     */
    @Override
    public void trimTo(int size) {
        if (size < 3)
            throw new IllegalArgumentException("size < 3");
        super.trimTo(size);
    }

    /**
     * This subclass version disallows setting MSH.1 or MSH.2 directly.
     *
     * @throws IllegalArgumentException if <code>index</code> is less than three
     * @throws IllegalArgumentException if field is null
     */
    @Override
    public void setField(int index, HL7Field field) {
        if (index < 3)
            throw new IllegalArgumentException("index < 3");
        super.setField(index, field);
    }

    /**
     * Create an acknowlegement message for this message.
     *
     * @param serno next local HL7 serial number
     * @return an acknowlegement of this message
     * @throws HL7ContentException if the required fields are not present
     *  in this instance
     */
    public HL7Message createACK(long serno) throws HL7ContentException {

        // We must have at least up to MSH.11
        if (getNumFields() < 12)
            throw new HL7ContentException("insufficient fields for ACK'ing");
        HL7Field versionID = this.getVersionID() != null ? this.getVersionID() : new HL7Field(DEFAULT_VERSION_ID);

        // Build ACK
        HL7Message ack = new HL7Message();
        MSHSegment msh = ack.getMSHSegment();
        msh.setTimestamp(new HL7Field(new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Date())));
        msh.setMessageType(ACK_MSH_9);
        msh.setControlID(new HL7Field("" + serno));
        msh.setProcessingID(this.getProcessingID());
        msh.setVersionID(versionID);
        HL7Segment msa = new HL7Segment(MSA_SEGMENT_NAME);
        msa.setField(1, ACK_MSA_1);
        msa.setField(2, this.getControlID());
        ack.getSegments().add(msa);

        // Done
        return ack;
    }

    /**
     * Check whether the given message is an acknowlegement of this MSH.
     *
     * @param ack putative acknowlegement of this message
     * @throws HL7ContentException if the required fields are not present
     *  in this instance
     */
    public boolean isACK(HL7Message ack) throws HL7ContentException {
        HL7Field controlID = this.getControlID();
        HL7Field procID = this.getProcessingID();
        if (controlID == null || procID == null)
            throw new HL7ContentException("insufficient fields for ACK'ing");
        MSHSegment ackMSH = ack.getMSHSegment();
        return ACK.equals(ack.getField("MSH.9").get(0, 0, 0))
          && controlID.equals(ack.getField("MSA.2"))
          && procID.equals(ackMSH.getProcessingID())
          && ACK_MSA_1.equals(ack.getField("MSA.1"));
    }

    /**
     * Append string encoding of this segment to the provided buffer.
     *
     * <p>
     * This overridden version ensures that MSH.1 and MSH.2 are not escaped.
     */
    @Override
    public void append(StringBuilder buf, HL7Seps seps) {
        HL7Field[] fields = this.fields.toArray(new HL7Field[this.fields.size()]);
        buf.append(fields[0]);
        buf.append(seps);
        for (int i = 3; i < fields.length; i++) {
            buf.append(seps.getFieldSep());
            fields[i].append(buf, seps);
        }
    }

    /**
     * Convert to a string using the provided separators.
     *
     * <p>
     * This overridden version ensures that MSH.1 and MSH.2 are consistent with <code>seps</code> in the generated string.
     * This separators configured in this instance are not affected regardless.
     */
    @Override
    public String toString(HL7Seps seps) {
        if (seps.equals(getHL7Seps()))
            return super.toString(seps);
        MSHSegment copy = new MSHSegment(this);
        copy.setHL7Seps(seps);
        return copy.toString();
    }

    @Override
    public String toString() {
        return super.toString(getHL7Seps());
    }
}

