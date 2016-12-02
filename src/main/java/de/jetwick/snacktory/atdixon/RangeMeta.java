package de.jetwick.snacktory.atdixon;

import java.util.Objects;

public final class RangeMeta<T> {

    public final int offset; // offset of text within string
    public final int len;    // length of text within string
    public final T val;      // value of metadata

    public RangeMeta(int offset, int len, T val) {
        this.offset = offset;
        this.len = len;
        this.val = val;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RangeMeta that = (RangeMeta) o;
        return offset == that.offset &&
            len == that.len &&
            Objects.equals(val, that.val);
    }

    @Override
    public int hashCode() {
        return Objects.hash(offset, len, val);
    }

    @Override
    public String toString() {
        return String.format("RangeMeta(%s,%s,%s)", offset, len, val);
    }
}
