package de.jetwick.snacktory.atdixon;

import java.util.Objects;

public final class Href {

    public final String tagName;
    public final String href;

    public Href(String tagName, String href) {
        this.tagName = tagName;
        this.href = href;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Href href1 = (Href) o;
        return Objects.equals(tagName, href1.tagName) &&
            Objects.equals(href, href1.href);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tagName, href);
    }

    @Override
    public String toString() {
        return "Href(" + tagName + "," + href + ")";
    }
}
