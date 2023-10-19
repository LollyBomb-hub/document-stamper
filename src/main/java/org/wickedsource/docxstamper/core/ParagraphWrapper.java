package org.wickedsource.docxstamper.core;

import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.wickedsource.docxstamper.util.RunUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * A "Run" defines a region of text within a docx document with a common set of properties. Word processors are
 * relatively free in splitting a paragraph of text into multiple runs, so there is no strict rule to say over how many
 * runs a word or a string of words is spread.
 * <p/>
 * This class aggregates multiple runs so they can be treated as a single text, no matter how many runs the text spans.
 * Call addRun() to add all runs that should be aggregated. Then, call methods to modify the aggregated text. Finally,
 * call getText() or getRuns() to get the modified text or the list of modified runs.
 */
@SuppressWarnings("unused")
public class ParagraphWrapper {

    private int currentPosition = 0;

    private final List<IndexedRun> runs = new ArrayList<>();

    private final P paragraph;

    public ParagraphWrapper(P paragraph) {
        this.paragraph = paragraph;
        recalculateRuns();
    }

    public void recalculateRuns() {
        currentPosition = 0;
        this.runs.clear();
        int index = 0;
        for (Object contentElement : paragraph.getContent()) {
            if (contentElement instanceof R && !"".equals(RunUtil.getText((R) contentElement))) {
                this.addRun((R) contentElement, index);
            }
            index++;
        }
    }

    public R getRunByText(String text) {
        for (IndexedRun r: runs) {
            if (RunUtil.getText(r.getRun()).contains(text)) {
                return r.getRun();
            }
        }
        return null;
    }

    /**
     * Adds a run to the aggregation.
     *
     * @param run the run to add.
     */
    private void addRun(R run, int index) {
        int startIndex = currentPosition;
        int endIndex = currentPosition + RunUtil.getText(run).length() - 1;
        runs.add(new IndexedRun(startIndex, endIndex, index, run));
        currentPosition = endIndex + 1;
    }


    /**
     * Replaces the given placeholder String with the replacement object within the paragraph.
     * The replacement object must be a valid DOCX4J Object.
     *
     * @param placeholder the placeholder to be replaced.
     * @param replacement the object to replace the placeholder String.
     */
    public void replace(String placeholder, R replacement) {
        String text = getText();
        int matchStartIndex = text.indexOf(placeholder);
        if (matchStartIndex == -1) {
            // nothing to replace
            return;
        }
        int matchEndIndex = matchStartIndex + placeholder.length() - 1;
        List<IndexedRun> affectedRuns = getAffectedRuns(matchStartIndex, matchEndIndex);

        boolean singleRun = affectedRuns.size() == 1;

        if (singleRun) {
            IndexedRun run = affectedRuns.get(0);

            boolean placeholderSpansCompleteRun = placeholder.length() == RunUtil.getText(run.getRun()).length();
            boolean placeholderAtStartOfRun = matchStartIndex == run.getStartIndex();
            boolean placeholderAtEndOfRun = matchEndIndex == run.getEndIndex();
            boolean placeholderWithinRun = matchStartIndex > run.getStartIndex() && matchEndIndex < run.getEndIndex();

            if (placeholderSpansCompleteRun) {
                RPr rPr = run.getRun().getRPr();
                this.paragraph.getContent().remove(run.getRun());
                if (replacement != null) {
                    replacement.setRPr(rPr);
                    this.paragraph.getContent().add(run.getIndexInParent(), replacement);
                }
                recalculateRuns();
            } else if (placeholderAtStartOfRun) {
                RPr rPr = run.getRun().getRPr();
                run.replace(matchStartIndex, matchEndIndex, "");
                if (replacement != null) {
                    replacement.setRPr(rPr);
                    this.paragraph.getContent().add(run.getIndexInParent(), replacement);
                }
                recalculateRuns();
            } else if (placeholderAtEndOfRun) {
                RPr rPr = run.getRun().getRPr();
                run.replace(matchStartIndex, matchEndIndex, "");
                if (replacement != null) {
                    replacement.setRPr(rPr);
                    this.paragraph.getContent().add(run.getIndexInParent(), replacement);
                }
                recalculateRuns();
            } else if (placeholderWithinRun) {
                RPr rPr = run.getRun().getRPr();
                String runText = RunUtil.getText(run.getRun());
                int startIndex = runText.indexOf(placeholder);
                int endIndex = startIndex + placeholder.length();
                if (replacement != null) {
                    replacement.setRPr(rPr);
                    R run1 = RunUtil.create(runText.substring(0, startIndex), this.paragraph);
                    R run2 = RunUtil.create(runText.substring(endIndex), this.paragraph);
                    this.paragraph.getContent().add(run.getIndexInParent(), run2);
                    this.paragraph.getContent().add(run.getIndexInParent(), replacement);
                    this.paragraph.getContent().add(run.getIndexInParent(), run1);
                    this.paragraph.getContent().remove(run.getRun());
                }
                recalculateRuns();
            }

        } else {
            IndexedRun firstRun = affectedRuns.get(0);
            IndexedRun lastRun = affectedRuns.get(affectedRuns.size() - 1);

            // remove the placeholder from first and last run
            firstRun.replace(matchStartIndex, matchEndIndex, "");
            lastRun.replace(matchStartIndex, matchEndIndex, "");

            // remove all runs between first and last
            for (IndexedRun run : affectedRuns) {
                if (run != firstRun && run != lastRun) {
                    this.paragraph.getContent().remove(run.getRun());
                }
            }

            RPr rPr = firstRun.getRun().getRPr();

            // add replacement run between first and last run
            if (replacement != null) {
                replacement.setRPr(rPr);
                this.paragraph.getContent().add(firstRun.getIndexInParent() + 1, replacement);
            }

            recalculateRuns();
        }
    }

    public void replace(String placeholder, List<Object> replacementRuns) {
        String text = getText();
        int matchStartIndex = text.indexOf(placeholder);
        if (matchStartIndex == -1) {
            // nothing to replace
            return;
        }
        int matchEndIndex = matchStartIndex + placeholder.length() - 1;
        List<IndexedRun> affectedRuns = getAffectedRuns(matchStartIndex, matchEndIndex);

        boolean singleRun = affectedRuns.size() == 1;

        if (singleRun) {
            IndexedRun run = affectedRuns.get(0);

            boolean placeholderSpansCompleteRun = placeholder.length() == RunUtil.getText(run.getRun()).length();
            boolean placeholderAtStartOfRun = matchStartIndex == run.getStartIndex();
            boolean placeholderAtEndOfRun = matchEndIndex == run.getEndIndex();
            boolean placeholderWithinRun = matchStartIndex > run.getStartIndex() && matchEndIndex < run.getEndIndex();

            if (placeholderSpansCompleteRun) {
                RPr rPr = run.getRun().getRPr();
                replacementRuns.forEach(
                        r -> {
                            if (r instanceof R) {
                                ((R) r).setRPr(rPr);
                            }
                        }
                );
                this.paragraph.getContent().remove(run.getRun());
                this.paragraph.getContent().addAll(run.getIndexInParent(), replacementRuns);
                recalculateRuns();
            } else if (placeholderAtStartOfRun) {
                RPr rPr = run.getRun().getRPr();
                replacementRuns.forEach(
                        r -> {
                            if (r instanceof R) {
                                ((R) r).setRPr(rPr);
                            }
                        }
                );
                run.replace(matchStartIndex, matchEndIndex, "");
                this.paragraph.getContent().addAll(run.getIndexInParent(), replacementRuns);
                recalculateRuns();
            } else if (placeholderAtEndOfRun) {
                RPr rPr = run.getRun().getRPr();
                replacementRuns.forEach(
                        r -> {
                            if (r instanceof R) {
                                ((R) r).setRPr(rPr);
                            }
                        }
                );
                run.replace(matchStartIndex, matchEndIndex, "");
                this.paragraph.getContent().addAll(run.getIndexInParent() + 1, replacementRuns);
                recalculateRuns();
            } else if (placeholderWithinRun) {
                String runText = RunUtil.getText(run.getRun());
                int startIndex = runText.indexOf(placeholder);
                int endIndex = startIndex + placeholder.length();
                R run1 = RunUtil.create(runText.substring(0, startIndex), this.paragraph);
                R run2 = RunUtil.create(runText.substring(endIndex), this.paragraph);
                this.paragraph.getContent().add(run.getIndexInParent(), run2);
                RPr rPr = run.getRun().getRPr();
                replacementRuns.forEach(
                        r -> {
                            if (r instanceof R) {
                                ((R) r).setRPr(rPr);
                            }
                        }
                );
                this.paragraph.getContent().addAll(run.getIndexInParent(), replacementRuns);
                this.paragraph.getContent().add(run.getIndexInParent(), run1);
                this.paragraph.getContent().remove(run.getRun());
                recalculateRuns();
            }

        } else {
            IndexedRun firstRun = affectedRuns.get(0);
            IndexedRun lastRun = affectedRuns.get(affectedRuns.size() - 1);

            // remove the placeholder from first and last run
            firstRun.replace(matchStartIndex, matchEndIndex, "");
            lastRun.replace(matchStartIndex, matchEndIndex, "");

            // remove all runs between first and last
            for (IndexedRun run : affectedRuns) {
                if (run != firstRun && run != lastRun) {
                    this.paragraph.getContent().remove(run.getRun());
                }
            }

            RPr rPr = firstRun.getRun().getRPr();
            replacementRuns.forEach(
                    r -> {
                        if (r instanceof R) {
                            ((R) r).setRPr(rPr);
                        }
                    }
            );

            // add replacement run between first and last run
            this.paragraph.getContent().addAll(firstRun.getIndexInParent() + 1, replacementRuns);

            recalculateRuns();
        }
    }

    private List<IndexedRun> getAffectedRuns(int startIndex, int endIndex) {
        List<IndexedRun> affectedRuns = new ArrayList<>();
        for (IndexedRun run : runs) {
            if (run.isTouchedByRange(startIndex, endIndex)) {
                affectedRuns.add(run);
            }
        }
        return affectedRuns;
    }

    /**
     * Returns the aggregated text over all runs.
     *
     * @return the text of all runs.
     */
    public String getText() {
        return getText(this.runs);
    }

    private String getText(List<IndexedRun> runs) {
        StringBuilder builder = new StringBuilder();
        for (IndexedRun run : runs) {
            builder.append(RunUtil.getText(run.getRun()));
        }
        return builder.toString();
    }

    /**
     * Returns the list of runs that are aggregated. Depending on what modifications were done to the aggregated text
     * this list may not return the same runs that were initially added to the aggregator.
     *
     * @return the list of aggregated runs.
     */
    public List<R> getRuns() {
        List<R> resultList = new ArrayList<>();
        for (IndexedRun run : runs) {
            resultList.add(run.getRun());
        }
        return resultList;
    }

    @Override
    public String toString() {
        return getText();
    }

    public P getParagraph() {
        return paragraph;
    }
}
