
/*
 * Copyright (C) 2008 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.hl7;

import java.util.ArrayList;

/**
 * List of {@link HL7Segment} objects that requires there to always be an initial MSH segment.
 * In particular, this class enforces the following constraints:
 * <ul>
 *  <li>There must be at least one item in the list</li>
 *  <li>The first item in the list must be an {@link MSHSegment}</li>
 * </ul>
 *
 * Illegal operations result in an {@link IllegalStateException} being thrown.
 *
 * <p>
 * Note we do not enforce the other segments to be anything, not even non-null.
 */
@SuppressWarnings("serial")
public final class HL7SegmentList extends ArrayList<HL7Segment> {

    /**
     * Constructor.
     *
     * @param msh MSH segment
     * @throws IllegalArgumentException if <code>msh</code> is null
     */
    public HL7SegmentList(MSHSegment msh) {
        if (msh == null)
            throw new IllegalArgumentException("null msh");
        this.add(msh);
    }

    @Override
    public void clear() {
        throw new IllegalStateException("list can't be empty");
    }

    @Override
    public HL7Segment remove(int index) {
        if (index == 0 && !(this.get(0) instanceof MSHSegment))
            throw new IllegalStateException("can't remove initial MSH");
        return super.remove(index);
    }

    @Override
    public boolean remove(Object obj) {
        if (obj != null && obj.equals(get(0)))
            throw new IllegalStateException("can't remove initial MSH");
        return super.remove(obj);
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        if (fromIndex <= 0 && toIndex > 0 && !(this.get(0) instanceof MSHSegment))
            throw new IllegalStateException("can't remove initial MSH");
        super.removeRange(fromIndex, toIndex);
    }

    @Override
    public HL7Segment set(int index, HL7Segment segment) {
        if (index == 0 && !(segment instanceof MSHSegment))
            throw new IllegalStateException("can't replace initial MSH segment with non-MSH segment");
        return super.set(index, segment);
    }
}

