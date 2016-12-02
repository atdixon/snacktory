package de.jetwick.snacktory.atdixon;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public final class InternalUtil {

    private InternalUtil() {}

    /**
     * Recompute meta (offset, len) values given the provided clips. Visible for testing.
     */
    public static <T> List<RangeMeta<T>> clip(List<RangeMeta<T>> metas, List<Integer[]> clips) {
        // assume meta is in order
        // assume clips is in order

        final List<RangeMeta<T>> answer = new ArrayList<>();

        // meta stack -- "earliest" metas on top
        final Stack<RangeMeta<T>> metaStack = new Stack<>();
        for (int i = metas.size() - 1; i >= 0; --i)
            metaStack.push(metas.get(i));

        // clip stack -- "earliest" clips on top
        final Stack<Integer[]> clipStack = new Stack<>();
        for (int i = clips.size() - 1; i >= 0; --i)
            clipStack.push(clips.get(i));

        int clipped = 0; // number of clipped chars before curr-meta
        while (!metaStack.isEmpty()) {
            if (clipStack.isEmpty()) {
                final RangeMeta<T> currMeta = metaStack.pop();
                answer.add(new RangeMeta<>(currMeta.offset - clipped, currMeta.len, currMeta.val));
                continue;
            }
            final RangeMeta<T> currMeta = metaStack.peek();
            final Integer[] currClip = clipStack.peek();

            if (currClip[0] + currClip[1] <= currMeta.offset) { // clip entirely precedes current meta
                clipStack.pop(); // move to next clip
                clipped += currClip[1];
            } else if (currClip[0] >= currMeta.offset + currMeta.len) { // clip entirely succeeds current meta
                metaStack.pop(); // move to next meta
                answer.add(new RangeMeta<>(currMeta.offset - clipped, currMeta.len, currMeta.val));
            } else { // current clip overlaps current meta
                clipStack.pop(); // move to next clip
                metaStack.pop(); // move to next meta

                // chars if any preceding current meta
                final int prec = Math.max(0, currMeta.offset - currClip[0]);

                Integer[] curr = currClip; // invariant: curr ALWAYS overlaps currMeta
                int snipped = 0;
                while (true) {
                    final int snip =
                        Math.min(curr[0] + curr[1], currMeta.offset + currMeta.len)
                            - Math.max(curr[0], currMeta.offset);

                    snipped += snip;

                    if (!clipStack.isEmpty() && overlaps(clipStack.peek(), currMeta)) {
                        curr = clipStack.pop();
                    } else {
                        break;
                    }
                }

                // chars if any succeeding last overlapper
                final int succ =
                    Math.max(0, (curr[0] + curr[1]) - (currMeta.offset + currMeta.len));

                // now that we've handled all clips potentially affecting current meta, add adjusted current meta
                answer.add(new RangeMeta<>(currMeta.offset - clipped - prec, currMeta.len - snipped, currMeta.val));

                // push a new/adjusted clip to clip anything that *follows* current meta (note: succ may = 0)
                clipStack.push(new Integer[] { currMeta.offset + currMeta.len, succ });

                // increment running clipped total
                clipped += prec + snipped;
            }
        }

        return answer;
    }

    private static boolean overlaps(Integer[] clip, RangeMeta<?> meta) {
        return clip[0] < (meta.offset + meta.len)
            && (clip[0] + clip[1]) > meta.offset;
    }
}
