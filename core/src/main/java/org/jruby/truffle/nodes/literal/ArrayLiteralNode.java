/*
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.literal;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import org.jruby.truffle.nodes.*;
import org.jruby.truffle.nodes.core.ArrayAllocationSite;
import org.jruby.truffle.runtime.*;
import org.jruby.truffle.runtime.core.RubyArray;

public class ArrayLiteralNode extends RubyNode {

    @Children protected final RubyNode[] values;
    @Child protected ArrayAllocationSite arrayAllocationSite;

    public ArrayLiteralNode(RubyContext context, SourceSection sourceSection, RubyNode[] values) {
        super(context, sourceSection);
        this.values = values;
        arrayAllocationSite = new ArrayAllocationSite.UninitializedArrayAllocationSite(context);
    }

    @ExplodeLoop
    @Override
    public RubyArray executeArray(VirtualFrame frame) {
        Object store = arrayAllocationSite.start(values.length);

        for (int n = 0; n < values.length; n++) {
            try {
                if (arrayAllocationSite.evaluateElementsAs() == Integer.class) {
                    store = arrayAllocationSite.set(store, n, values[n].executeIntegerFixnum(frame));
                } else if (arrayAllocationSite.evaluateElementsAs() == Long.class) {
                    store = arrayAllocationSite.set(store, n, values[n].executeLongFixnum(frame));
                } else if (arrayAllocationSite.evaluateElementsAs() == Double.class) {
                    store = arrayAllocationSite.set(store, n, values[n].execute(frame));
                } else {
                    store = arrayAllocationSite.set(store, n, values[n].execute(frame));
                }
            } catch (UnexpectedResultException e) {
                store = arrayAllocationSite.set(store, n, e.getResult());
            }
        }

        return arrayAllocationSite.finish(store, values.length);
    }

    @ExplodeLoop
    @Override
    public void executeVoid(VirtualFrame frame) {
        for (int n = 0; n < values.length; n++) {
            values[n].executeVoid(frame);
        }
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return executeArray(frame);
    }

    @Override
    public Object isDefined(VirtualFrame frame) {
        return getContext().makeString("expression");
    }

    // TODO(CS): remove this - shouldn't be fiddling with nodes from the outside
    public RubyNode[] getValues() {
        return values;
    }

}
