
/*
 * Copyright (C) 2008 Archie L. Cobbs. All rights reserved.
 *
 * $Id$
 */

package org.dellroad.hl7.io;

import java.util.List;

import org.dellroad.hl7.HL7ContentException;
import org.dellroad.hl7.HL7Seps;
import org.dellroad.hl7.Input1Test;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

public class HL7FileReaderTest extends Input1Test {

    @Test
    public void testInput() {
        HL7Seps seps1;
        try {
            seps1 = new HL7Seps(':', ';', '~', '\\', '&');
        } catch (HL7ContentException e) {
            throw new RuntimeException("impossible");
        }
        HL7Seps seps2 = HL7Seps.DEFAULT;
        assertEquals(this.msg1.getMSHSegment().getHL7Seps(), seps1);
        assertEquals(this.msg2.getMSHSegment().getHL7Seps(), seps2);
        assertEquals(cdr(this.msg1.getSegments()),
          cdr(this.msg2.getSegments()));
        assertEquals(this.msg1.toString(seps2), this.msg2.toString());
        assertEquals(this.msg1.toString(), this.msg2.toString(seps1));
    }

    private <E> List<E> cdr(List<E> list) {
        return list.subList(1, list.size());
    }
}

