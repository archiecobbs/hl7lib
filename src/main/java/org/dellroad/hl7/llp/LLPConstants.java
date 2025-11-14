
/*
 * Copyright (C) 2008 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.hl7.llp;

/**
 * HL7 LLP framing constants.
 */
public final class LLPConstants {

    /** Framing start byte. */
    public static final int LEADING_BYTE = 0x0b;
    /** Framing end byte #1. */
    public static final int TRAILING_BYTE_0 = 0x1c;
    /** Framing end byte #2. */
    public static final int TRAILING_BYTE_1 = 0x0d;

    private LLPConstants() {
    }
}

