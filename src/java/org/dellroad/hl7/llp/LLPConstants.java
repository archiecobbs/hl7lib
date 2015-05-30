
/*
 * Copyright (C) 2008 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.hl7.llp;

import java.nio.charset.Charset;

/**
 * HL7 LLP framing constants.
 */
public interface LLPConstants {

    Charset CHARACTER_ENCODING = Charset.forName("ISO-8859-1");

    int LEADING_BYTE = 0x0b;
    int TRAILING_BYTE_0 = 0x1c;
    int TRAILING_BYTE_1 = 0x0d;
}

