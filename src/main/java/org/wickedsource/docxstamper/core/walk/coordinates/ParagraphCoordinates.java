package org.wickedsource.docxstamper.core.walk.coordinates;

import org.docx4j.XmlUtils;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.wickedsource.docxstamper.core.IndexedRun;
import org.wickedsource.docxstamper.model.Pair;
import org.wickedsource.docxstamper.util.RunUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class ParagraphCoordinates extends AbstractCoordinates {

    private final P paragraph;
    private ListIterator<Object> paragraphContentsIterator;

    private final ListIterator<Object> parentIterator;

    public ParagraphCoordinates(P paragraph, ListIterator<Object> parentIterator) {
        this.paragraph = paragraph;
        this.parentIterator = parentIterator;
    }

    public P getParagraph() {
        return paragraph;
    }

    public ListIterator<Object> getParentIterator() {
        return parentIterator;
    }

    public String getText() {
        paragraphContentsIterator = paragraph.getContent().listIterator();
        StringBuilder result = new StringBuilder();
        while (paragraphContentsIterator.hasNext()) {
            Object next = XmlUtils.unwrap(paragraphContentsIterator.next());
            if (next instanceof R) {
                result.append(RunUtil.getText((R) next));
            }
        }
        return result.toString();
    }

    public String getText(int from, int to) {
        return getText().substring(from, to);
    }

    public List<R> getRunsByIndexes(int fromRunIndex, int toRunIndex) {
        paragraphContentsIterator = paragraph.getContent().listIterator(fromRunIndex);
        List<R> result = new ArrayList<>();
        while (fromRunIndex <= toRunIndex && paragraphContentsIterator.hasNext()) {
            Object next = XmlUtils.unwrap(paragraphContentsIterator.next());
            if (next instanceof R) {
                fromRunIndex++;
                result.add((R) next);
            }
        }
        return result;
    }

    public Integer getSizeOfContents() {
        return paragraph.getContent() == null ? null : paragraph.getContent().size();
    }

    public Pair<Integer, Integer> getBeginAndCount(int begin, int end) {
        end--;
        paragraphContentsIterator = paragraph.getContent().listIterator();
        List<Pair<Integer, Pair<Integer, Integer>>> runsToSizingMappings = new ArrayList<>();
        int previousLastIndex = -1;
        int currentIndex = 0;
        Integer x = null;
        while (paragraphContentsIterator.hasNext()) {
            Object next = XmlUtils.unwrap(paragraphContentsIterator.next());

            if (next instanceof R) {
                int length = RunUtil.getText((R) next).length();
                runsToSizingMappings.add(Pair.of(currentIndex, Pair.of(previousLastIndex, previousLastIndex + length)));
                previousLastIndex = previousLastIndex + length;
            }

            currentIndex++;
        }

        for (Pair<Integer, Pair<Integer, Integer>> indexedRange: runsToSizingMappings) {
            Integer index = indexedRange.getFirst();
            Pair<Integer, Integer> range = indexedRange.getSecond();

            if (x == null) {
                if (range.getFirst() < begin && range.getSecond() >= begin) {
                    x = index;
                }
            } else {
                if (range.getFirst() < end && range.getSecond() >= end) {
                    return Pair.of(x, index - x + 1);
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "ParagraphCoordinates{" +
                "paragraph=" + paragraph +
                ", hashcode=" + paragraph.hashCode() +
                '}';
    }
}
