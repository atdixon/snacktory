package de.jetwick.snacktory.atdixon;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class SpecialOutputFormatterTest {

    @Test
    public void testSimpleClipScenarios() {
        // reuseables
        List<RangeMeta<String>> r;
        List<RangeMeta<String>> metas;
        List<Integer[]> clips;
        RangeMeta<String> rm;

        // SCENARIO -- empty everything
        metas = new ArrayList<>();
        clips = new ArrayList<>();
        r = SpecialOutputFormatter.clip(metas, clips);
        assertTrue(r.isEmpty());

        // SCENARIO -- clip one BEFORE meta
        metas = new ArrayList<>();
        metas.add(new RangeMeta<>(50, 25, "foo"));
        clips = new ArrayList<>();
        clips.add(new Integer[] { 0, 25 });
        r = SpecialOutputFormatter.clip(metas, clips);
        assertEquals(r.size(), 1);
        rm = r.iterator().next();
        assertEquals(new RangeMeta<>(25, 25, "foo"), rm);

        // SCENARIO -- clip one AFTER meta
        metas = new ArrayList<>();
        metas.add(new RangeMeta<>(0, 25, "foo"));
        clips = new ArrayList<>();
        clips.add(new Integer[] { 50, 25 });
        r = SpecialOutputFormatter.clip(metas, clips);
        assertEquals(r.size(), 1);
        rm = r.iterator().next();
        assertEquals(new RangeMeta<>(0, 25, "foo"), rm);

        // SCENARIO -- clip one OVERLAP FIRST HALF of meta
        metas = new ArrayList<>();
        metas.add(new RangeMeta<>(25, 25, "foo"));
        clips = new ArrayList<>();
        clips.add(new Integer[] { 10, 25 });
        r = SpecialOutputFormatter.clip(metas, clips);
        assertEquals(r.size(), 1);
        rm = r.iterator().next();
        assertEquals(new RangeMeta<>(10, 15, "foo"), rm);

        // SCENARIO -- clip one OVERLAP LAST HALF of meta
        metas = new ArrayList<>();
        metas.add(new RangeMeta<>(10, 25, "foo"));
        clips = new ArrayList<>();
        clips.add(new Integer[] { 25, 25 });
        r = SpecialOutputFormatter.clip(metas, clips);
        assertEquals(r.size(), 1);
        rm = r.iterator().next();
        assertEquals(new RangeMeta<>(10, 15, "foo"), rm);

        // SCENARIO -- clip ranges exact -- all but last
        metas = new ArrayList<>();
        for (int i = 0, ch = (int) 'a'; i < 100; i += 10, ++ch)
            metas.add(new RangeMeta<>(i, 10, String.valueOf((char) ch)));
        // assert: metas = [[0, 10, "a"] [10, 10, "b"] ... [80, 10, "i"]]
        clips = new ArrayList<>();
        for (int i = 0; i < 90; i += 10)
            clips.add(new Integer[] { i, 10 });
        // assert: clips = "
        r = SpecialOutputFormatter.clip(metas, clips);
        assertEquals(r.size(), metas.size());
        assertEquals(asList(
            new RangeMeta<>(0, 0, "a"),
            new RangeMeta<>(0, 0, "b"),
            new RangeMeta<>(0, 0, "c"),
            new RangeMeta<>(0, 0, "d"),
            new RangeMeta<>(0, 0, "e"),
            new RangeMeta<>(0, 0, "f"),
            new RangeMeta<>(0, 0, "g"),
            new RangeMeta<>(0, 0, "h"),
            new RangeMeta<>(0, 0, "i"),
            new RangeMeta<>(0, 10, "j")), r);

        // SCENARIO -- clip most everything with single clip
        metas = new ArrayList<>();
        for (int i = 3, ch = (int) 'a'; i < 100; i += 10, ++ch)
            metas.add(new RangeMeta<>(i, 8, String.valueOf((char) ch)));
        // assert: metas = [[3, 8, "a"] [13, 8, "b"] ... [83, 8, "i"] [93, 8, "j"]]
        clips = new ArrayList<>();
        clips.add(new Integer[] { 2, 85 }); // [2, 87)
        // assert: clips = "
        r = SpecialOutputFormatter.clip(metas, clips);
        assertEquals(r.size(), metas.size());
        assertEquals(asList(
            new RangeMeta<>(2, 0, "a"),
            new RangeMeta<>(2, 0, "b"),
            new RangeMeta<>(2, 0, "c"),
            new RangeMeta<>(2, 0, "d"),
            new RangeMeta<>(2, 0, "e"),
            new RangeMeta<>(2, 0, "f"),
            new RangeMeta<>(2, 0, "g"),
            new RangeMeta<>(2, 0, "h"),
            new RangeMeta<>(2, 4, "i"),
            new RangeMeta<>(8, 8, "j")), r);
    }

    @Test
    public void testTrickyClipScenario1() {
        // reuseables
        List<RangeMeta<String>> r;
        List<RangeMeta<String>> metas;
        List<Integer[]> clips;
        RangeMeta<String> rm;

        // SCENARIO -- madhouse 1: several clips within ranges
        metas = new ArrayList<>();
        for (int i = 0, ch = (int) 'a'; i < 100; i += 20, ++ch)
            metas.add(new RangeMeta<>(i, 10, String.valueOf((char) ch)));
        // assert: metas = [[0, 10] [20, 10] [40, 10] [60, 10] [80, 10]]
        clips = new ArrayList<>();
        for (int i = 5; i < 100; i += 10)
            clips.add(new Integer[] { i, 5 });
        // assert: clips = [[5, 5] [15, 5] [25, 5] [35, 5] [45, 5] [55, 5] ... [95, 5]]
        r = SpecialOutputFormatter.clip(metas, clips);
        assertEquals(r.size(), metas.size());
        // expected: [[0, 5] ...
        assertEquals(asList(
            new RangeMeta<>(0, 5, "a"),
            new RangeMeta<>(10, 5, "b"),
            new RangeMeta<>(20, 5, "c"),
            new RangeMeta<>(30, 5, "d"),
            new RangeMeta<>(40, 5, "e")
        ), r);
    }

    @Test
    public void testTrickyClipScenario2() {
        // reuseables
        List<RangeMeta<String>> r;
        List<RangeMeta<String>> metas;
        List<Integer[]> clips;
        RangeMeta<String> rm;

        // SCENARIO -- madhouse 2: clips just swallowing ranges
        metas = new ArrayList<>();
        for (int i = 0, ch = (int) 'a'; i < 100; i += 20, ++ch)
            metas.add(new RangeMeta<>(i, 10, String.valueOf((char) ch)));
        // assert: metas = [[0, 10] [20, 10] [40, 10] [60, 10] [80, 10]]
        clips = new ArrayList<>();
        for (int i = 0; i < 100; i += 20)
            clips.add(new Integer[] { i, 15 });
        // assert: clips = [[0, 15] [20, 15] [40, 15] [60, 15] [80, 15]]
        r = SpecialOutputFormatter.clip(metas, clips);
        assertEquals(r.size(), metas.size());
        assertEquals(asList(
            new RangeMeta<>(0, 0, "a"),
            new RangeMeta<>(5, 0, "b"),
            new RangeMeta<>(10, 0, "c"),
            new RangeMeta<>(15, 0, "d"),
            new RangeMeta<>(20, 0, "e")
        ), r);
    }

    @Test
    public void testTrickyClipScenario3() {
        // reuseables
        List<RangeMeta<String>> r;
        List<RangeMeta<String>> metas;
        List<Integer[]> clips;
        RangeMeta<String> rm;

        // SCENARIO -- madhouse 3: clips swallowing multiple ranges
        metas = new ArrayList<>();
        for (int i = 5, ch = (int) 'a'; i < 100; i += 20, ++ch)
            metas.add(new RangeMeta<>(i, 10, String.valueOf((char) ch)));
        // assert: metas = [[5, 10, "a"] [25, 10, "b"] [45, 10, "c"] [65, 10, "d"] [85, 10, "e"]]
        clips = new ArrayList<>();
        for (int i = 1; i < 100; i += 30)
            clips.add(new Integer[]{i, 25});
        // assert: clips = [[1, 25] [31, 25] [61, 25] [91, 25]]
        r = SpecialOutputFormatter.clip(metas, clips);
        assertEquals(r.size(), metas.size());
        assertEquals(asList(
            new RangeMeta<>(1, 0, "a"),
            new RangeMeta<>(1, 5, "b"),
            new RangeMeta<>(6, 0, "c"),
            new RangeMeta<>(11, 0, "d"),
            new RangeMeta<>(11, 5, "e")
        ), r);
    }

}
