
/*
 * Copyright (C) 2008 Archie L. Cobbs. All rights reserved.
 *
 * $Id$
 */

package org.dellroad.hl7;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertTrue;

public class MSHSegmentTest extends Input1Test {

    @Test
    public void testConstructor() throws HL7ContentException {
        MSHSegment msh1 = new MSHSegment();
        MSHSegment msh2 = new MSHSegment(HL7Seps.DEFAULT);
        assertEquals(msh1, msh2);
        assertEquals(msh1.getHL7Seps(), HL7Seps.DEFAULT);
        assertEquals(msh1.getHL7Seps(), msh2.getHL7Seps());
        assertEquals(msh1.getField(0).get(0, 0, 0), "MSH");
        assertEquals(msh1.getField(1).get(0, 0, 0), "|");
        assertEquals(msh1.getField(2).get(0, 0, 0), "^~\\&");

        HL7Seps seps = new HL7Seps('f', 'c', 'r', 'e', 's');
        MSHSegment msh3 = new MSHSegment(seps);
        assertNotSame(msh1, msh3);
        assertEquals(msh1.toString(seps), msh3.toString());
        assertEquals(msh3.getHL7Seps(), seps);
        assertEquals(msh3.getField(0).get(0, 0, 0), "MSH");
        assertEquals(msh3.getField(1).get(0, 0, 0), "f");
        assertEquals(msh3.getField(2).get(0, 0, 0), "cres");

        msh3.setHL7Seps(HL7Seps.DEFAULT);
        assertEquals(msh1, msh3);
        assertEquals(msh3.getField(0).get(0, 0, 0), "MSH");
        assertEquals(msh3.getField(1).get(0, 0, 0), "|");
        assertEquals(msh3.getField(2).get(0, 0, 0), "^~\\&");
    }

    @Test
    public void testCopyConstructor() {
        MSHSegment msh1 = new MSHSegment();
        msh1.setSendingApplication(new HL7Field("foobar"));
        MSHSegment msh2 = new MSHSegment(msh1);
        assertEquals(msh1, msh2);
        assertEquals("" + msh2.getSendingApplication(), "foobar");
    }

    @Test(dataProvider = "getParseData")
    public void testParseConstructor(String line, MSHSegment expected) {
        MSHSegment actual;
        try {
            actual = new MSHSegment(line);
        } catch (HL7ContentException e) {
            actual = null;
        }
        assertEquals(actual, expected);
    }

    @DataProvider(name = "getParseData")
    public Iterator<Object[]> genParseData() throws HL7ContentException {
        ArrayList<Object[]> list = new ArrayList<Object[]>();
        list.add(new Object[] { "MSHabcde",
          new MSHSegment(new HL7Seps('a', 'b', 'c', 'd', 'e')) });
        list.add(new Object[] { "MSHabcd",
          new MSHSegment(new HL7Seps('a', 'b', 'c', 'd')) });
        list.add(new Object[] { "MSHabc",
          new MSHSegment(new HL7Seps('a', 'b', 'c')) });
        list.add(new Object[] { "MSHabcdef", null });
        list.add(new Object[] { "MSHab", null });
        list.add(new Object[] { "MSHa", null });
        list.add(new Object[] { "MSH", null });
        list.add(new Object[] { "FOO|^~\\&", null });
        list.add(new Object[] { "MSH|^~\\&", new MSHSegment() });
        list.add(new Object[] { "MSH|^|", null });
        list.add(new Object[] { "MSH|^^", null });
        MSHSegment msh = new MSHSegment();
        for (int i = 3; i < 10; i++)
            msh.setField(i, "" + i);
        msh.setField(11, "11");
        list.add(new Object[] { "MSH|^~\\&|3|4|5|6|7|8|9||11", msh });
        return list.iterator();
    }

    @Test(dataProvider = "fieldNames")
    public void testFields(int index, String name) throws Exception {
        MSHSegment msh = new MSHSegment();
        Method getter = msh.getClass().getMethod("get" + name);
        Method setter = msh.getClass().getMethod("set" + name, HL7Field.class);
        HL7Field field = new HL7Field("foobar-" + name);
        setter.invoke(msh, field);
        assertEquals(msh.getField(index), field);
        assertEquals(getter.invoke(msh), field);
        field = new HL7Field("janfu-" + name);
        msh.setField(index, field);
        assertEquals(msh.getField(index), field);
        assertEquals(getter.invoke(msh), field);
    }

    @DataProvider(name = "fieldNames")
    public Iterator<Object[]> genFieldNames() throws HL7ContentException {
        ArrayList<Object[]> list = new ArrayList<Object[]>();
        list.add(new Object[] { 3, "SendingApplication" });
        list.add(new Object[] { 4, "SendingFacility" });
        list.add(new Object[] { 5, "ReceivingApplication" });
        list.add(new Object[] { 6, "ReceivingFacility" });
        list.add(new Object[] { 7, "Timestamp" });
        list.add(new Object[] { 9, "MessageType" });
        list.add(new Object[] { 10, "ControlID" });
        list.add(new Object[] { 11, "ProcessingID" });
        list.add(new Object[] { 12, "VersionID" });
        return list.iterator();
    }

    @Test
    public void testACK() throws HL7ContentException {
        MSHSegment msh = this.msg1.getMSHSegment();
        HL7Message ack = msh.createACK(1234);
        assertTrue(msh.isACK(ack));
        MSHSegment ackmsh = ack.getMSHSegment();
        assertEquals(ackmsh.getMessageType(), new HL7Field("ACK"));
        assertEquals(ackmsh.getProcessingID(), msh.getProcessingID());
        assertEquals(ack.get("MSA.0"), "MSA");
        assertEquals(ack.get("MSA.1"), "AA");
        assertEquals(ack.getField("MSA.2"), msh.getControlID());

        // Sometimes ACK MSH.9 also contains MSH.9.2 from original (e.g., "ACK^A08" instead of just "ACK")
        ackmsh.setMessageType(new HL7Field("ACK^A08", HL7Seps.DEFAULT));
        assertTrue(msh.isACK(ack));
    }

    @Test
    public void testInvalid() throws HL7ContentException {
        MSHSegment msh = new MSHSegment();
        try {
            msh.setField(0, "MSH");
            assert false;
        } catch (IllegalArgumentException e) {
            // ok
        }
        try {
            msh.setField(1, "|");
            assert false;
        } catch (IllegalArgumentException e) {
            // ok
        }
        try {
            msh.setField(2, "^~\\&");
            assert false;
        } catch (IllegalArgumentException e) {
            // ok
        }
        try {
            msh.trimTo(1);
            assert false;
        } catch (IllegalArgumentException e) {
            // ok
        }
        try {
            msh.createACK(0);
            assert false;
        } catch (HL7ContentException e) {
            // ok
        }
        try {
            msh.isACK(new HL7Message());
            assert false;
        } catch (HL7ContentException e) {
            // ok
        }
    }
}

