/*
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.methods.arguments;

import java.util.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;
import org.jruby.truffle.nodes.*;
import org.jruby.truffle.nodes.core.ArrayAllocationSite;
import org.jruby.truffle.runtime.*;
import org.jruby.truffle.runtime.core.*;
import org.jruby.truffle.runtime.core.RubyArray;

/**
 * Read the rest of arguments after a certain point into an array.
 */
@NodeInfo(shortName = "read-rest-of-arguments")
public class ReadRestArgumentNode extends RubyNode {

    @Child protected ArrayAllocationSite arrayAllocationSite;

    private final int index;

    public ReadRestArgumentNode(RubyContext context, SourceSection sourceSection, int index) {
        super(context, sourceSection);
        arrayAllocationSite = new ArrayAllocationSite.UninitializedArrayAllocationSite(context);
        this.index = index;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        notDesignedForCompilation();

        if (RubyArguments.getUserArgumentsCount(frame.getArguments()) <= index) {
            return arrayAllocationSite.empty();
        } else if (index == 0) {
            final Object[] arguments = RubyArguments.extractUserArguments(frame.getArguments());
            Object store = arrayAllocationSite.start(arguments.length);
            store = arrayAllocationSite.setAll(store, 0, arguments, arguments.length);
            return arrayAllocationSite.finish(store, arguments.length);
        } else {
            final Object[] arguments = RubyArguments.extractUserArguments(frame.getArguments());
            final int length = arguments.length - index;
            Object store = arrayAllocationSite.start(length);
            store = arrayAllocationSite.setAll(store, 0, arguments, index, length);
            return arrayAllocationSite.finish(store, length);
        }
    }
}
