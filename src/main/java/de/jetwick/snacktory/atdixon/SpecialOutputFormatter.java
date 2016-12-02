package de.jetwick.snacktory.atdixon;

import de.jetwick.snacktory.OutputFormatter;
import de.jetwick.snacktory.SHelper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
            return WithMeta.of(str.value(), str.meta());

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

        return WithMeta.of(sb.toString(), Href.class, InternalUtil.clip(str.getMeta(Href.class), clips));
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
                if (!isEmpty(element.attr("href"))) {
                    final StringBuilder hrefText = new StringBuilder();
                    // note: here we call super version of method because we don't need metadata
                    // accumulated *within* an "a" tag.
                    super.appendTextSkipHidden(element, hrefText);
                    if (hrefText.length() > 1) {
                        final String href = element.attr("href");
                        if (href != null && href.trim().length() > 0) { // href is not blank
                            final RangeMeta<Href> meta
                                = new RangeMeta<>(accum.value().length(), hrefText.length(), new Href(element.tagName(), href));
                            accum.value().append(hrefText);
                            accum.addMeta(Href.class, meta);
                        }
                    }
                } else {
                    _appendTextSkipHidden(element, accum);
                }
            }
        }
    }

    private static boolean isEmpty(String val) {
        return val == null || val.trim().isEmpty();
    }

    /** Append source to target merging and adjusting known metadata. */
    @SuppressWarnings("unchecked")
    private static void internalSpecialAppend(WithMeta<StringBuilder> target,
                                              WithMeta<StringBuilder> source) {
        final int targetLen = target.value().length();
        target.value().append(source.value()); // append the actual text
        // append the meta, creating new metas with adjusted offsets
        for (Map.Entry<Class, List<RangeMeta>> e : source.meta().entrySet()) {
            for (RangeMeta v : e.getValue()) {
                target.addMeta(e.getKey(),
                    new RangeMeta(v.offset + targetLen, v.len, v.val));
            }
        }
    }

}
