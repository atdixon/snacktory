package de.jetwick.snacktory.atdixon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wraps an object with metadata that is a mapping of types to metadata value(s) of that type.
 * Not thread-safe.
 */
public final class WithMeta<T extends CharSequence> {

    public static <T extends CharSequence> WithMeta<T> of(T val) {
        return new WithMeta<>(val, new HashMap<Class, List<RangeMeta>>());
    }

    @SuppressWarnings("unchecked")
    public static <C, T extends CharSequence> WithMeta<T> of(T val, final Class<C> type, final List<RangeMeta<C>> meta) {
        return new WithMeta<>(val, new HashMap<Class, List<RangeMeta>>() {{
            put(type, (List) meta);
        }});
    }

    public static <T extends CharSequence> WithMeta<T> of(T val, Map<Class, List<RangeMeta>> meta) {
        return new WithMeta<>(val, meta);
    }

    private final T value;
    private final Map<Class, List<RangeMeta>> meta;

    private WithMeta(T value, Map<Class, List<RangeMeta>> meta) {
        this.value = value;
        this.meta = new HashMap<>(meta); // copy, so it's guaranteed mutable and separate from others
    }

    public T value() {
        return value;
    }

    /*package*/ Map<Class, List<RangeMeta>> meta() {
        return Collections.unmodifiableMap(meta);
    }

    /** Answers possibly empty never null list. */
    @SuppressWarnings("unchecked")
    public <C> List<RangeMeta<C>> getMeta(Class<C> type) {
        List existing = meta.get(type);
        if (existing == null)
            meta.put(type, existing = new ArrayList<>());
        return existing;
    }

    public <C> void addMeta(Class<C> type, RangeMeta<C> val) {
        // add to *end*
        getMeta(type).add(val);
    }

}
