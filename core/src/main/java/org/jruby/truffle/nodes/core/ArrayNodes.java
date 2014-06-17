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

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.SourceSection;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.oracle.truffle.api.utilities.BranchProfile;
import org.jruby.runtime.Visibility;
import org.jruby.truffle.nodes.RubyRootNode;
import org.jruby.truffle.nodes.call.DispatchHeadNode;
import org.jruby.truffle.runtime.*;
import org.jruby.truffle.runtime.control.BreakException;
import org.jruby.truffle.runtime.control.NextException;
import org.jruby.truffle.runtime.control.RedoException;
import org.jruby.truffle.runtime.core.*;
import org.jruby.truffle.runtime.core.RubyArray;
import org.jruby.truffle.runtime.core.RubyRange;
import org.jruby.truffle.runtime.methods.RubyMethod;
import org.jruby.truffle.runtime.util.ArrayUtils;
import org.jruby.util.Memo;

import java.util.Arrays;
import java.util.Comparator;

@CoreClass(name = "Array")
public abstract class ArrayNodes {

    @CoreMethod(names = "+", minArgs = 1, maxArgs = 1)
    public abstract static class AddNode extends ArrayCoreMethodNode {

        @Child protected ArrayAllocationSite arrayAllocationSite;

        public AddNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            arrayAllocationSite = new ArrayAllocationSite.UninitializedArrayAllocationSite(context);
        }

        public AddNode(AddNode prev) {
            super(prev);
            arrayAllocationSite = prev.arrayAllocationSite;
        }

        @Specialization
        public RubyArray addIntegerFixnum(RubyArray a, RubyArray b) {
            notDesignedForCompilation();

            final int combinedSize = a.getSize() + b.getSize();
            Object store = arrayAllocationSite.start(combinedSize);
            store = arrayAllocationSite.set(store, 0, a.getStore(), a.getSize());
            store = arrayAllocationSite.set(store, a.getSize(), b.getStore(), b.getSize());
            return arrayAllocationSite.finish(store, combinedSize);
        }

    }

    @CoreMethod(names = "-", minArgs = 1, maxArgs = 1)
    public abstract static class SubNode extends ArrayCoreMethodNode {

        @Child protected ArrayAllocationSite arrayAllocationSite;

        public SubNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            arrayAllocationSite = new ArrayAllocationSite.UninitializedArrayAllocationSite(context);
        }

        public SubNode(SubNode prev) {
            super(prev);
            arrayAllocationSite = prev.arrayAllocationSite;
        }

        @Specialization(guards = "areBothIntegerFixnum", order = 1)
        public RubyArray subIntegerFixnum(RubyArray a, RubyArray b) {
            notDesignedForCompilation();

            final int[] as = (int[]) a.getStore();
            final int[] bs = (int[]) b.getStore();

            final int[] sub = new int[a.getSize()];

            int i = 0;

            for (int n = 0; n < a.getSize(); n++) {
                if (!ArrayUtils.contains(bs, as[n])) {
                    sub[i] = as[n];
                    i++;
                }
            }

            Object store = arrayAllocationSite.start(i);
            store = arrayAllocationSite.setAll(store, 0, sub, i);
            return arrayAllocationSite.finish(store, i);
        }

        @Specialization(guards = "areBothLongFixnum", order = 2)
        public RubyArray subLongFixnum(RubyArray a, RubyArray b) {
            notDesignedForCompilation();

            final long[] as = (long[]) a.getStore();
            final long[] bs = (long[]) b.getStore();

            final long[] sub = new long[a.getSize()];

            int i = 0;

            for (int n = 0; n < a.getSize(); n++) {
                if (!ArrayUtils.contains(bs, as[n])) {
                    sub[i] = as[n];
                    i++;
                }
            }

            Object store = arrayAllocationSite.start(i);
            store = arrayAllocationSite.setAll(store, 0, sub, i);
            return arrayAllocationSite.finish(store, i);
        }

        @Specialization(guards = "areBothFloat", order = 3)
        public RubyArray subDouble(RubyArray a, RubyArray b) {
            notDesignedForCompilation();

            final double[] as = (double[]) a.getStore();
            final double[] bs = (double[]) b.getStore();

            final double[] sub = new double[a.getSize()];

            int i = 0;

            for (int n = 0; n < a.getSize(); n++) {
                if (!ArrayUtils.contains(bs, as[n])) {
                    sub[i] = as[n];
                    i++;
                }
            }

            Object store = arrayAllocationSite.start(i);
            store = arrayAllocationSite.setAll(store, 0, sub, i);
            return arrayAllocationSite.finish(store, i);
        }

        @Specialization(guards = "areBothObject", order = 4)
        public RubyArray subObject(RubyArray a, RubyArray b) {
            notDesignedForCompilation();

            final Object[] as = (Object[]) a.getStore();
            final Object[] bs = (Object[]) b.getStore();

            final Object[] sub = new Object[a.getSize()];

            int i = 0;

            for (int n = 0; n < a.getSize(); n++) {
                if (!ArrayUtils.contains(bs, as[n])) {
                    sub[i] = as[n];
                    i++;
                }
            }

            Object store = arrayAllocationSite.start(i);
            store = arrayAllocationSite.setAll(store, 0, sub, i);
            return arrayAllocationSite.finish(store, i);
        }

    }

    @CoreMethod(names = "*", minArgs = 1, maxArgs = 1, lowerFixnumParameters = 0)
    public abstract static class MulNode extends ArrayCoreMethodNode {

        @Child protected ArrayAllocationSite arrayAllocationSite;

        public MulNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            arrayAllocationSite = new ArrayAllocationSite.UninitializedArrayAllocationSite(context);
        }

        public MulNode(MulNode prev) {
            super(prev);
            arrayAllocationSite = prev.arrayAllocationSite;
        }

        @Specialization(guards = "isNull", order = 1)
        public RubyArray mulEmpty(RubyArray array, int count) {
            return arrayAllocationSite.empty();
        }

        @Specialization(guards = "isIntegerFixnum", order = 2)
        public RubyArray mulIntegerFixnum(RubyArray array, int count) {
            final int[] store = (int[]) array.getStore();
            final int length = array.getSize();

            final int newLength = length * count;
            Object newStore = arrayAllocationSite.start(newLength);

            for (int n = 0; n < count; n++) {
                arrayAllocationSite.setAll(newStore, n * length, store, length);
            }

            return arrayAllocationSite.finish(newStore, newLength);
        }

        @Specialization(guards = "isLongFixnum", order = 3)
        public RubyArray mulLongFixnum(RubyArray array, int count) {
            final long[] store = (long[]) array.getStore();
            final int length = array.getSize();

            final int newLength = length * count;
            Object newStore = arrayAllocationSite.start(newLength);

            for (int n = 0; n < count; n++) {
                arrayAllocationSite.setAll(newStore, n * length, store, length);
            }

            return arrayAllocationSite.finish(newStore, newLength);
        }

        @Specialization(guards = "isFloat", order = 4)
        public RubyArray mulFloat(RubyArray array, int count) {
            final double[] store = (double[]) array.getStore();
            final int length = array.getSize();

            final int newLength = length * count;
            Object newStore = arrayAllocationSite.start(newLength);

            for (int n = 0; n < count; n++) {
                arrayAllocationSite.setAll(newStore, n * length, store, length);
            }

            return arrayAllocationSite.finish(newStore, newLength);
        }

        @Specialization(guards = "isObject", order = 5)
        public RubyArray mulObject(RubyArray array, int count) {
            final Object[] store = (Object[]) array.getStore();
            final int length = array.getSize();

            final int newLength = length * count;
            Object newStore = arrayAllocationSite.start(newLength);

            for (int n = 0; n < count; n++) {
                arrayAllocationSite.setAll(newStore, n * length, store, length);
            }

            return arrayAllocationSite.finish(newStore, newLength);
        }

    }

    @CoreMethod(names = "|", minArgs = 1, maxArgs = 1)
    public abstract static class UnionNode extends ArrayCoreMethodNode {

        @Child protected ArrayAllocationSite arrayAllocationSite;

        public UnionNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            arrayAllocationSite = new ArrayAllocationSite.UninitializedArrayAllocationSite(context);
        }

        public UnionNode(UnionNode prev) {
            super(prev);
            arrayAllocationSite = prev.arrayAllocationSite;
        }

        @Specialization(guards = "areBothIntegerFixnum", order = 1)
        public RubyArray orIntegerFixnum(RubyArray a, RubyArray b) {
            notDesignedForCompilation();

            final int[] as = (int[]) a.getStore();
            final int[] bs = (int[]) b.getStore();

            final int[] or = Arrays.copyOf(as, a.getSize() + b.getSize());

            int i = a.getSize();

            for (int n = 0; n < b.getSize(); n++) {
                if (!ArrayUtils.contains(as, bs[n])) {
                    or[i] = bs[n];
                    i++;
                }
            }

            Object store = arrayAllocationSite.start(i);
            store = arrayAllocationSite.setAll(store, 0, or, i);
            return arrayAllocationSite.finish(store, i);
        }

        @Specialization(guards = "areBothLongFixnum", order = 2)
        public RubyArray orLongFixnum(RubyArray a, RubyArray b) {
            notDesignedForCompilation();

            final long[] as = (long[]) a.getStore();
            final long[] bs = (long[]) b.getStore();

            final long[] or = Arrays.copyOf(as, a.getSize() + b.getSize());

            int i = a.getSize();

            for (int n = 0; n < b.getSize(); n++) {
                if (!ArrayUtils.contains(as, bs[n])) {
                    or[i] = bs[n];
                    i++;
                }
            }

            Object store = arrayAllocationSite.start(i);
            store = arrayAllocationSite.setAll(store, 0, or, i);
            return arrayAllocationSite.finish(store, i);
        }

        @Specialization(guards = "areBothFloat", order = 3)
        public RubyArray orDouble(RubyArray a, RubyArray b) {
            notDesignedForCompilation();

            final double[] as = (double[]) a.getStore();
            final double[] bs = (double[]) b.getStore();

            final double[] or = Arrays.copyOf(as, a.getSize() + b.getSize());

            int i = a.getSize();

            for (int n = 0; n < b.getSize(); n++) {
                if (!ArrayUtils.contains(as, bs[n])) {
                    or[i] = bs[n];
                    i++;
                }
            }

            Object store = arrayAllocationSite.start(i);
            store = arrayAllocationSite.setAll(store, 0, or, i);
            return arrayAllocationSite.finish(store, i);
        }

        @Specialization(guards = "areBothObject", order = 4)
        public RubyArray orObject(RubyArray a, RubyArray b) {
            notDesignedForCompilation();

            final Object[] as = (Object[]) a.getStore();
            final Object[] bs = (Object[]) b.getStore();

            final Object[] or = Arrays.copyOf(as, a.getSize() + b.getSize());

            int i = a.getSize();

            for (int n = 0; n < b.getSize(); n++) {
                if (!ArrayUtils.contains(as, bs[n])) {
                    or[i] = bs[n];
                    i++;
                }
            }

            Object store = arrayAllocationSite.start(i);
            store = arrayAllocationSite.setAll(store, 0, or, i);
            return arrayAllocationSite.finish(store, i);
        }

    }

    @CoreMethod(names = "==", minArgs = 1, maxArgs = 1)
    public abstract static class EqualNode extends ArrayCoreMethodNode {

        @Child protected DispatchHeadNode equals;

        public EqualNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            equals = new DispatchHeadNode(context, "==", false, DispatchHeadNode.MissingBehavior.CALL_METHOD_MISSING);
        }

        public EqualNode(EqualNode prev) {
            super(prev);
            equals = prev.equals;
        }

        @Specialization(guards = "areBothIntegerFixnum", order = 1)
        public boolean equalIntegerFixnum(RubyArray a, RubyArray b) {
            notDesignedForCompilation();

            if (a == b) {
                return true;
            }

            if (a.getSize() != b.getSize()) {
                return false;
            }

            return Arrays.equals((int[]) a.getStore(), (int[]) b.getStore());
        }

        @Specialization(guards = "areBothLongFixnum", order = 2)
        public boolean equalLongFixnum(RubyArray a, RubyArray b) {
            notDesignedForCompilation();

            if (a == b) {
                return true;
            }

            if (a.getSize() != b.getSize()) {
                return false;
            }

            return Arrays.equals((long[]) a.getStore(), (long[]) b.getStore());
        }

        @Specialization(guards = "areBothFloat", order = 3)
        public boolean equalFloat(RubyArray a, RubyArray b) {
            notDesignedForCompilation();

            if (a == b) {
                return true;
            }

            if (a.getSize() != b.getSize()) {
                return false;
            }

            return Arrays.equals((double[]) a.getStore(), (double[]) b.getStore());
        }


        @Specialization(order = 5)
        public boolean equal(VirtualFrame frame, RubyArray a, RubyArray b) {
            notDesignedForCompilation();

            if (a == b) {
                return true;
            }

            if (a.getSize() != b.getSize()) {
                return false;
            }

            final Object[] as = a.slowToArray();
            final Object[] bs = b.slowToArray();

            for (int n = 0; n < a.getSize(); n++) {
                if (!(boolean)equals.dispatch(frame, as[n], null, bs[n])) {
                    return false;
                }
            }

            return true;
        }

    }

    @CoreMethod(names = {"[]", "at"}, minArgs = 1, maxArgs = 2, lowerFixnumParameters = {0, 1})
    public abstract static class IndexNode extends ArrayCoreMethodNode {

        @Child protected ArrayAllocationSite arrayAllocationSite;

        public IndexNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            arrayAllocationSite = new ArrayAllocationSite.UninitializedArrayAllocationSite(context);
        }

        public IndexNode(IndexNode prev) {
            super(prev);
            arrayAllocationSite = prev.arrayAllocationSite;
        }

        @Specialization(guards = "isNull", order = 1)
        public NilPlaceholder getNull(RubyArray array, int index, UndefinedPlaceholder undefined) {
            return NilPlaceholder.INSTANCE;
        }

        @Specialization(guards = "isIntegerFixnum", rewriteOn=UnexpectedResultException.class, order = 2)
        public int getIntegerFixnumInBounds(RubyArray array, int index, UndefinedPlaceholder undefined) throws UnexpectedResultException {
            int normalisedIndex = array.normaliseIndex(index);

            if (normalisedIndex < 0 || normalisedIndex >= array.getSize()) {
                throw new UnexpectedResultException(NilPlaceholder.INSTANCE);
            } else {
                return ((int[]) array.getStore())[normalisedIndex];
            }
        }

        @Specialization(guards = "isIntegerFixnum", order = 3)
        public Object getIntegerFixnum(RubyArray array, int index, UndefinedPlaceholder undefined) {
            int normalisedIndex = array.normaliseIndex(index);

            if (normalisedIndex < 0 || normalisedIndex >= array.getSize()) {
                return NilPlaceholder.INSTANCE;
            } else {
                return ((int[]) array.getStore())[normalisedIndex];
            }
        }

        @Specialization(guards = "isLongFixnum", rewriteOn=UnexpectedResultException.class, order = 4)
        public long getLongFixnumInBounds(RubyArray array, int index, UndefinedPlaceholder undefined) throws UnexpectedResultException {
            int normalisedIndex = array.normaliseIndex(index);

            if (normalisedIndex < 0 || normalisedIndex >= array.getSize()) {
                throw new UnexpectedResultException(NilPlaceholder.INSTANCE);
            } else {
                return ((long[]) array.getStore())[normalisedIndex];
            }
        }

        @Specialization(guards = "isLongFixnum", order = 5)
        public Object getLongFixnum(RubyArray array, int index, UndefinedPlaceholder undefined) {

            int normalisedIndex = array.normaliseIndex(index);

            if (normalisedIndex < 0 || normalisedIndex >= array.getSize()) {
                return NilPlaceholder.INSTANCE;
            } else {
                return ((long[]) array.getStore())[normalisedIndex];
            }
        }

        @Specialization(guards = "isFloat", rewriteOn=UnexpectedResultException.class, order = 6)
        public double getFloatInBounds(RubyArray array, int index, UndefinedPlaceholder undefined) throws UnexpectedResultException {
            int normalisedIndex = array.normaliseIndex(index);

            if (normalisedIndex < 0 || normalisedIndex >= array.getSize()) {
                throw new UnexpectedResultException(NilPlaceholder.INSTANCE);
            } else {
                return ((double[]) array.getStore())[normalisedIndex];
            }
        }

        @Specialization(guards = "isIntegerFixnum", order = 7)
        public Object getFloat(RubyArray array, int index, UndefinedPlaceholder undefined) {
            int normalisedIndex = array.normaliseIndex(index);

            if (normalisedIndex < 0 || normalisedIndex >= array.getSize()) {
                return NilPlaceholder.INSTANCE;
            } else {
                return ((int[]) array.getStore())[normalisedIndex];
            }
        }

        @Specialization(guards = "isObject", order = 8)
        public Object getObject(RubyArray array, int index, UndefinedPlaceholder undefined) {
            int normalisedIndex = array.normaliseIndex(index);

            if (normalisedIndex < 0 || normalisedIndex >= array.getSize()) {
                return NilPlaceholder.INSTANCE;
            } else {
                return ((Object[]) array.getStore())[normalisedIndex];
            }
        }

        @Specialization(guards = "isObject", order = 9)
        public Object getObject(RubyArray array, int index, int length) {
            notDesignedForCompilation();

            int normalisedIndex = array.normaliseIndex(index);

            if (normalisedIndex < 0 || normalisedIndex >= array.getSize()) {
                return NilPlaceholder.INSTANCE;
            } else {
                Object store = arrayAllocationSite.start(normalisedIndex + length);
                store = arrayAllocationSite.setAll(store, 0, (Object[]) array.getStore(), length);
                return arrayAllocationSite.finish(store, length);
            }
        }

        @Specialization(guards = "isObject", order = 10)
        public Object getObject(RubyArray array, RubyRange.IntegerFixnumRange range, UndefinedPlaceholder undefined) {
            notDesignedForCompilation();

            int normalisedIndex = array.normaliseIndex(range.getBegin());
            int length = array.normaliseExclusiveIndex(range.getExclusiveEnd()) - normalisedIndex;

            if (normalisedIndex < 0 || normalisedIndex >= array.getSize()) {
                return NilPlaceholder.INSTANCE;
            } else {
                Object store = arrayAllocationSite.start(normalisedIndex + length);
                store = arrayAllocationSite.setAll(store, 0, (Object[]) array.getStore(), length);
                return arrayAllocationSite.finish(store, length);
            }
        }

    }

    @CoreMethod(names = "[]=", minArgs = 2, maxArgs = 3, lowerFixnumParameters = 0)
    public abstract static class IndexSetNode extends ArrayCoreMethodNode {

        private final BranchProfile tooSmallBranch = new BranchProfile();
        private final BranchProfile pastEndBranch = new BranchProfile();
        private final BranchProfile appendBranch = new BranchProfile();
        private final BranchProfile beyondBranch = new BranchProfile();
        private final BranchProfile reallocateBranch = new BranchProfile();

        public IndexSetNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public IndexSetNode(IndexSetNode prev) {
            super(prev);
        }

        @Specialization(guards = "isNull", order = 1)
        public Object setNull(RubyArray array, int index, Object value, UndefinedPlaceholder unused) {
            notDesignedForCompilation();

            array.slowPush(value);
            return value;
        }

        @Specialization(guards = "isIntegerFixnum", order = 2)
        public int setIntegerFixnum(RubyArray array, int index, int value, UndefinedPlaceholder unused) {
            final int normalisedIndex = array.normaliseIndex(index);
            int[] store = (int[]) array.getStore();

            if (normalisedIndex < 0) {
                tooSmallBranch.enter();
                throw new UnsupportedOperationException();
            } else if (normalisedIndex >= array.getSize()) {
                pastEndBranch.enter();

                if (normalisedIndex == array.getSize()) {
                    appendBranch.enter();

                    if (normalisedIndex >= store.length) {
                        reallocateBranch.enter();
                        array.setStore(store = Arrays.copyOf(store, ArrayUtils.capacity(store.length, normalisedIndex + 1)), array.getSize());
                    }

                    store[normalisedIndex] = value;
                    array.setSize(array.getSize() + 1);
                } else if (normalisedIndex > array.getSize()) {
                    beyondBranch.enter();
                    throw new UnsupportedOperationException();
                }
            } else {
                store[normalisedIndex] = value;
            }

            return value;
        }

        @Specialization(guards = "isIntegerFixnum", order = 3)
        public long setLongInIntegerFixnum(RubyArray array, int index, long value, UndefinedPlaceholder unused) {
            final int normalisedIndex = array.normaliseIndex(index);

            long[] store = ArrayUtils.longCopyOf((int[]) array.getStore());
            array.setStore(store, array.getSize());

            if (normalisedIndex < 0) {
                tooSmallBranch.enter();
                throw new UnsupportedOperationException();
            } else if (normalisedIndex >= array.getSize()) {
                pastEndBranch.enter();

                if (normalisedIndex == array.getSize()) {
                    appendBranch.enter();

                    if (normalisedIndex >= store.length) {
                        reallocateBranch.enter();
                        array.setStore(store = Arrays.copyOf(store, ArrayUtils.capacity(store.length, normalisedIndex + 1)), array.getSize());
                    }

                    store[normalisedIndex] = value;
                    array.setSize(array.getSize() + 1);
                } else if (normalisedIndex > array.getSize()) {
                    beyondBranch.enter();
                    throw new UnsupportedOperationException();
                }
            } else {
                store[normalisedIndex] = value;
            }

            return value;
        }

        @Specialization(guards = "isLongFixnum", order = 4)
        public long setLongFixnum(RubyArray array, int index, long value, UndefinedPlaceholder unused) {
            System.err.println("setting " + index);

            final int normalisedIndex = array.normaliseIndex(index);
            long[] store = (long[]) array.getStore();

            if (normalisedIndex < 0) {
                tooSmallBranch.enter();
                throw new UnsupportedOperationException();
            } else if (normalisedIndex >= array.getSize()) {
                pastEndBranch.enter();

                if (normalisedIndex == array.getSize()) {
                    appendBranch.enter();

                    if (normalisedIndex >= store.length) {
                        reallocateBranch.enter();
                        array.setStore(store = Arrays.copyOf(store, ArrayUtils.capacity(store.length, normalisedIndex + 1)), array.getSize());
                    }

                    store[normalisedIndex] = value;
                    array.setSize(array.getSize() + 1);
                } else if (normalisedIndex > array.getSize()) {
                    beyondBranch.enter();
                    throw new UnsupportedOperationException();
                }
            } else {
                store[normalisedIndex] = value;
            }

            return value;
        }

        @Specialization(guards = "isFloat", order = 5)
        public double setFloat(RubyArray array, int index, double value, UndefinedPlaceholder unused) {
            final int normalisedIndex = array.normaliseIndex(index);
            double[] store = (double[]) array.getStore();

            if (normalisedIndex < 0) {
                tooSmallBranch.enter();
                throw new UnsupportedOperationException();
            } else if (normalisedIndex >= array.getSize()) {
                pastEndBranch.enter();

                if (normalisedIndex == array.getSize()) {
                    appendBranch.enter();

                    if (normalisedIndex >= store.length) {
                        reallocateBranch.enter();
                        array.setStore(store = Arrays.copyOf(store, ArrayUtils.capacity(store.length, normalisedIndex + 1)), array.getSize());
                    }

                    store[normalisedIndex] = value;
                    array.setSize(array.getSize() + 1);
                } else if (normalisedIndex > array.getSize()) {
                    beyondBranch.enter();
                    throw new UnsupportedOperationException();
                }
            } else {
                store[normalisedIndex] = value;
            }

            return value;
        }

        @Specialization(guards = "isObject", order = 6)
        public Object setObject(RubyArray array, int index, Object value, UndefinedPlaceholder unused) {
            final int normalisedIndex = array.normaliseIndex(index);
            Object[] store = (Object[]) array.getStore();

            if (normalisedIndex < 0) {
                tooSmallBranch.enter();
                throw new UnsupportedOperationException();
            } else if (normalisedIndex >= array.getSize()) {
                pastEndBranch.enter();

                if (normalisedIndex == array.getSize()) {
                    appendBranch.enter();

                    if (normalisedIndex >= store.length) {
                        reallocateBranch.enter();
                        array.setStore(store = Arrays.copyOf(store, ArrayUtils.capacity(store.length, normalisedIndex + 1)), array.getSize());
                    }

                    store[normalisedIndex] = value;
                    array.setSize(array.getSize() + 1);
                } else if (normalisedIndex > array.getSize()) {
                    beyondBranch.enter();
                    throw new UnsupportedOperationException();
                }
            } else {
                store[normalisedIndex] = value;
            }

            return value;
        }

        @Specialization(guards = "isIntegerFixnum", order = 7)
        public RubyArray setIntegerFixnumRange(RubyArray array, RubyRange.IntegerFixnumRange range, RubyArray other, UndefinedPlaceholder unused) {
            // TODO(CS): why can't this be a guard?
            if (other.getStore() instanceof int[]) {
                if (range.doesExcludeEnd()) {
                    CompilerDirectives.transferToInterpreter();
                    throw new UnsupportedOperationException();
                } else {
                    int normalisedBegin = array.normaliseIndex(range.getBegin());
                    int normalisedEnd = array.normaliseIndex(range.getEnd());

                    if (normalisedBegin == 0 && normalisedEnd == array.getSize() - 1) {
                        array.setStore(Arrays.copyOf((int[]) other.getStore(), other.getSize()), other.getSize());
                    } else {
                        panic();
                        throw new RuntimeException();
                    }
                }
            } else {
                panic(other.getStore());
                throw new RuntimeException();
            }

            return other;
        }

        @Specialization(order = 8)
        public Object setUnexpected(RubyArray array, int index, Object value, UndefinedPlaceholder unused) {
            notDesignedForCompilation();

            if (array.getStore() instanceof int[] && value instanceof Integer) {
                return setIntegerFixnum(array, index, (int) value, unused);
            }

            // Just convert to object for now

            if (!(array.getStore() instanceof Object[])) {
                array.setStore(array.slowToArray(), array.getSize());
            }

            final int normalisedIndex = array.normaliseIndex(index);
            Object[] store = (Object[]) array.getStore();

            if (normalisedIndex < 0) {
                tooSmallBranch.enter();
                throw new UnsupportedOperationException();
            } else if (normalisedIndex >= array.getSize()) {
                pastEndBranch.enter();

                if (normalisedIndex == array.getSize()) {
                    appendBranch.enter();

                    if (normalisedIndex >= store.length) {
                        reallocateBranch.enter();
                        array.setStore(store = Arrays.copyOf(store, ArrayUtils.capacity(store.length, normalisedIndex + 1)), array.getSize());
                    }

                    store[normalisedIndex] = value;
                    array.setSize(array.getSize() + 1);
                } else if (normalisedIndex > array.getSize()) {
                    beyondBranch.enter();
                    throw new UnsupportedOperationException();
                }
            } else {
                store[normalisedIndex] = value;
            }

            return value;
        }

    }

    @CoreMethod(names = "all?", needsBlock = true, maxArgs = 0)
    public abstract static class AllNode extends YieldingArrayCoreMethodNode {

        public AllNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public AllNode(AllNode prev) {
            super(prev);
        }

        @Specialization(guards = "isNull", order = 1)
        public boolean allNull(VirtualFrame frame, RubyArray array, RubyProc block) {
            return true;
        }

        @Specialization(guards = "isIntegerFixnum", order = 2)
        public boolean allIntegerFixnum(VirtualFrame frame, RubyArray array, RubyProc block) {
            notDesignedForCompilation();

            for (int n = 0; n < array.getSize(); n++) {
                if (!yieldBoolean(frame, block, ((int[]) array.getStore())[n])) {
                    return false;
                }
            }

            return true;
        }

        @Specialization(guards = "isLongFixnum", order = 3)
        public boolean allLongFixnum(VirtualFrame frame, RubyArray array, RubyProc block) {
            notDesignedForCompilation();

            for (int n = 0; n < array.getSize(); n++) {
                if (!yieldBoolean(frame, block, ((long[]) array.getStore())[n])) {
                    return false;
                }
            }

            return true;
        }

        @Specialization(guards = "isFloat", order = 4)
        public boolean allFloat(VirtualFrame frame, RubyArray array, RubyProc block) {
            notDesignedForCompilation();

            for (int n = 0; n < array.getSize(); n++) {
                if (!yieldBoolean(frame, block, ((double[]) array.getStore())[n])) {
                    return false;
                }
            }

            return true;
        }

        @Specialization(guards = "isObject", order = 5)
        public boolean allObject(VirtualFrame frame, RubyArray array, RubyProc block) {
            notDesignedForCompilation();

            for (int n = 0; n < array.getSize(); n++) {
                if (!yieldBoolean(frame, block, ((Object[]) array.getStore())[n])) {
                    return false;
                }
            }

            return true;
        }

    }

    @CoreMethod(names = "any?", needsBlock = true, maxArgs = 0)
    public abstract static class AnyNode extends YieldingArrayCoreMethodNode {

        public AnyNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public AnyNode(AnyNode prev) {
            super(prev);
        }

        @Specialization(guards = "isNull", order = 1)
        public boolean anyNull(VirtualFrame frame, RubyArray array, RubyProc block) {
            return false;
        }

        @Specialization(guards = "isIntegerFixnum", order = 2)
        public boolean allIntegerFixnum(VirtualFrame frame, RubyArray array, RubyProc block) {
            notDesignedForCompilation();

            for (int n = 0; n < array.getSize(); n++) {
                if (yieldBoolean(frame, block, ((int[]) array.getStore())[n])) {
                    return true;
                }
            }

            return false;
        }

        @Specialization(guards = "isLongFixnum", order = 3)
        public boolean anyLongFixnum(VirtualFrame frame, RubyArray array, RubyProc block) {
            notDesignedForCompilation();

            for (int n = 0; n < array.getSize(); n++) {
                if (yieldBoolean(frame, block, ((long[]) array.getStore())[n])) {
                    return true;
                }
            }

            return false;
        }

        @Specialization(guards = "isFloat", order = 4)
        public boolean anyFloat(VirtualFrame frame, RubyArray array, RubyProc block) {
            notDesignedForCompilation();

            for (int n = 0; n < array.getSize(); n++) {
                if (yieldBoolean(frame, block, ((double[]) array.getStore())[n])) {
                    return true;
                }
            }

            return false;
        }

        @Specialization(guards = "isObject", order = 5)
        public boolean anyObject(VirtualFrame frame, RubyArray array, RubyProc block) {
            notDesignedForCompilation();

            for (int n = 0; n < array.getSize(); n++) {
                if (yieldBoolean(frame, block, ((Object[]) array.getStore())[n])) {
                    return true;
                }
            }

            return false;
        }

    }

    @CoreMethod(names = "clear", maxArgs = 0)
    public abstract static class ClearNode extends ArrayCoreMethodNode {

        public ClearNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public ClearNode(ClearNode prev) {
            super(prev);
        }

        @Specialization
        public RubyArray clear(RubyArray array) {
            notDesignedForCompilation();

            array.setSize(0);
            return array;
        }

    }

    @CoreMethod(names = "compact", maxArgs = 0)
    public abstract static class CompactNode extends ArrayCoreMethodNode {

        public CompactNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public CompactNode(CompactNode prev) {
            super(prev);
        }

        @Specialization(guards = "!isObject", order = 1)
        public RubyArray compatNotObjects(RubyArray array) {
            return array;
        }

        @Specialization(guards = "isObject", order = 2)
        public RubyArray compatObjects(RubyArray array) {
            notDesignedForCompilation();

            final Object[] compacted = new Object[array.getSize()];
            int compactedSize = 0;

            for (Object object : array.slowToArray()) {
                if (object != NilPlaceholder.INSTANCE) {
                    compacted[compactedSize] = object;
                    compactedSize++;
                }
            }

            array.setStore(compacted, compactedSize);

            return array;
        }

    }

    @CoreMethod(names = "concat", minArgs = 1, maxArgs = 1)
    public abstract static class ConcatNode extends ArrayCoreMethodNode {

        public ConcatNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public ConcatNode(ConcatNode prev) {
            super(prev);
        }

        @Specialization(guards = "areBothNull", order = 1)
        public RubyArray concatNull(RubyArray array, RubyArray other) {
            return array;
        }

        @Specialization(guards = "areBothIntegerFixnum", order = 2)
        public RubyArray concatIntegerFixnum(RubyArray array, RubyArray other) {
            notDesignedForCompilation();

            // TODO(CS): is there already space in array?
            array.setStore(Arrays.copyOf((int[]) array.getStore(), array.getSize() + other.getSize()), array.getSize());
            System.arraycopy(other.getStore(), 0, array.getStore(), array.getSize(), other.getSize());
            array.setSize(array.getSize() + other.getSize());
            return array;
        }

        @Specialization(guards = "areBothLongFixnum", order = 3)
        public RubyArray concatLongFixnum(RubyArray array, RubyArray other) {
            notDesignedForCompilation();

            // TODO(CS): is there already space in array?
            array.setStore(Arrays.copyOf((long[]) array.getStore(), array.getSize() + other.getSize()), array.getSize());
            System.arraycopy(other.getStore(), 0, array.getStore(), array.getSize(), other.getSize());
            array.setSize(array.getSize() + other.getSize());
            return array;
        }

        @Specialization(guards = "areBothFloat", order = 4)
        public RubyArray concatDouble(RubyArray array, RubyArray other) {
            notDesignedForCompilation();

            // TODO(CS): is there already space in array?
            array.setStore(Arrays.copyOf((double[]) array.getStore(), array.getSize() + other.getSize()), array.getSize());
            System.arraycopy(other.getStore(), 0, array.getStore(), array.getSize(), other.getSize());
            array.setSize(array.getSize() + other.getSize());
            return array;
        }

        @Specialization(guards = "areBothObject", order = 5)
        public RubyArray concatObject(RubyArray array, RubyArray other) {
            notDesignedForCompilation();

            // TODO(CS): is there already space in array?
            array.setStore(Arrays.copyOf((Object[]) array.getStore(), array.getSize() + other.getSize()), array.getSize());
            System.arraycopy(other.getStore(), 0, array.getStore(), array.getSize(), other.getSize());
            array.setSize(array.getSize() + other.getSize());
            return array;
        }

        @Specialization(order = 6)
        public RubyArray concat(RubyArray array, RubyArray other) {
            notDesignedForCompilation();

            // TODO(CS): is there already space in array?
            // TODO(CS): if array is Object[], use Arrays.copyOf
            final Object[] newStore = new Object[array.getSize() + other.getSize()];
            ArrayUtils.copy(array.getStore(), newStore, 0, array.getSize());
            ArrayUtils.copy(other.getStore(), newStore, array.getSize(), other.getSize());
            array.setStore(newStore, array.getSize() + other.getSize());
            return array;
        }

    }

    @CoreMethod(names = "delete", minArgs = 1, maxArgs = 1)
    public abstract static class DeleteNode extends ArrayCoreMethodNode {

        @Child protected DispatchHeadNode threeEqual;

        public DeleteNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            threeEqual = new DispatchHeadNode(context, "===", false, DispatchHeadNode.MissingBehavior.CALL_METHOD_MISSING);
        }

        public DeleteNode(DeleteNode prev) {
            super(prev);
            threeEqual = prev.threeEqual;
        }

        @Specialization(guards = "isIntegerFixnum", order = 1)
        public Object deleteIntegerFixnum(VirtualFrame frame, RubyArray array, Object value) {
            final int[] store = (int[]) array.getStore();

            Object found = NilPlaceholder.INSTANCE;

            int i = 0;

            for (int n = 0; n < array.getSize(); n++) {
                final Object stored = store[n];

                // TODO(CS): need a cast node around the dispatch

                if (stored == value || (boolean) threeEqual.dispatch(frame, store[n], null, value)) {
                    found = store[n];
                    continue;
                }

                if (i != n) {
                    store[i] = store[n];
                }

                i++;
            }

            array.setSize(i);
            return found;
        }

        @Specialization(guards = "isObject", order = 2)
        public Object deleteObject(VirtualFrame frame, RubyArray array, Object value) {
            final Object[] store = (Object[]) array.getStore();

            Object found = NilPlaceholder.INSTANCE;

            int i = 0;

            for (int n = 0; n < array.getSize(); n++) {
                final Object stored = store[n];

                // TODO(CS): need a cast node around the dispatch

                if (stored == value || (boolean) threeEqual.dispatch(frame, store[n], null, value)) {
                    found = store[n];
                    continue;
                }

                if (i != n) {
                    store[i] = store[n];
                }

                i++;
            }

            array.setSize(i);
            return found;
        }

    }

    @CoreMethod(names = "delete_at", minArgs = 1, maxArgs = 1)
    public abstract static class DeleteAtNode extends ArrayCoreMethodNode {

        private static final BranchProfile tooSmallBranch = new BranchProfile();
        private static final BranchProfile beyondEndBranch = new BranchProfile();

        public DeleteAtNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public DeleteAtNode(DeleteAtNode prev) {
            super(prev);
        }

        @Specialization(guards = "isIntegerFixnum", rewriteOn = UnexpectedResultException.class, order = 1)
        public int deleteAtIntegerFixnumInBounds(RubyArray array, int index) throws UnexpectedResultException {
            final int normalisedIndex = array.normaliseIndex(index);

            if (normalisedIndex < 0) {
                throw new UnexpectedResultException(NilPlaceholder.INSTANCE);
            } else if (normalisedIndex >= array.getSize()) {
                throw new UnexpectedResultException(NilPlaceholder.INSTANCE);
            } else {
                final int[] store = (int[]) array.getStore();
                final int value = store[normalisedIndex];
                System.arraycopy(store, normalisedIndex + 1, store, normalisedIndex, array.getSize() - normalisedIndex - 1);
                array.setSize(array.getSize() - 1);
                return value;
            }
        }

        @Specialization(guards = "isIntegerFixnum", order = 2)
        public Object deleteAtIntegerFixnum(RubyArray array, int index) {
            notDesignedForCompilation();

            int normalisedIndex = index;

            if (normalisedIndex < 0) {
                normalisedIndex = array.getSize() + index;
            }

            if (normalisedIndex < 0) {
                tooSmallBranch.enter();
                CompilerDirectives.transferToInterpreter();
                throw new UnsupportedOperationException();
            } else if (normalisedIndex >= array.getSize()) {
                beyondEndBranch.enter();
                throw new UnsupportedOperationException();
            } else {
                final int[] store = (int[]) array.getStore();
                final int value = store[normalisedIndex];
                System.arraycopy(store, normalisedIndex + 1, store, normalisedIndex, array.getSize() - normalisedIndex - 1);
                array.setSize(array.getSize() - 1);
                return value;
            }
        }

    }

    @CoreMethod(names = {"dup", "clone"}, maxArgs = 0)
    public abstract static class DupNode extends ArrayCoreMethodNode {

        @Child protected ArrayAllocationSite arrayAllocationSite;

        public DupNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            arrayAllocationSite = new ArrayAllocationSite.UninitializedArrayAllocationSite(context);
        }

        public DupNode(DupNode prev) {
            super(prev);
            arrayAllocationSite = prev.arrayAllocationSite;
        }

        @Specialization(guards = "isNull", order = 1)
        public Object dupNull(RubyArray array) {
            return arrayAllocationSite.empty();
        }

        @Specialization(guards = "isIntegerFixnum", order = 2)
        public Object dupIntegerFixnum(RubyArray array) {
            notDesignedForCompilation();

            final int size = array.getSize();
            Object store = arrayAllocationSite.start(size);
            arrayAllocationSite.setAll(store, 0, (int[]) array.getStore(), size);
            return arrayAllocationSite.finish(store, size);
        }

        @Specialization(guards = "isLongFixnum", order = 3)
        public Object dupLongFixnum(RubyArray array) {
            notDesignedForCompilation();

            final int size = array.getSize();
            Object store = arrayAllocationSite.start(size);
            arrayAllocationSite.setAll(store, 0, (long[]) array.getStore(), size);
            return arrayAllocationSite.finish(store, size);
        }

        @Specialization(guards = "isFloat", order = 4)
        public Object dupFloat(RubyArray array) {
            notDesignedForCompilation();

            final int size = array.getSize();
            Object store = arrayAllocationSite.start(size);
            arrayAllocationSite.setAll(store, 0, (double[]) array.getStore(), size);
            return arrayAllocationSite.finish(store, size);
        }

        @Specialization(guards = "isObject", order = 5)
        public Object dupObject(RubyArray array) {
            notDesignedForCompilation();

            final int size = array.getSize();
            Object store = arrayAllocationSite.start(size);
            arrayAllocationSite.setAll(store, 0, (Object[]) array.getStore(), size);
            return arrayAllocationSite.finish(store, size);
        }

    }

    @CoreMethod(names = "each", needsBlock = true, maxArgs = 0)
    public abstract static class EachNode extends YieldingArrayCoreMethodNode {

        private final BranchProfile breakProfile = new BranchProfile();
        private final BranchProfile nextProfile = new BranchProfile();
        private final BranchProfile redoProfile = new BranchProfile();

        public EachNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public EachNode(EachNode prev) {
            super(prev);
        }

        @Specialization(guards = "isNull", order = 1)
        public Object eachNull(VirtualFrame frame, RubyArray array, RubyProc block) {
            return NilPlaceholder.INSTANCE;
        }

        @Specialization(guards = "isIntegerFixnum", order = 2)
        public Object eachIntegerFixnum(VirtualFrame frame, RubyArray array, RubyProc block) {
            final int[] store = (int[]) array.getStore();

            int count = 0;

            try {
                outer:
                for (int n = 0; n < array.getSize(); n++) {
                    while (true) {
                        if (CompilerDirectives.inInterpreter()) {
                            count++;
                        }

                        try {
                            yield(frame, block, store[n]);
                            continue outer;
                        } catch (BreakException e) {
                            breakProfile.enter();
                            return e.getResult();
                        } catch (NextException e) {
                            nextProfile.enter();
                            continue outer;
                        } catch (RedoException e) {
                            redoProfile.enter();
                        }
                    }
                }
            } finally {
                if (CompilerDirectives.inInterpreter()) {
                    ((RubyRootNode) getRootNode()).reportLoopCountThroughBlocks(count);
                }
            }

            return array;
        }

        @Specialization(guards = "isLongFixnum", order = 3)
        public Object eachLongFixnum(VirtualFrame frame, RubyArray array, RubyProc block) {
            final long[] store = (long[]) array.getStore();

            int count = 0;

            try {
                outer:
                for (int n = 0; n < array.getSize(); n++) {
                    while (true) {
                        if (CompilerDirectives.inInterpreter()) {
                            count++;
                        }

                        try {
                            yield(frame, block, store[n]);
                            continue outer;
                        } catch (BreakException e) {
                            breakProfile.enter();
                            return e.getResult();
                        } catch (NextException e) {
                            nextProfile.enter();
                            continue outer;
                        } catch (RedoException e) {
                            redoProfile.enter();
                        }
                    }
                }
            } finally {
                if (CompilerDirectives.inInterpreter()) {
                    ((RubyRootNode) getRootNode()).reportLoopCountThroughBlocks(count);
                }
            }

            return array;
        }

        @Specialization(guards = "isFloat", order = 4)
        public Object eachFloat(VirtualFrame frame, RubyArray array, RubyProc block) {
            final double[] store = (double[]) array.getStore();

            int count = 0;

            try {
                outer:
                for (int n = 0; n < array.getSize(); n++) {
                    while (true) {
                        if (CompilerDirectives.inInterpreter()) {
                            count++;
                        }

                        try {
                            yield(frame, block, store[n]);
                            continue outer;
                        } catch (BreakException e) {
                            breakProfile.enter();
                            return e.getResult();
                        } catch (NextException e) {
                            nextProfile.enter();
                            continue outer;
                        } catch (RedoException e) {
                            redoProfile.enter();
                        }
                    }
                }
            } finally {
                if (CompilerDirectives.inInterpreter()) {
                    ((RubyRootNode) getRootNode()).reportLoopCountThroughBlocks(count);
                }
            }

            return array;
        }

        @Specialization(guards = "isObject", order = 5)
        public Object eachObject(VirtualFrame frame, RubyArray array, RubyProc block) {
            final Object[] store = (Object[]) array.getStore();

            int count = 0;

            try {
                outer:
                for (int n = 0; n < array.getSize(); n++) {
                    while (true) {
                        if (CompilerDirectives.inInterpreter()) {
                            count++;
                        }

                        try {
                            yield(frame, block, store[n]);
                            continue outer;
                        } catch (BreakException e) {
                            breakProfile.enter();
                            return e.getResult();
                        } catch (NextException e) {
                            nextProfile.enter();
                            continue outer;
                        } catch (RedoException e) {
                            redoProfile.enter();
                        }
                    }
                }
            } finally {
                if (CompilerDirectives.inInterpreter()) {
                    ((RubyRootNode) getRootNode()).reportLoopCountThroughBlocks(count);
                }
            }

            return array;
        }

    }

    @CoreMethod(names = "each_with_index", needsBlock = true, maxArgs = 0)
    public abstract static class EachWithIndexNode extends YieldingCoreMethodNode {

        private final BranchProfile breakProfile = new BranchProfile();
        private final BranchProfile nextProfile = new BranchProfile();
        private final BranchProfile redoProfile = new BranchProfile();

        public EachWithIndexNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public EachWithIndexNode(EachWithIndexNode prev) {
            super(prev);
        }

        @Specialization
        public Object eachWithIndex(VirtualFrame frame, RubyArray array, RubyProc block) {
            notDesignedForCompilation();

            final Object[] store = (Object[]) array.getStore();

            int count = 0;

            try {
                outer:
                for (int n = 0; n < array.getSize(); n++) {
                    while (true) {
                        if (CompilerDirectives.inInterpreter()) {
                            count++;
                        }

                        try {
                            yield(frame, block, store[n], n);
                            continue outer;
                        } catch (BreakException e) {
                            breakProfile.enter();
                            return e.getResult();
                        } catch (NextException e) {
                            nextProfile.enter();
                            continue outer;
                        } catch (RedoException e) {
                            redoProfile.enter();
                        }
                    }
                }
            } finally {
                if (CompilerDirectives.inInterpreter()) {
                    ((RubyRootNode) getRootNode()).reportLoopCountThroughBlocks(count);
                }
            }

            return array;
        }

    }

    @CoreMethod(names = "empty?", maxArgs = 0)
    public abstract static class EmptyNode extends ArrayCoreMethodNode {

        public EmptyNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public EmptyNode(EmptyNode prev) {
            super(prev);
        }

        @Specialization
        public boolean isEmpty(RubyArray array) {
            return array.getSize() == 0;
        }

    }

    @CoreMethod(names = "find", needsBlock = true, maxArgs = 0)
    public abstract static class FindNode extends YieldingArrayCoreMethodNode {

        public FindNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public FindNode(FindNode prev) {
            super(prev);
        }

        @Specialization(guards = "isNull", order = 1)
        public Object findNull(VirtualFrame frame, RubyArray array, RubyProc block) {
            return NilPlaceholder.INSTANCE;
        }

        @Specialization(guards = "isIntegerFixnum", order = 2)
        public Object findIntegerFixnum(VirtualFrame frame, RubyArray array, RubyProc block) {
            notDesignedForCompilation();

            final int[] store = (int[]) array.getStore();

            for (int n = 0; n < array.getSize(); n++) {
                try {
                    final Object value = store[n];

                    if (yieldBoolean(frame, block, value)) {
                        return value;
                    }
                } catch (BreakException e) {
                    break;
                }
            }

            return NilPlaceholder.INSTANCE;
        }

        @Specialization(guards = "isLongFixnum", order = 3)
        public Object findLongFixnum(VirtualFrame frame, RubyArray array, RubyProc block) {
            notDesignedForCompilation();

            final long[] store = (long[]) array.getStore();

            for (int n = 0; n < array.getSize(); n++) {
                try {
                    final Object value = store[n];

                    if (yieldBoolean(frame, block, value)) {
                        return value;
                    }
                } catch (BreakException e) {
                    break;
                }
            }

            return NilPlaceholder.INSTANCE;
        }

        @Specialization(guards = "isFloat", order = 4)
        public Object findFloat(VirtualFrame frame, RubyArray array, RubyProc block) {
            notDesignedForCompilation();

            final double[] store = (double[]) array.getStore();

            for (int n = 0; n < array.getSize(); n++) {
                try {
                    final Object value = store[n];

                    if (yieldBoolean(frame, block, value)) {
                        return value;
                    }
                } catch (BreakException e) {
                    break;
                }
            }

            return NilPlaceholder.INSTANCE;
        }

        @Specialization(guards = "isObject", order = 5)
        public Object findObject(VirtualFrame frame, RubyArray array, RubyProc block) {
            notDesignedForCompilation();

            final Object[] store = (Object[]) array.getStore();

            for (int n = 0; n < array.getSize(); n++) {
                try {
                    final Object value = store[n];

                    if (yieldBoolean(frame, block, value)) {
                        return value;
                    }
                } catch (BreakException e) {
                    break;
                }
            }

            return NilPlaceholder.INSTANCE;
        }
    }

    @CoreMethod(names = "first", maxArgs = 0)
    public abstract static class FirstNode extends ArrayCoreMethodNode {

        public FirstNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public FirstNode(FirstNode prev) {
            super(prev);
        }

        @Specialization(guards = "isNull", order = 1)
        public NilPlaceholder firstNull(RubyArray array) {
            notDesignedForCompilation();

            return NilPlaceholder.INSTANCE;
        }

        @Specialization(guards = "isIntegerFixnum", order = 2)
        public Object firstIntegerFixnum(RubyArray array) {
            notDesignedForCompilation();

            if (array.getSize() == 0) {
                return NilPlaceholder.INSTANCE;
            } else {
                return ((int[]) array.getStore())[0];
            }
        }

        @Specialization(guards = "isObject", order = 3)
        public Object firstObject(RubyArray array) {
            notDesignedForCompilation();

            if (array.getSize() == 0) {
                return NilPlaceholder.INSTANCE;
            } else {
                return ((Object[]) array.getStore())[0];
            }
        }

    }

    @CoreMethod(names = "flatten", maxArgs = 0)
    public abstract static class FlattenNode extends ArrayCoreMethodNode {

        public FlattenNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public FlattenNode(FlattenNode prev) {
            super(prev);
        }

        @Specialization
        public RubyArray flatten(RubyArray array) {
            throw new UnsupportedOperationException();
        }

    }

    @CoreMethod(names = "include?", minArgs = 1, maxArgs = 1)
    public abstract static class IncludeNode extends ArrayCoreMethodNode {

        @Child protected DispatchHeadNode threeEqual;

        public IncludeNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            threeEqual = new DispatchHeadNode(context, "===", false, DispatchHeadNode.MissingBehavior.CALL_METHOD_MISSING);
        }

        public IncludeNode(IncludeNode prev) {
            super(prev);
            threeEqual = prev.threeEqual;
        }

        @Specialization(guards = "isNull", order = 1)
        public boolean includeNull(VirtualFrame frame, RubyArray array, Object value) {
            return false;
        }

        @Specialization(guards = "isIntegerFixnum", order = 2)
        public boolean includeFixnum(VirtualFrame frame, RubyArray array, Object value) {
            final int[] store = (int[]) array.getStore();

            for (int n = 0; n < array.getSize(); n++) {
                final Object stored = store[n];

                // TODO(CS): cast node around the dispatch
                notDesignedForCompilation();

                if (stored == value || (boolean) threeEqual.dispatch(frame, store[n], null, value)) {
                    return true;
                }
            }

            return false;
        }

        @Specialization(guards = "isObject", order = 3)
        public boolean includeObject(VirtualFrame frame, RubyArray array, Object value) {
            final Object[] store = (Object[]) array.getStore();

            for (int n = 0; n < array.getSize(); n++) {
                final Object stored = store[n];

                // TODO(CS): cast node around the dispatch
                notDesignedForCompilation();

                if (stored == value || (boolean) threeEqual.dispatch(frame, store[n], null, value)) {
                    return true;
                }
            }

            return false;
        }

    }

    @CoreMethod(names = "initialize", needsBlock = true, minArgs = 1, maxArgs = 2)
    public abstract static class InitializeNode extends ArrayCoreMethodNode {

        public InitializeNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public InitializeNode(InitializeNode prev) {
            super(prev);
        }

        @Specialization
        public RubyArray initialize(RubyArray array, int size, UndefinedPlaceholder defaultValue) {
            return initialize(array, size, NilPlaceholder.INSTANCE);
        }

        @Specialization(guards = "areIntArraysEnabled")
        public RubyArray initialize(RubyArray array, int size, int defaultValue) {
            final int[] store = new int[size];
            Arrays.fill(store, defaultValue);
            array.setStore(store, size);
            return array;
        }

        @Specialization
        public RubyArray initialize(RubyArray array, int size, long defaultValue) {
            final long[] store = new long[size];
            Arrays.fill(store, defaultValue);
            array.setStore(store, size);
            return array;
        }

        @Specialization
        public RubyArray initialize(RubyArray array, int size, double defaultValue) {
            final double[] store = new double[size];
            Arrays.fill(store, defaultValue);
            array.setStore(store, size);
            return array;
        }

        @Specialization
        public RubyArray initialize(RubyArray array, int size, Object defaultValue) {
            final Object[] store = new Object[size];
            Arrays.fill(store, defaultValue);
            array.setStore(store, size);
            return array;
        }

    }

    @CoreMethod(names = {"inject", "reduce"}, needsBlock = true, minArgs = 0, maxArgs = 1)
    public abstract static class InjectNode extends YieldingArrayCoreMethodNode {

        public InjectNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public InjectNode(InjectNode prev) {
            super(prev);
        }

        @Specialization(guards = "isObject")
        public Object inject(VirtualFrame frame, RubyArray array, Object initial, RubyProc block) {
            int count = 0;

            final Object[] store = (Object[]) array.getStore();

            Object accumulator = initial;

            try {
                for (int n = 0; n < array.getSize(); n++) {
                    if (CompilerDirectives.inInterpreter()) {
                        count++;
                    }

                    accumulator = yield(frame, block, accumulator, store[n]);
                }
            } finally {
                if (CompilerDirectives.inInterpreter()) {
                    ((RubyRootNode) getRootNode()).reportLoopCountThroughBlocks(count);
                }
            }

            return accumulator;
        }

        @Specialization
        public Object inject(RubyArray array, RubySymbol symbol, UndefinedPlaceholder unused) {
            notDesignedForCompilation();

            final Object[] store = array.slowToArray();

            if (store.length < 2) {
                throw new UnsupportedOperationException();
            }

            Object accumulator = getContext().getCoreLibrary().box(store[0]).send(symbol.toString(), null, store[1]);

            for (int n = 2; n < array.getSize(); n++) {
                accumulator = getContext().getCoreLibrary().box(accumulator).send(symbol.toString(), null, store[n]);
            }

            return accumulator;
        }

    }

    @CoreMethod(names = "insert", minArgs = 2, maxArgs = 2)
    public abstract static class InsertNode extends ArrayCoreMethodNode {

        private static final BranchProfile tooSmallBranch = new BranchProfile();

        public InsertNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public InsertNode(InsertNode prev) {
            super(prev);
        }

        @Specialization(guards = "isNull")
        public Object insert(RubyArray array, int index, Object value) {
            notDesignedForCompilation();

            final Object[] store = new Object[index + 1];
            Arrays.fill(store, NilPlaceholder.INSTANCE);
            store[index] = value;
            array.setSize(array.getSize() + 1);
            return array;
        }

        @Specialization(guards = "isIntegerFixnum")
        public Object insert(RubyArray array, int index, int value) {
            final int normalisedIndex = array.normaliseIndex(index);
            final int[] store = (int[]) array.getStore();

            if (normalisedIndex < 0) {
                tooSmallBranch.enter();
                throw new UnsupportedOperationException();
            } else if (array.getSize() > store.length + 1) {
                CompilerDirectives.transferToInterpreter();
                throw new UnsupportedOperationException();
            } else {
                System.arraycopy(store, normalisedIndex, store, normalisedIndex + 1, array.getSize() - normalisedIndex);
                store[normalisedIndex] = value;
                array.setSize(array.getSize() + 1);
            }

            return array;
        }

    }

    @CoreMethod(names = {"inspect", "to_s"}, maxArgs = 0)
    public abstract static class InspectNode extends CoreMethodNode {

        @Child protected DispatchHeadNode inspect;

        public InspectNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            inspect = new DispatchHeadNode(context, "inspect", false, DispatchHeadNode.MissingBehavior.CALL_METHOD_MISSING);
        }

        public InspectNode(InspectNode prev) {
            super(prev);
            inspect = prev.inspect;
        }

        @Specialization
        public RubyString inspect(VirtualFrame frame, RubyArray array) {
            notDesignedForCompilation();

            final StringBuilder builder = new StringBuilder();
            final Object[] objects = array.slowToArray();

            builder.append("[");

            for (int n = 0; n < objects.length; n++) {
                if (n > 0) {
                    builder.append(", ");
                }

                // TODO(CS): to string

                builder.append(inspect.dispatch(frame, objects[n], null));
            }

            builder.append("]");

            return getContext().makeString(builder.toString());
        }

    }

    @CoreMethod(names = "join", minArgs = 1, maxArgs = 1)
    public abstract static class JoinNode extends ArrayCoreMethodNode {

        public JoinNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public JoinNode(JoinNode prev) {
            super(prev);
        }

        @Specialization
        public RubyString join(RubyArray array, RubyString separator) {
            notDesignedForCompilation();

            final StringBuilder builder = new StringBuilder();

            final Object[] objects = array.slowToArray();

            for (int n = 0; n < objects.length; n++) {
                if (n > 0) {
                    builder.append(separator);
                }

                builder.append(objects[n]);
            }

            return getContext().makeString(builder.toString());
        }

    }

    @CoreMethod(names = "last", maxArgs = 0)
    public abstract static class LastNode extends ArrayCoreMethodNode {

        public LastNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public LastNode(LastNode prev) {
            super(prev);
        }

        @Specialization
        public Object last(RubyArray array) {
            notDesignedForCompilation();

            if (array.getSize() == 0) {
                return NilPlaceholder.INSTANCE;
            } else {
                return array.slowToArray()[array.getSize() - 1];
            }
        }

    }

    @CoreMethod(names = {"map", "collect"}, needsBlock = true, maxArgs = 0)
    public abstract static class MapNode extends YieldingArrayCoreMethodNode {

        @Child protected ArrayAllocationSite arrayAllocationSite;

        public MapNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            arrayAllocationSite = new ArrayAllocationSite.UninitializedArrayAllocationSite(context);
        }

        public MapNode(MapNode prev) {
            super(prev);
            arrayAllocationSite = prev.arrayAllocationSite;
        }

        @Specialization(guards = "isNull", order = 1)
        public RubyArray mapNull(VirtualFrame frame, RubyArray array, RubyProc block) {
            return arrayAllocationSite.empty();
        }

        @Specialization(guards = "isIntegerFixnum", order = 2)
        public RubyArray mapIntegerFixnum(VirtualFrame frame, RubyArray array, RubyProc block) {
            final int[] store = (int[]) array.getStore();

            Object mappedStore = arrayAllocationSite.start(array.getSize());

            int count = 0;

            try {
                for (int n = 0; n < array.getSize(); n++) {
                    if (CompilerDirectives.inInterpreter()) {
                        count++;
                    }

                    mappedStore = arrayAllocationSite.set(mappedStore, n, yield(frame, block, store[n]));
                }
            } finally {
                if (CompilerDirectives.inInterpreter()) {
                    ((RubyRootNode) getRootNode()).reportLoopCountThroughBlocks(count);
                }
            }

            return arrayAllocationSite.finish(mappedStore, array.getSize());
        }

        @Specialization(guards = "isLongFixnum", order = 3)
        public RubyArray mapLongFixnum(VirtualFrame frame, RubyArray array, RubyProc block) {
            final long[] store = (long[]) array.getStore();

            Object mappedStore = arrayAllocationSite.start(array.getSize());

            int count = 0;

            try {
                for (int n = 0; n < array.getSize(); n++) {
                    if (CompilerDirectives.inInterpreter()) {
                        count++;
                    }

                    mappedStore = arrayAllocationSite.set(mappedStore, n, yield(frame, block, store[n]));
                }
            } finally {
                if (CompilerDirectives.inInterpreter()) {
                    ((RubyRootNode) getRootNode()).reportLoopCountThroughBlocks(count);
                }
            }

            return arrayAllocationSite.finish(mappedStore, array.getSize());
        }

        @Specialization(guards = "isObject", order = 4)
        public RubyArray mapObject(VirtualFrame frame, RubyArray array, RubyProc block) {
            final Object[] store = (Object[]) array.getStore();

            Object mappedStore = arrayAllocationSite.start(array.getSize());

            int count = 0;

            try {
                for (int n = 0; n < array.getSize(); n++) {
                    if (CompilerDirectives.inInterpreter()) {
                        count++;
                    }

                    mappedStore = arrayAllocationSite.set(mappedStore, n, yield(frame, block, store[n]));
                }
            } finally {
                if (CompilerDirectives.inInterpreter()) {
                    ((RubyRootNode) getRootNode()).reportLoopCountThroughBlocks(count);
                }
            }

            return arrayAllocationSite.finish(mappedStore, array.getSize());
        }
    }

    @CoreMethod(names = {"map!", "collect!"}, needsBlock = true, maxArgs = 0)
    public abstract static class MapInPlaceNode extends YieldingArrayCoreMethodNode {

        @Child protected ArrayAllocationSite arrayAllocationSite;

        public MapInPlaceNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            arrayAllocationSite = new ArrayAllocationSite.UninitializedArrayAllocationSite(context);
        }

        public MapInPlaceNode(MapInPlaceNode prev) {
            super(prev);
            arrayAllocationSite = prev.arrayAllocationSite;
        }

        @Specialization(guards = "isIntegerFixnum", order = 1)
        public RubyArray mapInPlaceFixnumInteger(VirtualFrame frame, RubyArray array, RubyProc block) {
            final int[] store = (int[]) array.getStore();
            Object mappedStore = arrayAllocationSite.start(array.getSize());

            int count = 0;

            try {
                for (int n = 0; n < array.getSize(); n++) {
                    if (CompilerDirectives.inInterpreter()) {
                        count++;
                    }

                    mappedStore = arrayAllocationSite.set(mappedStore, n, yield(frame, block, store[n]));
                }
            } finally {
                if (CompilerDirectives.inInterpreter()) {
                    ((RubyRootNode) getRootNode()).reportLoopCountThroughBlocks(count);
                }
            }

            array.setStore(arrayAllocationSite.finishStore(mappedStore), array.getSize());

            return array;
        }

        @Specialization(guards = "isObject", order = 2)
        public RubyArray mapInPlaceObject(VirtualFrame frame, RubyArray array, RubyProc block) {
            final Object[] store = (Object[]) array.getStore();
            Object mappedStore = arrayAllocationSite.start(array.getSize());

            int count = 0;

            try {
                for (int n = 0; n < array.getSize(); n++) {
                    if (CompilerDirectives.inInterpreter()) {
                        count++;
                    }

                    mappedStore = arrayAllocationSite.set(mappedStore, n, yield(frame, block, store[n]));
                }
            } finally {
                if (CompilerDirectives.inInterpreter()) {
                    ((RubyRootNode) getRootNode()).reportLoopCountThroughBlocks(count);
                }
            }

            array.setStore(arrayAllocationSite.finishStore(mappedStore), array.getSize());

            return array;
        }
    }

    // TODO: move into Enumerable?

    @CoreMethod(names = "min", maxArgs = 0)
    public abstract static class MinNode extends ArrayCoreMethodNode {

        @Child protected DispatchHeadNode eachNode;
        @Child protected DispatchHeadNode compareNode;

        public MinNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            eachNode = new DispatchHeadNode(context, "each", false, DispatchHeadNode.MissingBehavior.CALL_METHOD_MISSING);
            compareNode = new DispatchHeadNode(context, "<=>", false, DispatchHeadNode.MissingBehavior.CALL_METHOD_MISSING);
        }

        public MinNode(MinNode prev) {
            super(prev);
            eachNode = prev.eachNode;
            compareNode = prev.compareNode;
        }

        @Specialization
        public Object min(VirtualFrame frame, RubyArray array) {
            notDesignedForCompilation();

            // TODO(CS): will this be the right frame?
            final VirtualFrame finalFrame = frame;

            final Memo<Object> minimum = new Memo<>();

            final CallTarget callTarget = new CallTarget() {

                @Override
                public Object call(Object... arguments) {
                    final Object value = RubyArguments.getUserArgument(arguments, 0);

                    if (minimum.get() == null) {
                        minimum.set(value);
                    } else {
                        // TODO(CS): cast

                        if ((int) compareNode.dispatch(finalFrame, value, null, minimum.get()) < 0) {
                            minimum.set(value);
                        }
                    }

                    return NilPlaceholder.INSTANCE;
                }

            };

            final RubyProc compareProc = new RubyProc(getContext().getCoreLibrary().getProcClass(), RubyProc.Type.PROC, null, null,
                    new RubyMethod(null, null, null, Visibility.PRIVATE, false, callTarget, null, false));

            eachNode.dispatch(frame, array, compareProc);

            if (minimum.get() == null) {
                return NilPlaceholder.INSTANCE;
            } else {
                return minimum.get();
            }
        }

    }

    @CoreMethod(names = "pack", minArgs = 1, maxArgs = 1)
    public abstract static class PackNode extends ArrayCoreMethodNode {

        public PackNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public PackNode(PackNode prev) {
            super(prev);
        }

        @Specialization
        public RubyString pack(RubyArray array, RubyString format) {
            notDesignedForCompilation();

            return new RubyString(
                    getContext().getCoreLibrary().getStringClass(),
                    org.jruby.util.Pack.pack(
                            getContext().getRuntime(),
                            getContext().toJRuby(array),
                            getContext().toJRuby(format).getByteList()).getByteList());

        }

    }

    @CoreMethod(names = "pop", maxArgs = 0)
    public abstract static class PopNode extends ArrayCoreMethodNode {

        public PopNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public PopNode(PopNode prev) {
            super(prev);
        }

        @Specialization(guards = "isNull", order = 1)
        public Object popNil(RubyArray array) {
            return NilPlaceholder.INSTANCE;
        }

        @Specialization(guards = "isIntegerFixnum", rewriteOn = UnexpectedResultException.class, order = 2)
        public int popIntegerFixnumInBounds(RubyArray array) throws UnexpectedResultException {
            if (CompilerDirectives.injectBranchProbability(CompilerDirectives.UNLIKELY_PROBABILITY, array.getSize() == 0)) {
                throw new UnexpectedResultException(NilPlaceholder.INSTANCE);
            } else {
                final int value = ((int[]) array.getStore())[array.getSize() - 1];
                array.setSize(array.getSize() - 1);
                return value;
            }
        }

        @Specialization(guards = "isIntegerFixnum", rewriteOn = UnexpectedResultException.class, order = 3)
        public Object popIntegerFixnum(RubyArray array) throws UnexpectedResultException {
            if (CompilerDirectives.injectBranchProbability(CompilerDirectives.UNLIKELY_PROBABILITY, array.getSize() == 0)) {
                return NilPlaceholder.INSTANCE;
            } else {
                final int value = ((int[]) array.getStore())[array.getSize() - 1];
                array.setSize(array.getSize() - 1);
                return value;
            }
        }

        @Specialization(guards = "isLongFixnum", rewriteOn = UnexpectedResultException.class, order = 4)
        public long popLongFixnumInBounds(RubyArray array) throws UnexpectedResultException {
            if (CompilerDirectives.injectBranchProbability(CompilerDirectives.UNLIKELY_PROBABILITY, array.getSize() == 0)) {
                throw new UnexpectedResultException(NilPlaceholder.INSTANCE);
            } else {
                final long value = ((long[]) array.getStore())[array.getSize() - 1];
                array.setSize(array.getSize() - 1);
                return value;
            }
        }

        @Specialization(guards = "isLongFixnum", rewriteOn = UnexpectedResultException.class, order = 5)
        public Object popLongFixnum(RubyArray array) throws UnexpectedResultException {
            if (CompilerDirectives.injectBranchProbability(CompilerDirectives.UNLIKELY_PROBABILITY, array.getSize() == 0)) {
                return NilPlaceholder.INSTANCE;
            } else {
                final long value = ((long[]) array.getStore())[array.getSize() - 1];
                array.setSize(array.getSize() - 1);
                return value;
            }
        }

        @Specialization(guards = "isFloat", rewriteOn = UnexpectedResultException.class, order = 6)
        public double popFloatInBounds(RubyArray array) throws UnexpectedResultException {
            if (CompilerDirectives.injectBranchProbability(CompilerDirectives.UNLIKELY_PROBABILITY, array.getSize() == 0)) {
                throw new UnexpectedResultException(NilPlaceholder.INSTANCE);
            } else {
                final double value = ((double[]) array.getStore())[array.getSize() - 1];
                array.setSize(array.getSize() - 1);
                return value;
            }
        }

        @Specialization(guards = "isFloat", rewriteOn = UnexpectedResultException.class, order = 7)
        public Object popFloat(RubyArray array) throws UnexpectedResultException {
            if (CompilerDirectives.injectBranchProbability(CompilerDirectives.UNLIKELY_PROBABILITY, array.getSize() == 0)) {
                return NilPlaceholder.INSTANCE;
            } else {
                final double value = ((double[]) array.getStore())[array.getSize() - 1];
                array.setSize(array.getSize() - 1);
                return value;
            }
        }

        @Specialization(guards = "isObject", rewriteOn = UnexpectedResultException.class, order = 8)
        public Object popObject(RubyArray array) throws UnexpectedResultException {
            if (CompilerDirectives.injectBranchProbability(CompilerDirectives.UNLIKELY_PROBABILITY, array.getSize() == 0)) {
                return NilPlaceholder.INSTANCE;
            } else {
                final Object value = ((Object[]) array.getStore())[array.getSize() - 1];
                array.setSize(array.getSize() - 1);
                return value;
            }
        }

    }

    @CoreMethod(names = "product", minArgs = 1, maxArgs = 1)
    public abstract static class ProductNode extends ArrayCoreMethodNode {

        public ProductNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public ProductNode(ProductNode prev) {
            super(prev);
        }

        @Specialization(guards = {"isObject", "isOtherObject"})
        public Object product(RubyArray array, RubyArray other) {
            // TODO(CS): use an array allocation site

            final Object[] a = (Object[]) array.getStore();
            final int aLength = array.getSize();

            final Object[] b = (Object[]) other.getStore();
            final int bLength = other.getSize();

            final Object[] pairs = new Object[aLength * bLength];

            for (int an = 0; an < aLength; an++) {
                for (int bn = 0; bn < bLength; bn++) {
                    pairs[an * bLength + bn] = RubyArray.slowNewArray(getContext().getCoreLibrary().getArrayClass(), new Object[]{a[an], b[bn]}, 2);
                }
            }

            return RubyArray.slowNewArray(getContext().getCoreLibrary().getArrayClass(), pairs, pairs.length);
        }

    }

    @CoreMethod(names = {"push", "<<"}, isSplatted = true)
    public abstract static class PushNode extends ArrayCoreMethodNode {

        private final BranchProfile extendBranch = new BranchProfile();

        public PushNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public PushNode(PushNode prev) {
            super(prev);
        }

        @Specialization(guards = "isNull", order = 1)
        public RubyArray pushEmpty(RubyArray array, Object... values) {
            array.setStore(values, values.length);
            return array;
        }

        @Specialization(guards = "isIntegerFixnum", order = 2)
        public RubyArray pushIntegerFixnum(RubyArray array, Object... values) {
            boolean needToReType = false;

            for (int n = 0; n < values.length; n++) {
                final Object value = values[n];

                if (!(value instanceof Integer || (value instanceof Long && RubyFixnum.fitsIntoInteger((long) value)))) {
                    needToReType = true;
                }
            }

            final int start = array.getSize();
            final int newSize = array.getSize() + values.length;

            int[] store = (int[]) array.getStore();
            final boolean needToExtend = store.length < newSize;

            if (needToReType) {
                CompilerDirectives.transferToInterpreter();

                final Object[] retypedStore = ArrayUtils.box(store, ArrayUtils.capacity(store.length, newSize));

                for (int n = 0; n < values.length; n++) {
                    retypedStore[start + n] = values[n];
                }

                array.setStore(retypedStore, newSize);

                return array;
            }

            if (needToExtend) {
                extendBranch.enter();

                store = Arrays.copyOf(store, ArrayUtils.capacity(store.length, newSize));
                array.setStore(store, array.getSize());
            }

            for (int n = 0; n < values.length; n++) {
                final Object value = values[n];

                if (value instanceof Integer) {
                    store[start + n] = (int) values[n];
                } else if (value instanceof Long) {
                    store[start + n] = (int) (long) values[n];
                }
            }

            array.setSize(newSize);
            return array;
        }

        @Specialization(guards = "isLongFixnum", order = 3)
        public RubyArray pushLongFixnum(RubyArray array, Object... values) {
            boolean needToReType = false;

            for (int n = 0; n < values.length; n++) {
                final Object value = values[n];

                if (!(value instanceof Integer || value instanceof Long)) {
                    needToReType = true;
                }
            }

            final int start = array.getSize();
            final int newSize = array.getSize() + values.length;

            long[] store = (long[]) array.getStore();
            final boolean needToExtend = store.length < newSize;

            if (needToReType) {
                CompilerDirectives.transferToInterpreter();

                final Object[] retypedStore = ArrayUtils.box(store, ArrayUtils.capacity(store.length, newSize));

                for (int n = 0; n < values.length; n++) {
                    retypedStore[start + n] = values[n];
                }

                array.setStore(retypedStore, newSize);

                return array;
            }

            if (needToExtend) {
                extendBranch.enter();

                store = Arrays.copyOf(store, ArrayUtils.capacity(store.length, newSize));
                array.setStore(store, array.getSize());
            }

            for (int n = 0; n < values.length; n++) {
                final Object value = values[n];

                if (value instanceof Integer) {
                    store[start + n] = (int) values[n];
                } else if (value instanceof Long) {
                    store[start + n] = (long) values[n];
                }
            }

            array.setSize(newSize);
            return array;
        }

        @Specialization(guards = "isObject", order = 4)
        public RubyArray pushObject(RubyArray array, Object... values) {
            final int newSize = array.getSize() + values.length;

            Object[] store = (Object[]) array.getStore();

            if (store.length < newSize) {
                extendBranch.enter();
                array.setStore(store = Arrays.copyOf(store, ArrayUtils.capacity(store.length, newSize)), array.getSize());
            }

            int start = array.getSize();

            for (int n = 0; n < values.length; n++) {
                store[start + n] = values[n];
            }

            array.setSize(newSize);
            return array;
        }

    }

    @CoreMethod(names = "reject!", needsBlock = true, maxArgs = 0)
    public abstract static class RejectInPlaceNode extends YieldingArrayCoreMethodNode {

        public RejectInPlaceNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public RejectInPlaceNode(RejectInPlaceNode prev) {
            super(prev);
        }

        @Specialization(guards = "isIntegerFixnum", order = 0)
        public Object rejectInPlaceFixnumInteger(VirtualFrame frame, RubyArray array, RubyProc block) {
            final int[] store = (int[]) array.getStore();

            int i = 0;

            for (int n = 0; n < array.getSize(); n++) {
                if (yieldBoolean(frame, block, store[n])) {
                    continue;
                }

                if (i != n) {
                    store[i] = store[n];
                }

                i++;
            }

            array.setSize(i);
            return array;
        }

        @Specialization(guards = "isObject", order = 1)
        public Object rejectInPlaceObject(VirtualFrame frame, RubyArray array, RubyProc block) {
            final Object[] store = (Object[]) array.getStore();

            int i = 0;

            for (int n = 0; n < array.getSize(); n++) {
                if (yieldBoolean(frame, block, store[n])) {
                    continue;
                }

                if (i != n) {
                    store[i] = store[n];
                }

                i++;
            }

            array.setSize(i);
            return array;
        }

    }

    @CoreMethod(names = "replace", minArgs = 1, maxArgs = 1)
    public abstract static class ReplaceNode extends ArrayCoreMethodNode {

        public ReplaceNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public ReplaceNode(ReplaceNode prev) {
            super(prev);
        }

        @Specialization(guards = "isOtherNull", order = 1)
        public RubyArray replace(RubyArray array, RubyArray other) {
            notDesignedForCompilation();

            array.setSize(0);
            return array;
        }

        @Specialization(guards = "isOtherIntegerFixnum", order = 2)
        public RubyArray replaceIntegerFixnum(RubyArray array, RubyArray other) {
            notDesignedForCompilation();

            array.setStore(Arrays.copyOf((int[]) other.getStore(), other.getSize()), other.getSize());
            return array;
        }

        @Specialization(guards = "isOtherLongFixnum", order = 3)
        public RubyArray replaceLongFixnum(RubyArray array, RubyArray other) {
            notDesignedForCompilation();

            array.setStore(Arrays.copyOf((long[]) other.getStore(), other.getSize()), other.getSize());
            return array;
        }

        @Specialization(guards = "isOtherFloat", order = 4)
        public RubyArray replaceFloat(RubyArray array, RubyArray other) {
            notDesignedForCompilation();

            array.setStore(Arrays.copyOf((double[]) other.getStore(), other.getSize()), other.getSize());
            return array;
        }

        @Specialization(guards = "isOtherObject", order = 5)
        public RubyArray replaceObject(RubyArray array, RubyArray other) {
            notDesignedForCompilation();

            array.setStore(Arrays.copyOf((Object[]) other.getStore(), other.getSize()), other.getSize());
            return array;
        }

    }

    @CoreMethod(names = "select", needsBlock = true, maxArgs = 0)
    public abstract static class SelectNode extends YieldingArrayCoreMethodNode {

        @Child protected ArrayAllocationSite arrayAllocationSite;

        public SelectNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            arrayAllocationSite = new ArrayAllocationSite.UninitializedArrayAllocationSite(context);
        }

        public SelectNode(SelectNode prev) {
            super(prev);
            arrayAllocationSite = prev.arrayAllocationSite;
        }

        @Specialization(guards = "isNull", order = 1)
        public RubyArray selectNull(VirtualFrame frame, RubyArray array, RubyProc block) {
            return arrayAllocationSite.empty();
        }

        @Specialization(guards = "isObject", order = 2)
        public RubyArray selectObject(VirtualFrame frame, RubyArray array, RubyProc block) {
            final Object[] store = (Object[]) array.getStore();

            Object selectedStore = arrayAllocationSite.start(array.getSize());
            int selectedSize = 0;

            int count = 0;

            try {
                for (int n = 0; n < array.getSize(); n++) {
                    if (CompilerDirectives.inInterpreter()) {
                        count++;
                    }

                    final Object value = store[n];

                    // TODO(CS): cast to boolean?
                    notDesignedForCompilation();

                    assert RubyContext.shouldObjectBeVisible(value);
                    assert RubyContext.shouldObjectsBeVisible(new Object[]{value});
                    if (yieldBoolean(frame, block, new Object[]{value})) {
                        selectedStore = arrayAllocationSite.set(selectedStore, selectedSize, value);
                        selectedSize++;
                    }
                }
            } finally {
                if (CompilerDirectives.inInterpreter()) {
                    ((RubyRootNode) getRootNode()).reportLoopCountThroughBlocks(count);
                }
            }

            return arrayAllocationSite.finish(selectedStore, selectedSize);
        }

        @Specialization(guards = "isIntegerFixnum", order = 3)
        public RubyArray selectFixnumInteger(VirtualFrame frame, RubyArray array, RubyProc block) {
            final int[] store = (int[]) array.getStore();

            Object selectedStore = arrayAllocationSite.start(array.getSize());
            int selectedSize = 0;

            int count = 0;

            try {
                for (int n = 0; n < array.getSize(); n++) {
                    if (CompilerDirectives.inInterpreter()) {
                        count++;
                    }

                    final Object value = store[n];

                    // TODO(CS): cast to boolean?
                    notDesignedForCompilation();

                    if ((boolean) yield(frame, block, value)) {
                        selectedStore = arrayAllocationSite.set(selectedStore, selectedSize, value);
                        selectedSize++;
                    }
                }
            } finally {
                if (CompilerDirectives.inInterpreter()) {
                    ((RubyRootNode) getRootNode()).reportLoopCountThroughBlocks(count);
                }
            }

            return arrayAllocationSite.finish(selectedStore, selectedSize);
        }

    }

    @CoreMethod(names = "shift", maxArgs = 0)
    public abstract static class ShiftNode extends CoreMethodNode {

        public ShiftNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public ShiftNode(ShiftNode prev) {
            super(prev);
        }

        @Specialization
        public Object shift(RubyArray array) {
            notDesignedForCompilation();

            return array.slowShift();
        }

    }

    @CoreMethod(names = {"size", "length"}, maxArgs = 0)
    public abstract static class SizeNode extends ArrayCoreMethodNode {

        public SizeNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public SizeNode(SizeNode prev) {
            super(prev);
        }

        @Specialization
        public int size(RubyArray array) {
            return array.getSize();
        }

    }

    @CoreMethod(names = "slice", minArgs = 2, maxArgs = 2)
    public abstract static class SliceNode extends ArrayCoreMethodNode {

        @Child protected ArrayAllocationSite arrayAllocationSite;

        public SliceNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public SliceNode(SliceNode prev) {
            super(prev);
        }

        @Specialization(guards = "isIntegerFixnum")
        public RubyArray slice(RubyArray array, int start, int length) {
            notDesignedForCompilation();

            final int[] store = (int[]) array.getStore();

            final int normalisedStart = array.normaliseIndex(start);
            final int normalisedEnd = Math.min(normalisedStart + length, array.getSize() + length);
            final int sliceLength = normalisedEnd - normalisedStart;

            Object newStore = arrayAllocationSite.start(sliceLength);
            newStore = arrayAllocationSite.setAll(newStore, 0, Arrays.copyOfRange(store, normalisedStart, normalisedEnd), sliceLength);
            return arrayAllocationSite.finish(newStore, sliceLength);
        }

    }

    @CoreMethod(names = "sort", maxArgs = 0)
    public abstract static class SortNode extends ArrayCoreMethodNode {

        @Child protected DispatchHeadNode compareDispatchNode;
        @Child protected ArrayAllocationSite arrayAllocationSite;

        public SortNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            compareDispatchNode = new DispatchHeadNode(context, "<=>", false, DispatchHeadNode.MissingBehavior.CALL_METHOD_MISSING);
            arrayAllocationSite = new ArrayAllocationSite.UninitializedArrayAllocationSite(context);
        }

        public SortNode(SortNode prev) {
            super(prev);
            compareDispatchNode = prev.compareDispatchNode;
            arrayAllocationSite = prev.arrayAllocationSite;
        }

        @Specialization(guards = "isNull", order = 1)
        public RubyArray sortNull(RubyArray array) {
            return arrayAllocationSite.empty();
        }

        @Specialization(guards = {"isIntegerFixnum", "isVeryShort"}, order = 2)
        public RubyArray sortVeryShortIntegerFixnum(VirtualFrame frame, RubyArray array) {
            final int[] store = (int[]) array.getStore();
            final int[] sorted = Arrays.copyOf(store, array.getSize());

            // Insertion sort

            final int size = array.getSize();

            for (int i = 1; i < size; i++) {
                final int x = store[i];
                int j = i;
                // TODO(CS): node for this cast
                while (j > 0 && (int) compareDispatchNode.dispatch(frame, store[j - 1], null, x) > 0) {
                    store[j] = store[j - 1];
                    j--;
                }
                store[j] = x;
            }

            Object newStore = arrayAllocationSite.start(sorted.length);
            newStore = arrayAllocationSite.setAll(newStore, 0, sorted, sorted.length);
            return arrayAllocationSite.finish(newStore, sorted.length);
        }

        @Specialization(guards = "isIntegerFixnum", order = 3)
        public RubyArray sortIntegerFixnum(VirtualFrame frame, RubyArray array) {
            notDesignedForCompilation();

            final Object[] boxed = ArrayUtils.box((int[]) array.getStore());
            sort(frame, boxed);
            final int[] unboxed = ArrayUtils.unboxInteger(boxed);

            Object newStore = arrayAllocationSite.start(unboxed.length);
            newStore = arrayAllocationSite.setAll(newStore, 0, unboxed, unboxed.length);
            return arrayAllocationSite.finish(newStore, unboxed.length);
        }

        @Specialization(guards = "isLongFixnum", order = 4)
        public RubyArray sortLongFixnum(VirtualFrame frame, RubyArray array) {
            notDesignedForCompilation();

            final Object[] boxed = ArrayUtils.box((long[]) array.getStore());
            sort(frame, boxed);
            final long[] unboxed = ArrayUtils.unboxLong(boxed);

            Object newStore = arrayAllocationSite.start(unboxed.length);
            newStore = arrayAllocationSite.setAll(newStore, 0, unboxed, unboxed.length);
            return arrayAllocationSite.finish(newStore, unboxed.length);
        }

        @Specialization(guards = "isFloat", order = 5)
        public RubyArray sortDouble(VirtualFrame frame, RubyArray array) {
            notDesignedForCompilation();

            final Object[] boxed = ArrayUtils.box((double[]) array.getStore());
            sort(frame, boxed);
            final double[] unboxed = ArrayUtils.unboxDouble(boxed);

            Object newStore = arrayAllocationSite.start(unboxed.length);
            newStore = arrayAllocationSite.setAll(newStore, 0, unboxed, unboxed.length);
            return arrayAllocationSite.finish(newStore, unboxed.length);
        }

        @Specialization(guards = "isObject", order = 6)
        public RubyArray sortObject(VirtualFrame frame, RubyArray array) {
            notDesignedForCompilation();

            final Object[] sorted = Arrays.copyOf((Object[]) array.getStore(), array.getSize());
            sort(frame, sorted);

            Object newStore = arrayAllocationSite.start(sorted.length);
            newStore = arrayAllocationSite.setAll(newStore, 0, sorted, sorted.length);
            return arrayAllocationSite.finish(newStore, sorted.length);
        }

        private <T> void sort(VirtualFrame frame, T[] objects) {
            final VirtualFrame finalFrame = frame;

            Arrays.sort(objects, new Comparator<Object>() {

                @Override
                public int compare(Object a, Object b) {
                    // TODO(CS): node for this cast
                    return (int) compareDispatchNode.dispatch(finalFrame, a, null, b);
                }

            });
        }

        protected static boolean isVeryShort(RubyArray array) {
            return array.getSize() <= 3;
        }

    }

    @CoreMethod(names = "to_a", maxArgs = 0)
    public abstract static class ToANode extends CoreMethodNode {

        public ToANode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public ToANode(ToANode prev) {
            super(prev);
        }

        @Specialization
        public RubyArray toA(RubyArray array) {
            return array;
        }

    }

    @CoreMethod(names = "unshift", isSplatted = true)
    public abstract static class UnshiftNode extends CoreMethodNode {

        public UnshiftNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public UnshiftNode(UnshiftNode prev) {
            super(prev);
        }

        @Specialization
        public RubyArray unshift(RubyArray array, Object... args) {
            notDesignedForCompilation();

            array.slowUnshift(args);
            return array;
        }

    }

    @CoreMethod(names = "zip", minArgs = 1, maxArgs = 1)
    public abstract static class ZipNode extends ArrayCoreMethodNode {

        public ZipNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public ZipNode(ZipNode prev) {
            super(prev);
        }

        @Specialization(guards = {"isObject", "isOtherIntegerFixnum"}, order = 1)
        public RubyArray zipObjectIntegerFixnum(RubyArray array, RubyArray other) {
            // TODO(CS): use an array allocation site

            final Object[] a = (Object[]) array.getStore();
            final int aLength = array.getSize();

            final int[] b = (int[]) other.getStore();
            final int bLength = other.getSize();

            final int zippedLength = Math.min(aLength, bLength);
            final Object[] zipped = new Object[zippedLength];

            for (int n = 0; n < zippedLength; n++) {
                zipped[n] = RubyArray.slowNewArray(getContext().getCoreLibrary().getArrayClass(), new Object[]{a[n], b[n]}, 2);
            }

            return RubyArray.slowNewArray(getContext().getCoreLibrary().getArrayClass(), zipped, zippedLength);
        }

        @Specialization(guards = {"isObject", "isOtherObject"}, order = 2)
        public RubyArray zipObjectObject(RubyArray array, RubyArray other) {
            // TODO(CS): use an array allocation site

            final Object[] a = (Object[]) array.getStore();
            final int aLength = array.getSize();

            final Object[] b = (Object[]) other.getStore();
            final int bLength = other.getSize();

            final int zippedLength = Math.min(aLength, bLength);
            final Object[] zipped = new Object[zippedLength];

            for (int n = 0; n < zippedLength; n++) {
                zipped[n] = RubyArray.slowNewArray(getContext().getCoreLibrary().getArrayClass(), new Object[]{a[n], b[n]}, 2);
            }

            return RubyArray.slowNewArray(getContext().getCoreLibrary().getArrayClass(), zipped, zippedLength);
        }

    }

    @CoreMethod(names = "hash", maxArgs = 0)
    public abstract static class HashNode extends CoreMethodNode {

        public HashNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public HashNode(HashNode prev) {
            super(prev);
        }

        @Specialization
        public long hashNumber(RubyArray array) {
            return array.hashCode();
        }

    }
}
