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

import com.oracle.truffle.api.SourceSection;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import org.jruby.truffle.nodes.RubyNode;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.core.RubyArray;

@NodeInfo(shortName = "array-tail")
@NodeChildren({@NodeChild(value = "array", type = RubyNode.class)})
public abstract class ArrayGetTailNode extends RubyNode {

    @Child protected ArrayAllocationSite arrayAllocationSite;

    final int index;

    public ArrayGetTailNode(RubyContext context, SourceSection sourceSection, int index) {
        super(context, sourceSection);
        arrayAllocationSite = new ArrayAllocationSite.UninitializedArrayAllocationSite(context);
        this.index = index;
    }

    public ArrayGetTailNode(ArrayGetTailNode prev) {
        super(prev);
        arrayAllocationSite = prev.arrayAllocationSite;
        index = prev.index;
    }

    @Specialization(guards = "isNull", order = 1)
    public RubyArray getTailNull(RubyArray array) {
        notDesignedForCompilation();

        return arrayAllocationSite.empty();
    }

    @Specialization(guards = "isIntegerFixnum", order = 2)
    public RubyArray getTailIntegerFixnum(RubyArray array) {
        notDesignedForCompilation();

        if (index >= array.getSize()) {
            return arrayAllocationSite.empty();
        } else {
            final int length = array.getSize() - index;
            Object store = arrayAllocationSite.start(length);
            store = arrayAllocationSite.setAll(store, 0, (int[]) array.getStore(), index, length);
            return arrayAllocationSite.finish(store, length);
        }
    }

    @Specialization(guards = "isLongFixnum", order = 3)
    public RubyArray getTailLongFixnum(RubyArray array) {
        notDesignedForCompilation();

        if (index >= array.getSize()) {
            return arrayAllocationSite.empty();
        } else {
            final int length = array.getSize() - index;
            Object store = arrayAllocationSite.start(length);
            store = arrayAllocationSite.setAll(store, 0, (long[]) array.getStore(), index, length);
            return arrayAllocationSite.finish(store, length);
        }
    }

    @Specialization(guards = "isFloat", order = 4)
    public RubyArray getTailFloat(RubyArray array) {
        notDesignedForCompilation();

        if (index >= array.getSize()) {
            return arrayAllocationSite.empty();
        } else {
            final int length = array.getSize() - index;
            Object store = arrayAllocationSite.start(length);
            store = arrayAllocationSite.setAll(store, 0, (double[]) array.getStore(), index, length);
            return arrayAllocationSite.finish(store, length);
        }
    }

    @Specialization(guards = "isObject", order = 5)
    public RubyArray getTailObject(RubyArray array) {
        notDesignedForCompilation();

        if (index >= array.getSize()) {
            return arrayAllocationSite.empty();
        } else {
            final int length = array.getSize() - index;
            Object store = arrayAllocationSite.start(length);
            store = arrayAllocationSite.setAll(store, 0, (Object[]) array.getStore(), index, length);
            return arrayAllocationSite.finish(store, length);
        }
    }

    // TODO(CS): copied and pasted from ArrayCoreMethodNode - need a way to use statics from other classes in the DSL

    protected boolean isNull(RubyArray array) {
        return array.getStore() == null;
    }

    protected boolean isIntegerFixnum(RubyArray array) {
        return array.getStore() instanceof int[];
    }

    protected boolean isLongFixnum(RubyArray array) {
        return array.getStore() instanceof long[];
    }

    protected boolean isFloat(RubyArray array) {
        return array.getStore() instanceof double[];
    }

    protected boolean isObject(RubyArray array) {
        return array.getStore() instanceof Object[];
    }


}
