package de.jetwick.snacktory.atdixon;

import de.jetwick.snacktory.ArticleTextExtractor;
import de.jetwick.snacktory.ImageResult;
import de.jetwick.snacktory.JResult;
import de.jetwick.snacktory.OutputFormatter;
import de.jetwick.snacktory.SHelper;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;

public final class SpecialArticleTextExtractor extends ArticleTextExtractor {

    public SpecialArticleTextExtractor() {
        super();
        setOutputFormatter(new SpecialOutputFormatter());
    }

    protected void handleBestMatchElement(JResult res, OutputFormatter formatter, Element bestMatchElement) {
        final SpecialOutputFormatter asSpecialFormatter = (SpecialOutputFormatter) formatter;
        if (bestMatchElement != null) {
            List<ImageResult> images = new ArrayList<ImageResult>();
            Element imgEl = determineImageSource(bestMatchElement, images);
            if (imgEl != null) {
                res.setImageUrl(SHelper.replaceSpaces(imgEl.attr("src")));
                // TODO remove parent container of image if it is contained in bestMatchElement
                // to avoid image subtitles flooding in

                res.setImages(images);
            }

            // clean before grabbing text
            WithMeta<String> text = asSpecialFormatter._getFormattedText(bestMatchElement);
            //text = removeTitleFromText(text, res.getTitle());
            // this fails for short facebook post and probably tweets: text.length() > res.getDescription().length()
            if (text.value().length() > res.getTitle().length()) {
                res.setText(text.value());
                res.setTextMeta(text.meta());
//                print("best element:", bestMatchElement);
            }
            res.setTextList(formatter.getTextList(bestMatchElement));

            res.setBestMatchElement(bestMatchElement);
        }
    }

}
