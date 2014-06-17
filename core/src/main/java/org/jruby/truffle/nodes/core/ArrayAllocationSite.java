/*
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.core;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.nodes.Node;
import org.jruby.truffle.nodes.RubyNode;
import org.jruby.truffle.runtime.core.RubyArray;
import org.jruby.truffle.runtime.core.RubyFixnum;
import org.jruby.truffle.runtime.util.ArrayUtils;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.util.cli.Options;

public abstract class ArrayAllocationSite extends Node {

    protected final RubyContext context;

    public ArrayAllocationSite(RubyContext context) {
        this.context = context;
    }

    public abstract Class evaluateElementsAs();

    public RubyArray empty() {
        return RubyArray.unsafeNewArray(getContext().getCoreLibrary().getArrayClass(), null, 0);
    }

    public abstract Object start(int expectedLength);

    public abstract Object set(Object store, int index, Object value);

    public Object set(Object store, int index, int value) {
        RubyNode.notDesignedForCompilation();
        return set(store, index, (Object) value);
    }

    public Object set(Object store, int index, long value) {
        RubyNode.notDesignedForCompilation();
        return set(store, index, (Object) value);
    }

    public Object set(Object store, int index, double value) {
        RubyNode.notDesignedForCompilation();
        return set(store, index, (Object) value);
    }

    public Object set(Object store, int index, Object values, int length) {
        RubyNode.notDesignedForCompilation();

        if (values == null) {
            return store;
        } else if (values instanceof byte[]) {
            return setAll(store, index, (byte[]) values, length);
        } else if (values instanceof int[]) {
            return setAll(store, index, (int[]) values, length);
        } else if (values instanceof long[]) {
            return setAll(store, index, (long[]) values, length);
        } else if (values instanceof double[]) {
            return setAll(store, index, (double[]) values, length);
        } else if (values instanceof Object[]) {
            return setAll(store, index, (Object[]) values, length);
        } else {
            CompilerDirectives.transferToInterpreter();
            throw new UnsupportedOperationException();
        }
    }

    public Object setAll(Object store, int index, byte[] values, int length) {
        return setAll(store, index, values, 0, length);
    }

    public Object setAll(Object store, int index, int[] values, int length) {
        return setAll(store, index, values, 0, length);
    }

    public Object setAll(Object store, int index, long[] values, int length) {
        return setAll(store, index, values, 0, length);
    }

    public Object setAll(Object store, int index, double[] values, int length) {
        return setAll(store, index, values, 0, length);
    }

    public Object setAll(Object store, int index, Object[] values, int length) {
        return setAll(store, index, values, 0, length);
    }

    public abstract Object setAll(Object store, int index, byte[] values, int start, int length);
    public abstract Object setAll(Object store, int index, int[] values, int start, int length);
    public abstract Object setAll(Object store, int index, long[] values, int start, int length);
    public abstract Object setAll(Object store, int index, double[] values, int start, int length);
    public abstract Object setAll(Object store, int index, Object[] values, int start, int length);

    public RubyArray finish(Object store, int length) {
        return RubyArray.unsafeNewArray(getContext().getCoreLibrary().getArrayClass(), finishStore(store), length);
    }

    public Object finishStore(Object store) {
        return store;
    }

    protected RubyContext getContext() {
        return context;
    }

    public static class UninitializedArrayAllocationSite extends ArrayAllocationSite {

        private boolean couldUseInteger = Options.TRUFFLE_ARRAYS_INT.load();
        private boolean couldUseLong = Options.TRUFFLE_ARRAYS_LONG.load();
        private boolean couldUseDouble = Options.TRUFFLE_ARRAYS_DOUBLE.load();

        public UninitializedArrayAllocationSite(RubyContext context) {
            super(context);
        }

        @Override
        public Class evaluateElementsAs() {
            return Object.class;
        }

        @Override
        public Object start(int expectedLength) {
            CompilerDirectives.transferToInterpreter();
            System.err.println("transfer");

            return new Object[expectedLength];
        }

        @Override
        public Object set(Object store, int index, Object value) {
            CompilerDirectives.transferToInterpreter();
            System.err.println("transfer");

            if (value instanceof Integer) {
                couldUseDouble = false;
            } else if (value instanceof Long) {
                couldUseInteger = false;
                couldUseDouble = false;
            } else if (value instanceof Double) {
                couldUseInteger = false;
                couldUseLong = false;
            } else {
                couldUseInteger = false;
                couldUseLong = false;
                couldUseDouble = false;
            }

            ((Object[]) store)[index] = value;
            return store;
        }

        @Override
        public Object setAll(Object store, int index, byte[] values, int start, int length) {
            CompilerDirectives.transferToInterpreter();
            System.err.println("transfer");

            couldUseDouble = false;

            for (int n = 0; n < length; n++) {
                ((Object[]) store)[index + n] = values[start + n];
            }

            return store;
        }

        @Override
        public Object setAll(Object store, int index, int[] values, int start, int length) {
            CompilerDirectives.transferToInterpreter();
            System.err.println("transfer");

            couldUseDouble = false;

            for (int n = 0; n < length; n++) {
                ((Object[]) store)[index + n] = values[start + n];
            }

            return store;
        }

        @Override
        public Object setAll(Object store, int index, long[] values, int start, int length) {
            CompilerDirectives.transferToInterpreter();
            System.err.println("transfer");

            couldUseDouble = false;

            for (int n = 0; n < length; n++) {
                couldUseInteger &= RubyFixnum.fitsIntoInteger(values[n]);
                ((Object[]) store)[index + n] = values[start + n];
            }

            return store;
        }

        @Override
        public Object setAll(Object store, int index, double[] values, int start, int length) {
            CompilerDirectives.transferToInterpreter();
            System.err.println("transfer");

            couldUseInteger = false;
            couldUseLong = false;

            for (int n = 0; n < length; n++) {
                ((Object[]) store)[index + n] = values[start + n];
            }

            return store;
        }

        @Override
        public Object setAll(Object store, int index, Object[] values, int start, int length) {
            CompilerDirectives.transferToInterpreter();
            System.err.println("transfer");

            for (int n = 0; n < length; n++) {
                final Object value = values[start + n];

                if (value instanceof Integer) {
                    couldUseDouble = false;
                } else if (value instanceof Long) {
                    if (!RubyFixnum.fitsIntoInteger((long) value)) {
                        couldUseInteger = false;
                    }

                    couldUseDouble = false;
                } else if (value instanceof Double) {
                    couldUseInteger = false;
                    couldUseLong = false;
                } else {
                    couldUseInteger = false;
                    couldUseLong = false;
                    couldUseDouble = false;
                }

                ((Object[]) store)[index + n] = values[start + n];
            }

            return store;
        }

        @Override
        public Object finishStore(Object store) {
            CompilerDirectives.transferToInterpreter();
            System.err.println("transfer");

            if (couldUseInteger) {
                replace(new IntegerArrayAllocationSite(context));
                return ArrayUtils.unboxInteger((Object[]) store);
            } else if (couldUseLong) {
                replace(new LongArrayAllocationSite(context));
                return ArrayUtils.unboxLong((Object[]) store);
            } else if (couldUseDouble) {
                replace(new DoubleArrayAllocationSite(context));
                return ArrayUtils.unboxDouble((Object[]) store);
            } else {
                replace(new ObjectArrayAllocationSite(context));
                return store;
            }
        }

    }

    public static class IntegerArrayAllocationSite extends ArrayAllocationSite {

        public IntegerArrayAllocationSite(RubyContext context) {
            super(context);
        }

        @Override
        public Class evaluateElementsAs() {
            return Integer.class;
        }

        @Override
        public Object start(int expectedLength) {
            return new int[expectedLength];
        }

        @Override
        public Object set(Object store, int index, Object value) {
            if (value instanceof Integer) {
                return set(store, index, (int) value);
            } else if (value instanceof Long && RubyFixnum.fitsIntoInteger((long) value)) {
                return set(store, index, (long) value);
            } else {
                CompilerDirectives.transferToInterpreter();

                replace(new ObjectArrayAllocationSite(context));

                final Object[] newStore = ArrayUtils.box((int[]) store);
                newStore[index] = value;
                return newStore;
            }
        }

        @Override
        public Object set(Object store, int index, int value) {
            ((int[]) store)[index] = value;
            return store;
        }

        @Override
        public Object set(Object store, int index, long value) {
            if (RubyFixnum.fitsIntoInteger(value)) {
                ((int[]) store)[index] = (int) value;
                return store;
            } else {
                return set(store, index, value);
            }
        }

        @Override
        public Object setAll(Object store, int index, byte[] values, int start, int length) {
            for (int n = 0; n < length; n++) {
                ((int[]) store)[index + n] = values[start + n];
            }

            return store;
        }

        @Override
        public Object setAll(Object store, int index, int[] values, int start, int length) {
            System.arraycopy(values, start, store, index, length);
            return store;
        }

        @Override
        public Object setAll(Object store, int index, long[] values, int start, int length) {
            final int[] intStore = (int[]) store;

            for (int n = 0; n < length; n++) {
                if (RubyFixnum.fitsIntoInteger(values[n])) {
                    intStore[index + n] = (int) values[start + n];
                } else {
                    CompilerDirectives.transferToInterpreter();
                    throw new UnsupportedOperationException();
                }
            }

            return store;
        }

        @Override
        public Object setAll(Object store, int index, double[] values, int start, int length) {
            CompilerDirectives.transferToInterpreter();
            throw new UnsupportedOperationException();
        }

        @Override
        public Object setAll(Object store, int index, Object[] values, int start, int length) {
            final int[] intStore = (int[]) store;

            for (int n = 0; n < length; n++) {
                final Object value = values[start + n];

                if (value instanceof Integer) {
                    intStore[index + n] = (int) value;
                } else if (value instanceof Long && RubyFixnum.fitsIntoInteger((long) value)) {
                    intStore[index + n] = (int) (long) value;
                } else {
                    CompilerDirectives.transferToInterpreter();
                    System.err.println("transfer");

                    final Object[] objectStore = new Object[intStore.length];

                    for (int i = 0; i < index; i++) {
                        objectStore[i] = intStore[i];
                    }

                    for (int i = 0; i < length; i++) {
                        objectStore[index + i] = values[start + i];
                    }

                    replace(new ObjectArrayAllocationSite(context));

                    return objectStore;
                }
            }

            return store;
        }

    }

    public static class LongArrayAllocationSite extends ArrayAllocationSite {

        public LongArrayAllocationSite(RubyContext context) {
            super(context);
        }

        @Override
        public Class evaluateElementsAs() {
            return Long.class;
        }

        @Override
        public Object start(int expectedLength) {
            RubyNode.notDesignedForCompilation();

            return new long[expectedLength];
        }

        @Override
        public Object set(Object store, int index, Object value) {
            RubyNode.notDesignedForCompilation();

            if (value instanceof Long) {
                return set(store, index, (long) value);
            } else if (value instanceof Integer) {
                return set(store, index, (int) value);
            } else {
                CompilerDirectives.transferToInterpreter();

                replace(new ObjectArrayAllocationSite(context));

                final Object[] newStore = ArrayUtils.box((long[]) store);
                newStore[index] = value;
                return newStore;
            }
        }

        @Override
        public Object set(Object store, int index, int value) {
            RubyNode.notDesignedForCompilation();

            ((long[]) store)[index] = value;
            return store;
        }

        @Override
        public Object set(Object store, int index, long value) {
            RubyNode.notDesignedForCompilation();

            ((long[]) store)[index] = value;
            return store;
        }

        @Override
        public Object setAll(Object store, int index, byte[] values, int start, int length) {
            RubyNode.notDesignedForCompilation();

            for (int n = 0; n < length; n++) {
                ((byte[]) store)[index + n] = values[start + n];
            }

            return store;
        }

        @Override
        public Object setAll(Object store, int index, int[] values, int start, int length) {
            RubyNode.notDesignedForCompilation();

            final long[] longStore = (long[]) store;

            for (int n = 0; n < length; n++) {
                longStore[index + n] = values[start + n];
            }

            return store;
        }

        @Override
        public Object setAll(Object store, int index, long[] values, int start, int length) {
            RubyNode.notDesignedForCompilation();

            System.arraycopy(values, start, store, index, length);
            return store;
        }

        @Override
        public Object setAll(Object store, int index, double[] values, int start, int length) {
            CompilerDirectives.transferToInterpreter();
            throw new UnsupportedOperationException();
        }

        @Override
        public Object setAll(Object store, int index, Object[] values, int start, int length) {
            RubyNode.notDesignedForCompilation();

            final long[] longStore = (long[]) store;

            for (int n = 0; n < length; n++) {
                final Object value = values[start + n];

                if (value instanceof Integer) {
                    longStore[index + n] = (int) value;
                } else if (value instanceof Long && RubyFixnum.fitsIntoInteger((long) value)) {
                    longStore[index + n] = (long) value;
                } else {
                    CompilerDirectives.transferToInterpreter();
                    throw new UnsupportedOperationException();
                }
            }

            return store;
        }

    }

    public static class DoubleArrayAllocationSite extends ArrayAllocationSite {

        public DoubleArrayAllocationSite(RubyContext context) {
            super(context);
        }

        @Override
        public Class evaluateElementsAs() {
            return Double.class;
        }

        @Override
        public Object start(int length) {
            RubyNode.notDesignedForCompilation();

            return new double[length];
        }

        @Override
        public Object set(Object store, int index, Object value) {
            RubyNode.notDesignedForCompilation();

            if (value instanceof Double) {
                ((double[]) store)[index] = (double) value;
                return store;
            } else {
                CompilerDirectives.transferToInterpreter();

                replace(new ObjectArrayAllocationSite(context));

                final Object[] newStore = ArrayUtils.box((double[]) store);
                newStore[index] = value;
                return newStore;
            }
        }

        @Override
        public Object set(Object store, int index, double value) {
            RubyNode.notDesignedForCompilation();

            ((double[]) store)[index] = value;
            return store;
        }

        @Override
        public Object setAll(Object store, int index, byte[] values, int start, int length) {
            CompilerDirectives.transferToInterpreter();
            throw new UnsupportedOperationException();
        }

        @Override
        public Object setAll(Object store, int index, int[] values, int start, int length) {
            CompilerDirectives.transferToInterpreter();
            throw new UnsupportedOperationException();
        }

        @Override
        public Object setAll(Object store, int index, long[] values, int start, int length) {
            CompilerDirectives.transferToInterpreter();
            throw new UnsupportedOperationException();
        }

        @Override
        public Object setAll(Object store, int index, double[] values, int start, int length) {
            RubyNode.notDesignedForCompilation();

            System.arraycopy(values, start, store, index, length);
            return store;
        }

        @Override
        public Object setAll(Object store, int index, Object[] values, int start, int length) {
            RubyNode.notDesignedForCompilation();

            final double[] doubleStore = (double[]) store;

            for (int n = 0; n < length; n++) {
                final Object value = values[start + n];

                if (value instanceof Double) {
                    doubleStore[index + n] = (double) value;
                } else {
                    CompilerDirectives.transferToInterpreter();
                    throw new UnsupportedOperationException();
                }
            }

            return store;
        }

    }

    public static class ObjectArrayAllocationSite extends ArrayAllocationSite {

        public ObjectArrayAllocationSite(RubyContext context) {
            super(context);
        }

        @Override
        public Class evaluateElementsAs() {
            return Object.class;
        }

        @Override
        public Object start(int expectedLength) {
            return new Object[expectedLength];
        }

        @Override
        public Object set(Object store, int index, Object value) {
            ((Object[]) store)[index] = value;
            return store;
        }

        @Override
        public Object setAll(Object store, int index, byte[] values, int start, int length) {
            for (int n = 0; n < length; n++) {
                ((Object[]) store)[index + n] = values[start + n];
            }

            return store;
        }

        @Override
        public Object setAll(Object store, int index, int[] values, int start, int length) {
            final Object[] objectStore = (Object[]) store;

            for (int n = 0; n < length; n++) {
                objectStore[index + n] = values[start + n];
            }

            return store;
        }

        @Override
        public Object setAll(Object store, int index, long[] values, int start, int length) {
            final Object[] objectStore = (Object[]) store;

            for (int n = 0; n < length; n++) {
                objectStore[index + n] = values[start + n];
            }

            return store;
        }

        @Override
        public Object setAll(Object store, int index, double[] values, int start, int length) {
            final Object[] objectStore = (Object[]) store;

            for (int n = 0; n < length; n++) {
                objectStore[index + n] = values[start + n];
            }

            return store;
        }

        @Override
        public Object setAll(Object store, int index, Object[] values, int start, int length) {
            final Object[] objectStore = (Object[]) store;

            for (int n = 0; n < length; n++) {
                objectStore[index + n] = values[start + n];
            }

            return store;
        }

    }

}
