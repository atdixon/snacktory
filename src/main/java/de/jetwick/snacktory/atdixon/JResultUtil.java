package de.jetwick.snacktory.atdixon;

import de.jetwick.snacktory.JResult;

import java.util.List;

import static java.util.Arrays.asList;

public final class JResultUtil {

    private JResultUtil() {}

    /** Mutates jr, trimming text to null (and making corresponding text meta updates.) */
    public static void trimTextToNull(JResult jr) {
        final String text = jr.getText();

        if (text == null)
            return;

        int i = 0;
        while (i < text.length() && Character.isWhitespace(text.charAt(i))) {
            ++i;
        }
        int j = text.length() - 1;
        while (j >= 0 && Character.isWhitespace(text.charAt(j))) {
            --j;
        }
        final List<Integer[]> clips = asList(
            new Integer[] { 0, i },
            new Integer[] { j + 1, text.length() - j - 1 }
        );

        final String trimmed = text.substring(i, j + 1);
        jr.setText(trimmed.length() == 0 ? null : trimmed);
        if (jr.getTextMeta() != null)
            jr.setTextMeta(InternalUtil.clip(jr.getTextMeta(), clips));
    }

}
