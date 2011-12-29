
/*
 * Copyright (C) 2008 Archie L. Cobbs. All rights reserved.
 *
 * $Id$
 */

package org.dellroad.hl7.llp;

/**
 * HL7 LLP framing constants.
 */
public interface LLPConstants {

    String CHARACTER_ENCODING = "ISO-8859-1";

    int LEADING_BYTE = 0x0b;
    int TRAILING_BYTE_0 = 0x1c;
    int TRAILING_BYTE_1 = 0x0d;
}

