package de.jetwick.snacktory.atdixon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wraps an object with metadata that is a mapping of keys to value(s).
 * Not thread-safe.
 */
public final class WithMeta<T> {

    public static <T> WithMeta<T> of(T val) {
        return new WithMeta<T>(val, new HashMap<String, List<Object>>());
    }

    public static <T> WithMeta<T> of(T val, Map<String, List<Object>> meta) {
        return new WithMeta<T>(val, new HashMap<String, List<Object>>(meta)); // copy, so its guaranteed mutable and separate from others
    }

    private final T value;
    private final Map<String, List<Object>> meta;

    private WithMeta(T value, Map<String, List<Object>> meta) {
        this.value = value;
        this.meta = meta;
    }

    public T value() {
        return value;
    }

    public Map<String, List<Object>> meta() {
        return Collections.unmodifiableMap(meta);
    }

    /** Answers possibly empty never null list. */
    public List<Object> getMeta(String key) {
        List<Object> existing = meta.get(key);
        if (existing == null)
            meta.put(key, existing = new ArrayList<Object>());
        return existing;
    }

    public void addMeta(String key, Object val) {
        // add to *end*
        getMeta(key).add(val);
    }

}
