
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

public class HL7UtilTest {

    @Test(dataProvider = "findData")
    public void testFind(String value,
      char sep, int start, int end, int[] result) {
        if (end == -1)
            end = value.length();
        assert Arrays.equals(HL7Util.find(value, sep, start, end), result);
    }

    @DataProvider(name = "findData")
    public Iterator<Object[]> genFindData() {
        ArrayList<Object[]> list = new ArrayList<Object[]>();
        list.add(new Object[] {
          "", '/', 0, -1, new int[] { 0 }
        });
        list.add(new Object[] {
          "/", '/', 0, -1, new int[] { 0, 1 }
        });
        list.add(new Object[] {
          "/a", '/', 0, -1, new int[] { 0, 2 }
        });
        list.add(new Object[] {
          "a/a", '/', 0, -1, new int[] { 1, 3 }
        });
        list.add(new Object[] {
          "a//a", '/', 0, -1, new int[] { 1, 2, 4 }
        });
        list.add(new Object[] {
          "a//", '/', 0, -1, new int[] { 1, 2, 3 }
        });
        list.add(new Object[] {
          "//a", '/', 0, -1, new int[] { 0, 1, 3 }
        });
        list.add(new Object[] {
          "abcdef", '/', 0, -1, new int[] { 6 }
        });
        list.add(new Object[] {
          "ab/cd/ef", '/', 0, -1, new int[] { 2, 5, 8 }
        });
        list.add(new Object[] {
          "ab/cd/ef", '/', 2, 4, new int[] { 2, 4 }
        });
        list.add(new Object[] {
          "ab/cd/ef", '/', 2, 5, new int[] { 2, 5 }
        });
        list.add(new Object[] {
          "ab/cd/ef", '/', 2, 6, new int[] { 2, 5, 6 }
        });
        return list.iterator();
    }
}

