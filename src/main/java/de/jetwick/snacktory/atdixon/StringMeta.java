package de.jetwick.snacktory.atdixon;

import java.util.Objects;

public final class StringMeta {

    public final int offset; // offset of text within string
    public final int len;    // length of text within string
    public final String val; // value of metadata

    public StringMeta(int offset, int len, String val) {
        this.offset = offset;
        this.len = len;
        this.val = val;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringMeta that = (StringMeta) o;
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
        return String.format("StringMeta(%s,%s,%s)", offset, len, val);
    }
}
