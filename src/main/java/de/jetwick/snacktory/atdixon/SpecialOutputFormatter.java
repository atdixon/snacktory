package de.jetwick.snacktory.atdixon;

import de.jetwick.snacktory.OutputFormatter;
import de.jetwick.snacktory.SHelper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * NOTES:
 *      - some methods are rewritten with different and given same name as super but with _ prefix
 *      - general structure of code is attempted to match super for future if we wish to merge any patches from oss project
 */
public final class SpecialOutputFormatter extends OutputFormatter {

    public WithMeta<String> _getFormattedText(Element topNode) {
        removeNodesWithNegativeScores(topNode);
        WithMeta<StringBuilder> sb = WithMeta.of(new StringBuilder());
        _append(topNode, sb, nodesToKeepCssSelector);
        WithMeta<String> str = _innerTrim(sb);
        if (str.value().length() > 100)
            return WithMeta.of(str.toString(), str.meta());

        // no subelements
        if (str.value().isEmpty() || !topNode.text().isEmpty() && str.value().length() <= topNode.ownText().length())
            str = WithMeta.of(topNode.text()); // no meta

        // if jsoup failed to parse the whole html now parse this smaller
        // snippet again to avoid html tags disturbing our text:
        return WithMeta.of(Jsoup.parse(str.value()).text()); // no meta
    }

    /**
     * @see SHelper#innerTrim(String)
     */
    private static WithMeta<String> _innerTrim(final WithMeta<StringBuilder> str) {
        if (str.value().length() == 0)
            return WithMeta.of("");

        // list of segments ((offset, len) pair) "clipped"
        final List<Integer[]> clips = new LinkedList<Integer[]>();

        StringBuilder sb = new StringBuilder();
        int earliestPreviousSpace = -1;
        for (int i = 0; i < str.value().length(); i++) {
            char c = str.value().charAt(i);
            if (c == ' ' || (int) c == 9 || c == '\n') {
                earliestPreviousSpace = earliestPreviousSpace == -1 ? i : earliestPreviousSpace;
                continue;
            }

            if (earliestPreviousSpace > -1) {
                sb.append(' ');
                clips.add(new Integer[] { earliestPreviousSpace + 1, i - earliestPreviousSpace - 1 });
            }

            earliestPreviousSpace = -1;
            sb.append(c);
        }

        return WithMeta.of(sb.toString(),
            new HashMap<String, List<Object>>() {{
                put("href", clip(str.meta().get("href"), clips));
            }});
    }

    /**
     * Recompute meta (offset, len) values given the provided clips. Visible for testing.
     */
    public static List<Object/*StringMeta*/> clip(List<Object/*StringMeta*/> metas, List<Integer[]> clips) {
        // assume meta is in order
        // assume clips is in order

        final List<Object/*StringMeta*/> answer = new ArrayList<>();

        // meta stack -- "earliest" metas on top
        final Stack<StringMeta> metaStack = new Stack<>();
        for (int i = metas.size() - 1; i >= 0; --i)
            metaStack.push((StringMeta) metas.get(i));

        // clip stack -- "earliest" clips on top
        final Stack<Integer[]> clipStack = new Stack<>();
        for (int i = clips.size() - 1; i >= 0; --i)
            clipStack.push(clips.get(i));

        int clipped = 0; // number of clipped chars before curr-meta
        while (!metaStack.isEmpty()) {
            if (clipStack.isEmpty()) {
                final StringMeta currMeta = metaStack.pop();
                answer.add(new StringMeta(currMeta.offset - clipped, currMeta.len, currMeta.val));
                continue;
            }
            final StringMeta currMeta = metaStack.peek();
            final Integer[] currClip = clipStack.peek();

            if (currClip[0] + currClip[1] <= currMeta.offset) { // clip entirely precedes current meta
                clipStack.pop(); // move to next clip
                clipped += currClip[1];
            } else if (currClip[0] >= currMeta.offset + currMeta.len) { // clip entirely succeeds current meta
                metaStack.pop(); // move to next meta
                answer.add(new StringMeta(currMeta.offset - clipped, currMeta.len, currMeta.val));
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
                answer.add(new StringMeta(currMeta.offset - clipped - prec, currMeta.len - snipped, currMeta.val));

                // push a new/adjusted clip to clip anything that *follows* current meta (note: succ may = 0)
                clipStack.push(new Integer[] { currMeta.offset + currMeta.len, succ });

                // increment running clipped total
                clipped += prec + snipped;
            }
        }

        return answer;
    }

    private static boolean overlaps(Integer[] clip, StringMeta meta) {
        return clip[0] < (meta.offset + meta.len)
            && (clip[0] + clip[1]) > meta.offset;
    }

    protected void _append(Element node, WithMeta<StringBuilder> sb, String tagName) {
        // is select more costly then getElementsByTag?
        MAIN:
        for (Element e : node.select(tagName)) {
            Element tmpEl = e;
            // check all elements until 'node'
            while (tmpEl != null && !tmpEl.equals(node)) {
                if (unlikely(tmpEl))
                    continue MAIN;
                tmpEl = tmpEl.parent();
            }

            WithMeta<StringBuilder> text = _node2Text(e);
            if (text.value().length() == 0 || text.value().length() < minParagraphText || text.value().length() > SHelper.countLetters(text.value().toString()) * 2)
                continue;

            internalSpecialAppend(sb, text);
            sb.value().append("\n\n");
        }
    }

    protected WithMeta<StringBuilder> _node2Text(Element el) {
        WithMeta<StringBuilder> sb = WithMeta.of(new StringBuilder(200));
        _appendTextSkipHidden(el, sb);
        return sb;
    }

    /** Note meta accumulated is <em>in order</em>. */
    void _appendTextSkipHidden(Element e, WithMeta<StringBuilder> accum) {
        for (Node child : e.childNodes()) {
            if (unlikely(child))
                continue;
            if (child instanceof TextNode) {
                TextNode textNode = (TextNode) child;
                String txt = textNode.text();
                accum.value().append(txt);
            } else if (child instanceof Element) {
                Element element = (Element) child;
                if (accum.value().length() > 0 && element.isBlock() && !lastCharIsWhitespace(accum.value()))
                    accum.value().append(" ");
                else if (element.tagName().equals("br"))
                    accum.value().append(" ");
                if (element.tagName().equals("a")) {
                    final StringBuilder hrefText = new StringBuilder();
                    // note: here we call super version of method because we don't need metadata
                    // accumulated *within* an "a" tag.
                    super.appendTextSkipHidden(element, hrefText);
                    if (hrefText.length() > 1) {
                        final String href = element.attr("href");
                        if (href != null && href.trim().length() > 0) { // href is not blank
                            final StringMeta meta
                                = new StringMeta(accum.value().length(), hrefText.length(), href);
                            accum.value().append(hrefText);
                            accum.addMeta("href", meta);
                        }
                    }
                } else {
                    _appendTextSkipHidden(element, accum);
                }
            }
        }
    }

    /** Append source to target merging and adjusting known metadata. */
    private static void internalSpecialAppend(WithMeta<StringBuilder> target,
                                              WithMeta<StringBuilder> source) {
        final int targetLen = target.value().length();
        target.value().append(source.value()); // append the actual text
        // append the meta, creating new metas with adjusted offsets
        for (Object meta : source.getMeta("href")) {
            final StringMeta asImpl = (StringMeta) meta;
            target.addMeta("href", new StringMeta(asImpl.offset + targetLen, asImpl.len, asImpl.val));
        }
    }

}
