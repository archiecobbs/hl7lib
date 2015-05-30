
/*
 * Copyright (C) 2008 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.hl7;

import java.io.IOException;
import java.util.List;

import org.testng.annotations.BeforeClass;

public abstract class Input1Test extends InputTest {

    protected HL7Message msg1;
    protected HL7Message msg2;

    @BeforeClass
    public void loadMessages() throws HL7ContentException, IOException {
        List<HL7Message> list = readMessages(Input1Test.class.getResourceAsStream("input1.txt"));
        assert list.size() == 2;
        this.msg1 = list.get(0);
        this.msg2 = list.get(1);
    }
}

