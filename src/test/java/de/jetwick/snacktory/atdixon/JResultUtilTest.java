package de.jetwick.snacktory.atdixon;

import de.jetwick.snacktory.JResult;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public final class JResultUtilTest {

    @Test
    public void testJResultUtil() {
        JResult jr; // resuable
        List<RangeMeta<Href>> metas;

        jr = new JResult();
        JResultUtil.trimTextToNull(jr);
        assertNull(jr.getTextOrNull());
        assertNull(jr.getTextMeta());

        jr = new JResult();
        jr.setText("");
        JResultUtil.trimTextToNull(jr);
        assertNull(jr.getTextOrNull());
        assertNull(jr.getTextMeta());

        jr = new JResult();
        jr.setTextMeta(Collections.<RangeMeta<Href>>emptyList());
        JResultUtil.trimTextToNull(jr);
        assertNull(jr.getTextOrNull());
        assertTrue(jr.getTextMeta().isEmpty());

        jr = new JResult();
        jr.setText("foo bar");
        metas = asList(
            new RangeMeta<>(3, 3, new Href("a", "foo://bar")),
            new RangeMeta<>(7, 2, new Href("a", "goo://far")));
        jr.setTextMeta(metas);
        JResultUtil.trimTextToNull(jr);
        assertEquals("foo bar", jr.getTextOrNull());
        assertEquals(metas, jr.getTextMeta());

        jr = new JResult();
        jr.setText(" foo bar  \n ");
        metas = asList(
            new RangeMeta<>(3, 3, new Href("a", "foo://bar")),
            new RangeMeta<>(7, 2, new Href("a", "goo://far")));
        jr.setTextMeta(metas);
        JResultUtil.trimTextToNull(jr);
        assertEquals("foo bar", jr.getTextOrNull());
        assertEquals(asList(
            new RangeMeta<>(2, 3, new Href("a", "foo://bar")),
            new RangeMeta<>(6, 1, new Href("a", "goo://far"))),
            jr.getTextMeta());

        jr = new JResult();
        jr.setText(" foo bar  \n ");
        metas = singletonList(
            new RangeMeta<>(10, 2, new Href("a", "goo://far")));
        jr.setTextMeta(metas);
        JResultUtil.trimTextToNull(jr);
        assertEquals("foo bar", jr.getTextOrNull());
        assertEquals(singletonList(
            new RangeMeta<>(7, 0, new Href("a", "goo://far"))),
            jr.getTextMeta());
    }

}
