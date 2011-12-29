
/*
 * Copyright (C) 2008 Archie L. Cobbs. All rights reserved.
 *
 * $Id$
 */

package org.dellroad.hl7;

import java.util.ArrayList;
import java.util.Iterator;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotSame;

public class HL7SepsTest {

    private final HL7Seps escapeSeps;

    public HL7SepsTest() throws HL7ContentException {
        this.escapeSeps = new HL7Seps('f', 'c', 'r', '$', 's');
    }

    @Test
    public void testDefault() throws HL7ContentException {
        HL7Seps seps = HL7Seps.DEFAULT;
        assertEquals(seps.getFieldSep(), HL7Seps.DEFAULT_FIELD_SEPARATOR);
        assertEquals(seps.getRepSep(), HL7Seps.DEFAULT_REPEAT_SEPARATOR);
        assertEquals(seps.getCompSep(), HL7Seps.DEFAULT_COMPONENT_SEPARATOR);
        assertEquals(seps.getSubSep(), HL7Seps.DEFAULT_SUBCOMPONENT_SEPARATOR);
        assertEquals(seps.getEscChar(), HL7Seps.DEFAULT_ESCAPE_CHARACTER);
        assertEquals(seps, seps);
        assertEquals(seps, HL7Seps.DEFAULT);
        assertEquals(HL7Seps.DEFAULT, new HL7Seps(
          HL7Seps.DEFAULT_FIELD_SEPARATOR,
          HL7Seps.DEFAULT_COMPONENT_SEPARATOR,
          HL7Seps.DEFAULT_REPEAT_SEPARATOR,
          HL7Seps.DEFAULT_ESCAPE_CHARACTER,
          HL7Seps.DEFAULT_SUBCOMPONENT_SEPARATOR));
        HL7Seps seps5 = new HL7Seps(HL7Seps.DEFAULT_FIELD_SEPARATOR,
          HL7Seps.DEFAULT_COMPONENT_SEPARATOR,
          HL7Seps.DEFAULT_REPEAT_SEPARATOR,
          (char)0,
          (char)0);
        assertNotSame(seps5, HL7Seps.DEFAULT);
        HL7Seps seps4 = new HL7Seps(HL7Seps.DEFAULT_FIELD_SEPARATOR,
          HL7Seps.DEFAULT_COMPONENT_SEPARATOR,
          HL7Seps.DEFAULT_REPEAT_SEPARATOR,
          (char)0);
        assertNotSame(seps4, HL7Seps.DEFAULT);
        HL7Seps seps3 = new HL7Seps(HL7Seps.DEFAULT_FIELD_SEPARATOR,
          HL7Seps.DEFAULT_REPEAT_SEPARATOR,
          HL7Seps.DEFAULT_COMPONENT_SEPARATOR);
        assertNotSame(seps3, HL7Seps.DEFAULT);
        assertEquals(seps5, seps4);
        assertEquals(seps5, seps3);
        assertEquals(seps4, seps3);
        assertEquals(new HL7Seps('a', 'b', 'c'),
          new HL7Seps('a', 'b', 'c', (char)0, (char)0));
        assertEquals(new HL7Seps('a', 'b', 'c', 'e'),
          new HL7Seps('a', 'b', 'c', 'e', (char)0));
    }

    @Test
    public void testBogus() {
        try {
            new HL7Seps('a', 'b', 'c', 'd', 'a');
            assert false;
        } catch (HL7ContentException e) {
            // ignore
        }
        try {
            new HL7Seps('a', 'b', 'c', 'd', 'd');
            assert false;
        } catch (HL7ContentException e) {
            // ignore
        }
        try {
            new HL7Seps('a', 'b', 'a');
            assert false;
        } catch (HL7ContentException e) {
            // ignore
        }
        try {
            new HL7Seps('\u001f', 'b', 'c');
            assert false;
        } catch (HL7ContentException e) {
            // ignore
        }
        try {
            new HL7Seps('\u007f', 'b', 'c');
            assert false;
        } catch (HL7ContentException e) {
            // ignore
        }
        try {
            new HL7Seps('a', 'b', 'c', (char)0, 'd');
            assert false;
        } catch (HL7ContentException e) {
            // ignore
        }
        try {
            new HL7Seps('a', 'b', 'c', 'd', (char)0);
        } catch (HL7ContentException e) {
            assert false;
        }
    }

    @Test
    public void testEscapeFail() throws HL7ContentException {
        HL7Seps seps = new HL7Seps('a', 'b', 'c');
        assert !seps.hasSubcomponentSeparator();
        assert !seps.hasEscapeCharacter();
        assertEquals(seps.escape("a"), "");
        assertEquals(seps.escape("b"), "");
        assertEquals(seps.escape("c"), "");
        assertEquals(seps.escape("d"), "d");
        assertEquals(seps.escape("\\"), "\\");
    }

    @Test(dataProvider = "escapeData")
    public void testEscape(String input, String output)
      throws HL7ContentException {
        assertEquals(this.escapeSeps.escape(input), output);
    }

    // Contains reversible escape examples
    @DataProvider(name = "escapeData")
    public Iterator<Object[]> genEscapeData() {
        ArrayList<Object[]> list = new ArrayList<Object[]>();
        list.add(new Object[] { "f", "$F$" });
        list.add(new Object[] { "r", "$R$" });
        list.add(new Object[] { "c", "$S$" });
        list.add(new Object[] { "s", "$T$" });
        list.add(new Object[] { "$", "$E$" });
        list.add(new Object[] { "xxf", "xx$F$" });
        list.add(new Object[] { "xxr", "xx$R$" });
        list.add(new Object[] { "xxc", "xx$S$" });
        list.add(new Object[] { "xxs", "xx$T$" });
        list.add(new Object[] { "xx$", "xx$E$" });
        list.add(new Object[] { "fxx", "$F$xx" });
        list.add(new Object[] { "rxx", "$R$xx" });
        list.add(new Object[] { "cxx", "$S$xx" });
        list.add(new Object[] { "sxx", "$T$xx" });
        list.add(new Object[] { "$xx", "$E$xx" });
        list.add(new Object[] { "yyfxx", "yy$F$xx" });
        list.add(new Object[] { "yyrxx", "yy$R$xx" });
        list.add(new Object[] { "yycxx", "yy$S$xx" });
        list.add(new Object[] { "yysxx", "yy$T$xx" });
        list.add(new Object[] { "yy$xx", "yy$E$xx" });
        list.add(new Object[] { "frcs$", "$F$$R$$S$$T$$E$" });
        list.add(new Object[] { "aafrcs$zz", "aa$F$$R$$S$$T$$E$zz" });
        list.add(new Object[] {
          "a serf circus", "a $T$e$R$$F$ $S$i$R$$S$u$T$" });
        return list.iterator();
    }

    @Test(dataProvider = "escapeData")
    public void testEscapeReverse(String input, String output) {
        assertEquals(this.escapeSeps.unescape(output), input);
    }

    @Test(dataProvider = "unescapeData")
    public void testUnescape(String escaped, String unescaped) {
        assertEquals(this.escapeSeps.unescape(escaped), unescaped);
    }

    // Contains not-necessarily-reversible escape examples
    @DataProvider(name = "unescapeData")
    public Iterator<Object[]> genUnescapeData() {
        ArrayList<Object[]> list = new ArrayList<Object[]>();
        list.add(new Object[] { "foo $bar", "foo $bar" });
        list.add(new Object[] { "foo $", "foo $" });
        list.add(new Object[] { "$", "$" });
        list.add(new Object[] { "$foo", "$foo" });
        list.add(new Object[] { "foo $$ bar", "foo  bar" });
        list.add(new Object[] { "foo $a$ bar", "foo  bar" });
        list.add(new Object[] { "foo $bb$ bar", "foo  bar" });
        list.add(new Object[] { "foobar $jam$", "foobar " });
        list.add(new Object[] { "$foo$jam$bam", "jam$bam" });
        return list.iterator();
    }

    @Test(dataProvider = "toStringData")
    public void testToString(HL7Seps seps, String string) {
        assertEquals(seps.toString(), string);
    }

    @DataProvider(name = "toStringData")
    public Iterator<Object[]> genToStringData() throws HL7ContentException {
        ArrayList<Object[]> list = new ArrayList<Object[]>();
        list.add(new Object[] { HL7Seps.DEFAULT, "|^~\\&" });
        list.add(new Object[] { new HL7Seps('a', 'b', 'c'), "abc" });
        list.add(new Object[] { new HL7Seps('a', 'b', 'c', 'd'), "abcd" });
        list.add(new Object[] {
          new HL7Seps('a', 'b', 'c', 'd', 'e'), "abcde" });
        return list.iterator();
    }
}

