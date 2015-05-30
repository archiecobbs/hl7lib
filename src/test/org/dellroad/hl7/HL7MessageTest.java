
/*
 * Copyright (C) 2008 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.hl7;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

public class HL7MessageTest extends Input1Test {

    @Test
    public void testParseConstructor() throws HL7ContentException {
        HL7Message[] tests = new HL7Message[] { this.msg1, this.msg2 };
        for (HL7Message msg : tests)
            assertEquals(new HL7Message(msg.toString()), msg);
    }

    @Test
    public void testHBOCWorkaround() throws IOException, HL7ContentException {
        StringWriter writer = new StringWriter();
        InputStreamReader reader = new InputStreamReader(
          getClass().getResourceAsStream("input2.bin"));
        char[] buf = new char[100];
        for (int r; (r = reader.read(buf)) != -1; )
            writer.write(buf, 0, r);
        assertEquals(new HL7Message(writer.toString()), this.msg2);
    }

    @Test
    public void testFindSegment() {
        assertEquals(this.msg1.findSegment("MSH"), this.msg1.getMSHSegment());
        assertEquals(this.msg1.findSegment("ZAX"),
          this.msg1.getSegments().get(4));
        assertEquals(this.msg1.findSegment("ZAX", 5),
          this.msg1.getSegments().get(5));
        assertEquals(this.msg1.findSegment("ZAX", 6), null);
    }

    @Test(dataProvider = "getFieldData")
    public void testGetField(String field, int segnum, HL7Field value) {
        HL7Field gotten = this.msg2.getField(field, segnum);
        assertEquals(gotten, value);
        if (value != null)
            assertEquals(gotten.hashCode(), value.hashCode());
    }

    @DataProvider(name = "getFieldData")
    public Iterator<Object[]> genGetFieldData() {
        ArrayList<Object[]> list = new ArrayList<Object[]>();
        list.add(new Object[] { "MSH.0", 0, new HL7Field("MSH") });
        list.add(new Object[] { "MSH.1", 0, new HL7Field("|") });
        list.add(new Object[] { "MSH.2", 0, new HL7Field("^~\\&") });
        list.add(new Object[] { "MSH.8", 0, HL7Field.EMPTY });
        list.add(new Object[] { "MSH.9", 0, new HL7Field(
          new String[][][] { { { "ADT" }, { "A08" } } })});
        list.add(new Object[] { "MSH.14", 0, HL7Field.EMPTY });
        list.add(new Object[] { "MSH.21", 0, HL7Field.EMPTY });
        list.add(new Object[] { "MSH.22", 0, new HL7Field("2.2b") });
        list.add(new Object[] { "MSH.23", 0, null });
        list.add(new Object[] { "EVN.1", 0, new HL7Field("A08") });
        list.add(new Object[] { "EVN.5", 0, new HL7Field("MRP") });
        list.add(new Object[] { "PID.3", 0, new HL7Field(new String[][][] {
          { { "988747372" }, { null }, { "" }, { "ST01B" }, { "MR" } },
          { { "983928341" }, { "" }, { null }, { "ST01C" }, { "MR" } },
          { { "93823848" }, { "" }, { "" }, { "ST01" }, { "PI" } }
        })});
        list.add(new Object[] { "PID.11", 0, new HL7Field(new String[][][] {
          { { "123 MAIN ST" }, { "" }, { "ANYTOWN" }, { "AL" }, { "35432" },
            { "USA" }, { "H" }, { "01" }, { "" }, { "" }, { "" }, { "" },
            { "" }, { "" }, { "" }, { "" }, { "Y" } }
        })});
        HL7Field foo = new HL7Field(new String[][][] {
          { { "AAA" }, { "111", "222", null, "444" },
            { "" }, { "3333" }, { "" } },
          { { "BBB" }, { "111", "222", "", null }, { "" }, { "3333" } }
        });
        list.add(new Object[] { "ZAX.1", 0, foo });
        list.add(new Object[] { "ZAX.1", 4, foo });
        list.add(new Object[] { "ZAX.1", 5, new HL7Field("two") });
        list.add(new Object[] { "ZAX.1", 6, null });
        list.add(new Object[] { "ZAX.3", 0,
          new HL7Field(new String[] { null, "", null }) });
        list.add(new Object[] { "ZAX.4", 0, new HL7Field(new String[][][] {
          { { "escapes1=:;~\\&" }, { "escapes2=|^~\\&" } }
        })});
        list.add(new Object[] { "ZAX.5", 0, HL7Field.EMPTY });
        list.add(new Object[] { "ZAX.6", 0, null });
        return list.iterator();
    }

    @Test(dataProvider = "getData")
    public void testGet(String field, int repeat, String value) {
        assertEquals(this.msg2.get(field, 0, repeat), value);
        if (field.substring(4).indexOf('.') == -1) {
            assertEquals(this.msg2.get(field + ".1", 0, repeat), value);
            assertEquals(this.msg2.get(field + ".1.1", 0, repeat), value);
        } else if (field.substring(6).indexOf('.') == -1)
            assertEquals(this.msg2.get(field + ".1", 0, repeat), value);
    }

    @DataProvider(name = "getData")
    public Iterator<Object[]> genGetData() {
        ArrayList<Object[]> list = new ArrayList<Object[]>();
        list.add(new Object[] { "MSH.0", 0, "MSH" });
        list.add(new Object[] { "MSH.1", 0, "|" });
        list.add(new Object[] { "MSH.2", 0, "^~\\&" });
        list.add(new Object[] { "MSH.3", 0, "ST01" });
        list.add(new Object[] { "MSH.4", 0, "B" });
        list.add(new Object[] { "MSH.5", 0, "IM" });
        list.add(new Object[] { "MSH.6", 0, "B" });
        list.add(new Object[] { "MSH.7", 0, "20061211154319" });
        list.add(new Object[] { "MSH.8", 0, "" });
        list.add(new Object[] { "MSH.9.1", 0, "ADT" });
        list.add(new Object[] { "MSH.9.2", 0, "A08" });
        list.add(new Object[] { "MSH.10", 0, "6917898" });
        list.add(new Object[] { "MSH.11", 0, "P" });
        list.add(new Object[] { "MSH.12", 0, "2.2" });
        list.add(new Object[] { "MSH.13", 0, "6917898" });
        list.add(new Object[] { "MSH.14", 0, "" });
        list.add(new Object[] { "MSH.15", 0, "AL" });
        list.add(new Object[] { "MSH.16", 0, "" });
        list.add(new Object[] { "MSH.17", 0, "" });
        list.add(new Object[] { "MSH.18", 0, "" });
        list.add(new Object[] { "MSH.19", 0, "" });
        list.add(new Object[] { "MSH.20", 0, "" });
        list.add(new Object[] { "MSH.21", 0, "" });
        list.add(new Object[] { "MSH.22", 0, "2.2b" });
        list.add(new Object[] { "MSH.23", 0, null });
        list.add(new Object[] { "EVN.1", 0, "A08" });
        list.add(new Object[] { "EVN.5", 0, "MRP" });
        list.add(new Object[] { "PID.1", 0, "1" });
        list.add(new Object[] { "PID.2.1", 0, "01001991" });
        list.add(new Object[] { "PID.2.2", 0, "" });
        list.add(new Object[] { "PID.2.3", 0, "" });
        list.add(new Object[] { "PID.2.4", 0, "ST01" });
        list.add(new Object[] { "PID.2.5", 0, null });
        list.add(new Object[] { "PID.3.1", 0, "988747372" });
        list.add(new Object[] { "PID.3.2", 0, "" });
        list.add(new Object[] { "PID.3.3", 0, "" });
        list.add(new Object[] { "PID.3.4", 0, "ST01B" });
        list.add(new Object[] { "PID.3.5", 0, "MR" });
        list.add(new Object[] { "PID.3.6", 0, null });
        list.add(new Object[] { "PID.3.1", 1, "983928341" });
        list.add(new Object[] { "PID.3.2", 1, "" });
        list.add(new Object[] { "PID.3.3", 1, "" });
        list.add(new Object[] { "PID.3.4", 1, "ST01C" });
        list.add(new Object[] { "PID.3.5", 1, "MR" });
        list.add(new Object[] { "PID.3.6", 1, null });
        list.add(new Object[] { "PID.3.1", 2, "93823848" });
        list.add(new Object[] { "PID.3.2", 2, "" });
        list.add(new Object[] { "PID.3.3", 2, "" });
        list.add(new Object[] { "PID.3.4", 2, "ST01" });
        list.add(new Object[] { "PID.3.5", 2, "PI" });
        list.add(new Object[] { "PID.3.6", 2, null });
        list.add(new Object[] { "PID.4", 0, "" });
        list.add(new Object[] { "PID.5.1", 0, "JONES6" });
        list.add(new Object[] { "PID.5.2", 0, "SMITH" });
        list.add(new Object[] { "PID.5.3", 0, "P" });
        list.add(new Object[] { "PID.5.4", 0, "\"\"" });
        list.add(new Object[] { "PID.11.1", 0, "123 MAIN ST" });
        list.add(new Object[] { "ZAX.1", 0, "AAA" });
        list.add(new Object[] { "ZAX.1.2.1", 0, "111" });
        list.add(new Object[] { "ZAX.1.2.2", 0, "222" });
        list.add(new Object[] { "ZAX.1.2.3", 0, "" });
        list.add(new Object[] { "ZAX.1.2.4", 0, "444" });
        list.add(new Object[] { "ZAX.2", 0, "foobar" });
        list.add(new Object[] { "ZAX.2.1.2", 0, null });
        list.add(new Object[] { "ZAX.3", 0, "" });
        list.add(new Object[] { "ZAX.3", 1, "" });
        list.add(new Object[] { "ZAX.3", 2, "" });
        list.add(new Object[] { "ZAX.3", 3, null });
        list.add(new Object[] { "ZAX.4.1", 0, "escapes1=:;~\\&" });
        list.add(new Object[] { "ZAX.4.2", 0, "escapes2=|^~\\&" });
        return list.iterator();
    }
}

