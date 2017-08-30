
/*
 * Copyright (C) 2008 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.hl7;

import java.util.ArrayList;
import java.util.Iterator;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

public class HL7SegmentTest {

    @Test(dataProvider = "getParseData")
    public void testParseConstructor(String line, HL7Segment expected)
      throws HL7ContentException {
        HL7Segment actual;
        try {
            actual = new HL7Segment(line, HL7Seps.DEFAULT);
        } catch (HL7ContentException e) {
            actual = null;
        }
        assertEquals(actual, expected);
    }

    @DataProvider(name = "getParseData")
    public Iterator<Object[]> genParseData() throws HL7ContentException {
        ArrayList<Object[]> list = new ArrayList<Object[]>();
        list.add(new Object[] { "A", null });
        list.add(new Object[] { "\r", null });
        list.add(new Object[] { "\n", null });
        list.add(new Object[] { "AB", null });
        list.add(new Object[] { "ABCD", null });
        list.add(new Object[] { "ABC|DEF", new HL7Segment("ABC",
          new HL7Field[] { new HL7Field("DEF") }) });
        list.add(new Object[] { "ABC|DEF^GHI|JKL", new HL7Segment("ABC",
          new HL7Field[] {
           new HL7Field(new String[][][] { { { "DEF" }, { "GHI" } } }),
           new HL7Field("JKL") }) });
        return list.iterator();
    }

    @Test
    public void testInvalid() throws HL7ContentException {
        HL7Segment seg = new HL7Segment("FOO|bar^jan||foo", HL7Seps.DEFAULT);
        try {
            seg.setField(-1, "ABC");
            assert false;
        } catch (IllegalArgumentException e) {
            // ok
        }
        try {
            seg.setField(0, "ABC");
            assert false;
        } catch (IllegalArgumentException e) {
            // ok
        }
        try {
            seg.setField(1, (HL7Field)null);
            assert false;
        } catch (IllegalArgumentException e) {
            // ok
        }
        try {
            seg.setName("zeeee");
            assert false;
        } catch (HL7ContentException e) {
            // ok
        }
        try {
            seg.appendField(null);
            assert false;
        } catch (IllegalArgumentException e) {
            // ok
        }
    }
}

