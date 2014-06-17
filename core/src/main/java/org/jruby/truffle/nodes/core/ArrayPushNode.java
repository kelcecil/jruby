/*
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.core;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import org.jruby.truffle.nodes.*;
import org.jruby.truffle.runtime.*;
import org.jruby.truffle.runtime.core.RubyArray;

public class ArrayPushNode extends RubyNode {

    @Child protected RubyNode array;
    @Child protected RubyNode pushed;
    @Child protected ArrayAllocationSite arrayAllocationSite;

    public ArrayPushNode(RubyContext context, SourceSection sourceSection, RubyNode array, RubyNode pushed) {
        super(context, sourceSection);
        this.array = array;
        this.pushed = pushed;
        arrayAllocationSite = new ArrayAllocationSite.UninitializedArrayAllocationSite(context);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        notDesignedForCompilation();

        final RubyArray originalArray;

        try {
            originalArray = array.executeArray(frame);
        } catch (UnexpectedResultException e) {
            throw new UnsupportedOperationException();
        }

        final int length = originalArray.getSize() + 1;

        Object newStore = arrayAllocationSite.start(length);
        newStore = arrayAllocationSite.set(newStore, 0, originalArray.getStore(), originalArray.getSize());
        newStore = arrayAllocationSite.set(newStore, length - 1, pushed.execute(frame));
        return arrayAllocationSite.finish(newStore, length);
    }

}
