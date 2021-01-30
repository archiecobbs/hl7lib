
/*
 * Copyright (C) 2008 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.hl7.llp;

import java.nio.charset.Charset;

import org.dellroad.hl7.HL7Message;

/**
 * Strategy interface for determining the character encoding to use when reading and writing HL7 messages.
 */
public interface CharsetDecoder {

    /**
     * Determine the character encoding to use for an incoming HL7 message.
     *
     * @param buf raw HL7 message buffer
     * @param offset HL7 message offset in {@code buf}
     * @param length HL7 message length in {@code buf}
     * @return character encoding to use for the given message (never null)
     * @throws IllegalArgumentException if {@code buf} is null
     * @throws ArrayIndexOutOfBoundsException if {@code offset} and/or {@code length} are invalid
     */
    Charset charsetForIncomingMessage(byte[] buf, int offset, int length);

    /**
     * Determine the character encoding to use for an outgoing HL7 message.
     *
     * @param msg HL7 message
     * @return character encoding to use for the given message (never null)
     * @throws IllegalArgumentException if {@code msg} is null
     */
    Charset charsetForOutgoingMessage(HL7Message msg);

    /**
     * Create a {@link CharsetDecoder} that always returns the given {@link Charset}.
     *
     * @param charset character encoding to use
     * @return a fixed-answer {@link CharsetDecoder}
     * @throws IllegalArgumentException if {@code charset} is null
     */
    static CharsetDecoder fixed(final Charset charset) {
        return new CharsetDecoder() {
            @Override
            public Charset charsetForIncomingMessage(byte[] buf, int offset, int length) {
                if (buf == null)
                    throw new IllegalArgumentException("null buf ");
                return charset;
            }

            @Override
            public Charset charsetForOutgoingMessage(HL7Message msg) {
                if (msg == null)
                    throw new IllegalArgumentException("null msg");
                return charset;
            }
        };
    }
}
