package net.deanly.structlayout.type.guava;

/*
 * Copyright (C) 2011 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * A wrapper class for unsigned {@code long} values, supporting arithmetic operations.
 *
 * <p>In some cases, when speed is more important than code readability, it may be faster simply to
 * treat primitive {@code long} values as unsigned, using the methods from {@link UnsignedLongs}.
 *
 * <p>See the Guava User Guide article on <a
 * href="https://github.com/google/guava/wiki/PrimitivesExplained#unsigned-support">unsigned
 * primitive utilities</a>.
 *
 * @author Louis Wasserman
 * @author Colin Evans
 * @since 11.0
 */
public final class UnsignedLong extends Number implements Comparable<UnsignedLong> {

    private static final long UNSIGNED_MASK = 0x7fffffffffffffffL;

    private static final int CACHE_SIZE = 2048;
    private static final UnsignedLong[] CACHE = new UnsignedLong[CACHE_SIZE];
    public static final UnsignedLong ZERO;
    public static final UnsignedLong ONE;
    public static final UnsignedLong MAX_VALUE = new UnsignedLong(-1L);

    static {
        for (int i = 0; i < CACHE_SIZE; i++) {
            CACHE[i] = new UnsignedLong(i);
        }
        ZERO = CACHE[0];
        ONE = CACHE[1];
    }

    private final long value;

    UnsignedLong(long value) {
        this.value = value;
    }

    /**
     * Returns an {@code UnsignedLong} corresponding to a given bit representation. The argument is
     * interpreted as an unsigned 64-bit value. Specifically, the sign bit of {@code bits} is
     * interpreted as a normal bit, and all other bits are treated as usual.
     *
     * <p>If the argument is nonnegative, the returned result will be equal to {@code bits},
     * otherwise, the result will be equal to {@code 2^64 + bits}.
     *
     * <p>To represent decimal constants less than {@code 2^63}, consider {@link #valueOf(long)}
     * instead.
     *
     * @since 14.0
     */
    public static UnsignedLong fromLongBits(long bits) {
        if (bits >= 0 && bits < CACHE_SIZE) {
            return CACHE[(int) bits];
        }
        return new UnsignedLong(bits);
    }

    /**
     * Returns an {@code UnsignedLong} representing the same value as the specified {@code long}.
     *
     * @throws IllegalArgumentException if {@code value} is negative
     * @since 14.0
     */
    public static UnsignedLong valueOf(long value) {
        checkArgument(value >= 0, value);
        return fromLongBits(value);
    }

    /**
     * Returns a {@code UnsignedLong} representing the same value as the specified {@code BigInteger}.
     * This is the inverse operation of {@link #bigIntegerValue()}.
     *
     * @throws IllegalArgumentException if {@code value} is negative or {@code value >= 2^64}
     */
    public static UnsignedLong valueOf(BigInteger value) {
        checkNotNull(value);
        checkArgument(
                value.signum() >= 0 && value.bitLength() <= Long.SIZE,
                value);
        return fromLongBits(value.longValue());
    }

    /**
     * Returns an {@code UnsignedLong} holding the value of the specified {@code String}, parsed as an
     * unsigned {@code long} value.
     *
     * @throws NumberFormatException if the string does not contain a parsable unsigned {@code long}
     *     value
     */
    public static UnsignedLong valueOf(String string) {
        return valueOf(string, 10);
    }

    /**
     * Returns an {@code UnsignedLong} holding the value of the specified {@code String}, parsed as an
     * unsigned {@code long} value in the specified radix.
     *
     * @throws NumberFormatException if the string does not contain a parsable unsigned {@code long}
     *     value, or {@code radix} is not between {@link Character#MIN_RADIX} and {@link
     *     Character#MAX_RADIX}
     */
    public static UnsignedLong valueOf(String string, int radix) {
        return fromLongBits(UnsignedLongs.parseUnsignedLong(string, radix));
    }

    /**
     * Returns the result of adding this and {@code val}. If the result would have more than 64 bits,
     * returns the low 64 bits of the result.
     *
     * @since 14.0
     */
    public UnsignedLong plus(UnsignedLong val) {
        return fromLongBits(this.value + checkNotNull(val).value);
    }

    /**
     * Returns the result of subtracting this and {@code val}. If the result would have more than 64
     * bits, returns the low 64 bits of the result.
     *
     * @since 14.0
     */
    public UnsignedLong minus(UnsignedLong val) {
        return fromLongBits(this.value - checkNotNull(val).value);
    }

    /**
     * Returns the result of multiplying this and {@code val}. If the result would have more than 64
     * bits, returns the low 64 bits of the result.
     *
     * @since 14.0
     */
    public UnsignedLong multiply(UnsignedLong val) {
        return fromLongBits(value * checkNotNull(val).value);
    }

    /**
     * Returns the result of dividing this by {@code val}.
     *
     * @since 14.0
     */
    public UnsignedLong divide(UnsignedLong val) {
        return fromLongBits(UnsignedLongs.divide(value, checkNotNull(val).value));
    }

    /**
     * Returns this modulo {@code val}.
     *
     * @since 14.0
     */
    public UnsignedLong mod(UnsignedLong val) {
        return fromLongBits(UnsignedLongs.remainder(value, checkNotNull(val).value));
    }

    /** Returns the value of this {@code UnsignedLong} as an {@code int}. */
    @Override
    public int intValue() {
        return (int) value;
    }

    /**
     * Returns the value of this {@code UnsignedLong} as a {@code long}. This is an inverse operation
     * to {@link #fromLongBits}.
     *
     * <p>Note that if this {@code UnsignedLong} holds a value {@code >= 2^63}, the returned value
     * will be equal to {@code this - 2^64}.
     */
    @Override
    public long longValue() {
        return value;
    }

    /**
     * Returns the value of this {@code UnsignedLong} as a {@code float}, analogous to a widening
     * primitive conversion from {@code long} to {@code float}, and correctly rounded.
     */
    @Override
    public float floatValue() {
        if (value >= 0) {
            return (float) value;
        }
        // The top bit is set, which means that the float value is going to come from the top 24 bits.
        // So we can ignore the bottom 8, except for rounding. See doubleValue() for more.
        return (float) ((value >>> 1) | (value & 1)) * 2f;
    }

    /**
     * Returns the value of this {@code UnsignedLong} as a {@code double}, analogous to a widening
     * primitive conversion from {@code long} to {@code double}, and correctly rounded.
     */
    @Override
    public double doubleValue() {
        if (value >= 0) {
            return (double) value;
        }
        // The top bit is set, which means that the double value is going to come from the top 53 bits.
        // So we can ignore the bottom 11, except for rounding. We can unsigned-shift right 1, aka
        // unsigned-divide by 2, and convert that. Then we'll get exactly half of the desired double
        // value. But in the specific case where the bottom two bits of the original number are 01, we
        // want to replace that with 1 in the shifted value for correct rounding.
        return (double) ((value >>> 1) | (value & 1)) * 2.0;
    }

    /** Returns the value of this {@code UnsignedLong} as a {@link BigInteger}. */
    public BigInteger bigIntegerValue() {
        BigInteger bigInt = BigInteger.valueOf(value & UNSIGNED_MASK);
        if (value < 0) {
            bigInt = bigInt.setBit(Long.SIZE - 1);
        }
        return bigInt;
    }

    /** Returns the value of this {@code UnsignedLong} as a {@link BigInteger}. */
    public BigInteger toBigInteger() {
        return bigIntegerValue();
    }

    /**
     * Returns the value of this {@code UnsignedLong} as a {@link BigDecimal}.
     */
    public BigDecimal toBigDecimal() {
        return new BigDecimal(bigIntegerValue());
    }

    @Override
    public int compareTo(UnsignedLong o) {
        checkNotNull(o);
        return UnsignedLongs.compare(value, o.value);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UnsignedLong other) {
            return value == other.value;
        }
        return false;
    }

    /** Returns a string representation of the {@code UnsignedLong} value, in base 10. */
    @Override
    public String toString() {
        return UnsignedLongs.toString(value);
    }

    /**
     * Returns a string representation of the {@code UnsignedLong} value, in base {@code radix}. If
     * {@code radix < Character.MIN_RADIX} or {@code radix > Character.MAX_RADIX}, the radix {@code
     * 10} is used.
     */
    public String toString(int radix) {
        return UnsignedLongs.toString(value, radix);
    }


    /**
     * Ensures that the given argument satisfies the condition.
     * Supports dynamic message formatting with multiple arguments.
     *
     * @param condition        the condition to check for validity
     * @param errorMessageArgs the arguments for the error message template
     * @throws IllegalArgumentException if the condition is false
     */
    private static void checkArgument(boolean condition, Object... errorMessageArgs) {
        if (!condition) {
            // Use String.format to handle dynamic messages
            throw new IllegalArgumentException(String.format("value (%s) is outside the range for an unsigned long value", errorMessageArgs));
        }
    }

    /**
     * Ensures that the given object reference is not null.
     *
     * @param reference the object to check for null
     * @return the validated reference (non-null)
     * @throws NullPointerException if the reference is null
     */
    private static <T> T checkNotNull(T reference) {
        if (reference == null) {
            throw new NullPointerException("Reference is null.");
        }
        return reference;
    }

    /**
     * Compares this {@code UnsignedLong} with the specified {@code UnsignedLong}
     * and determines if this instance is greater than the other.
     *
     * @param other the {@code UnsignedLong} to be compared
     * @return {@code true} if this {@code UnsignedLong} is greater than the specified {@code other};
     *         {@code false} otherwise
     */
    public boolean isGreaterThan(UnsignedLong other) {
        checkNotNull(other);
        return this.compareTo(other) > 0;
    }

    /**
     * Compares this {@code UnsignedLong} with the specified {@code UnsignedLong}
     * and determines if this instance is greater than or equal to the other.
     *
     * @param other the {@code UnsignedLong} to be compared
     * @return {@code true} if this {@code UnsignedLong} is greater than or equal to the specified {@code other};
     *         {@code false} otherwise
     */
    public boolean isGreaterThanOrEqualTo(UnsignedLong other) {
        checkNotNull(other);
        return this.compareTo(other) >= 0;
    }

    /**
     * Compares this {@code UnsignedLong} with the specified {@code UnsignedLong}
     * and determines if this instance is less than the other.
     *
     * @param other the {@code UnsignedLong} to be compared
     * @return {@code true} if this {@code UnsignedLong} is less than the specified {@code other};
     *         {@code false} otherwise
     */
    public boolean isLessThan(UnsignedLong other) {
        checkNotNull(other);
        return this.compareTo(other) < 0;
    }

    /**
     * Compares this {@code UnsignedLong} with the specified {@code UnsignedLong}
     * to determine if this instance is less than or equal to the other.
     *
     * @param other the {@code UnsignedLong} to be compared
     * @return {@code true} if this {@code UnsignedLong} is less than or equal to the specified {@code other};
     *         {@code false} otherwise
     */
    public boolean isLessThanOrEqualTo(UnsignedLong other) {
        checkNotNull(other);
        return this.compareTo(other) <= 0;
    }

    /**
     * Determines if this {@code UnsignedLong} has the same value as the specified {@code UnsignedLong}.
     *
     * @param other the {@code UnsignedLong} to compare with this instance
     * @return {@code true} if this {@code UnsignedLong} has the same value as {@code other};
     *         {@code false} otherwise
     * @throws NullPointerException if {@code other} is null
     */
    public boolean hasEqualAmount(UnsignedLong other) {
        checkNotNull(other);
        return this.equals(other);
    }

    /**
     * Determines if this {@code UnsignedLong} represents the value zero.
     *
     * @return {@code true} if this {@code UnsignedLong} equals zero; {@code false} otherwise
     */
    public boolean isZero() {
        return this.equals(ZERO);
    }

}
