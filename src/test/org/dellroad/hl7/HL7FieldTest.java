
/*
 * Copyright (C) 2008 Archie L. Cobbs. All rights reserved.
 *
 * $Id$
 */

package org.dellroad.hl7;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

public class HL7FieldTest {

    @Test
    public void testEmpty() {
        testEmpty(HL7Field.EMPTY);
        testEmpty(new HL7Field((String)null));
        testEmpty(new HL7Field(""));
        testEmpty(new HL7Field(new String[] { null }));
        testEmpty(new HL7Field(new String[] { "" }));
        testEmpty(new HL7Field(new String[][][] { { { null } } }));
        testEmpty(new HL7Field(new String[][][] { { { "" } } }));
        testEmpty(new HL7Field("", HL7Seps.DEFAULT));
    }

    public void testEmpty(HL7Field empty) {
        assert empty.isEmpty();
        assertEquals(empty, HL7Field.EMPTY);
        assertEquals(empty.hashCode(), HL7Field.EMPTY.hashCode());
    }

    @Test
    public void testExceptions() {
        try {
            new HL7Field((String[][][])null);
            assert false;
        } catch (NullPointerException e) {
        }
        try {
            new HL7Field(new String[][][] { { { "foo" } }, null });
            assert false;
        } catch (NullPointerException e) {
        }
        try {
            new HL7Field(new String[][][] { { { "foo" }, null } });
            assert false;
        } catch (NullPointerException e) {
        }
    }

    @Test(dataProvider = "simpleValues")
    public void testSimple(String value) {
        HL7Field field = new HL7Field(value);
        assert field.isEmpty() == (value.length() == 0);
        assert value.equals(field.get(0, 0, 0));
        String escaped = HL7Seps.DEFAULT.escape(value);
        assertEquals(field.toString(), escaped);
        String[][][] array = field.getValue();
        assert array.length == 1;
        assert array[0].length == 1;
        assert array[0][0].length == 1;
        assertEquals(array[0][0][0], value);
    }

    @DataProvider(name = "simpleValues")
    public Iterator<Object[]> genSimpleValues() {
        ArrayList<Object[]> list = new ArrayList<Object[]>();
        list.add(new Object[] { "" });
        list.add(new Object[] { "foo" });
        list.add(new Object[] { "bar" });
        list.add(new Object[] { "a b c" });
        list.add(new Object[] { "|&^%$#@" });
        return list.iterator();
    }

    @Test(dataProvider = "repeatedValues")
    public void testRepeated(String[] values, String encoded) {
        HL7Field field = new HL7Field(values);
        assert !field.isEmpty();
        String[][][] array = field.getValue();
        assert array.length == values.length;
        for (int i = 0; i < array.length; i++) {
            String[][] repeat = array[i];
            assert repeat.length == 1;
            assert repeat[0].length == 1;
            assert repeat[0][0] == values[i];
        }
        assertEquals(field.toString(), encoded);
    }

    @DataProvider(name = "repeatedValues")
    public Iterator<Object[]> genRepeatedValues() {
        ArrayList<Object[]> list = new ArrayList<Object[]>();
        list.add(new Object[] {
          new String[] { "abc" }, "abc" });
        list.add(new Object[] {
          new String[] { "", "" }, "~" });
        list.add(new Object[] {
          new String[] { "abc", "def" }, "abc~def" });
        list.add(new Object[] {
          new String[] { "abc", "", "def" }, "abc~~def" });
        list.add(new Object[] {
          new String[] { "a~c", "d~f" }, "a\\R\\c~d\\R\\f" });
        list.add(new Object[] {
          new String[] { "", "a", "", "b", "", "c" }, "~a~~b~~c" });
        return list.iterator();
    }

    @Test(dataProvider = "complexValues")
    public void testComplex(String value, String[][][] parse) {

        // Test parse
        HL7Field field = new HL7Field(value, HL7Seps.DEFAULT);
        assert Arrays.deepEquals(field.getValue(), parse);
        assertEquals(field.toString(), value);

        // Test 3-dim constructor
        HL7Field field2 = new HL7Field(parse);
        assert Arrays.deepEquals(field2.getValue(), parse);
        assertEquals(field2.toString(), value);

        // They should be equivalent
        assertEquals(field, field2);
    }

    @DataProvider(name = "complexValues")
    public Iterator<Object[]> genComplex() {
        ArrayList<Object[]> list = new ArrayList<Object[]>();
        list.add(new Object[] { "abc~def",
          new String[][][] {
            { { "abc" } }, { { "def" } }
          }});
        list.add(new Object[] { "ab\\F\\cd&efg^hijk~foo^b&ar~bleh&&s~~adf^^a",
          new String[][][] {
            { { "ab|cd", "efg" }, { "hijk" } },
            { { "foo" }, { "b", "ar" } },
            { { "bleh", "", "s" } },
            { { "" } },
            { { "adf" }, { "" }, { "a" } },
          }});
        return list.iterator();
    }
}

