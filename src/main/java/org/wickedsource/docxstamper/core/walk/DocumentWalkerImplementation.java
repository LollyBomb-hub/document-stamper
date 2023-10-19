package org.wickedsource.docxstamper.core.walk;

import lombok.extern.slf4j.Slf4j;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.wickedsource.docxstamper.core.walk.coordinates.ParagraphCoordinates;
import org.wickedsource.docxstamper.model.Pair;
import org.wickedsource.docxstamper.model.Triplet;
import org.wickedsource.docxstamper.resolver.context.language.ExpressionResolver;
import org.wickedsource.docxstamper.util.ExpressionUtil;
import org.wickedsource.docxstamper.util.RunUtil;

import java.util.List;
import java.util.ListIterator;
import java.util.Stack;


@Slf4j
public abstract class DocumentWalkerImplementation<T> extends BaseCoordinatesWalker {

    private final ExpressionUtil expressionUtil;
    private final ExpressionResolver expressionResolver;
    private final T expressionContext;
    private final Stack<String> stackOfTrueConditionals = new Stack<>();
    private Integer currentHidingBeginIndexInText = null;
    private String currentHidingPlaceholder = null;
    private Triplet<Integer, Integer, ParagraphCoordinates> currentHidingCondition = null;

    public DocumentWalkerImplementation(WordprocessingMLPackage document, ExpressionUtil expressionUtil, ExpressionResolver expressionResolver, T expressionContext) {
        super(document);
        this.expressionUtil = expressionUtil;
        this.expressionResolver = expressionResolver;
        this.expressionContext = expressionContext;
    }

    @Override
    protected void processConditionals(ListIterator<Object> documentContentIterator) {
        while (documentContentIterator.hasNext()) {
            Object contentElement = documentContentIterator.next();
            Object unwrappedObject = XmlUtils.unwrap(contentElement);
            if (unwrappedObject instanceof P) {
                P p = (P) unwrappedObject;
                ParagraphCoordinates paragraphCoordinates = new ParagraphCoordinates(p, documentContentIterator);

                log.debug("Current paragraph contents: {}", paragraphCoordinates.getText());

                if (currentHidingPlaceholder != null) {
                    log.debug("Current hiding placeholder {}", currentHidingPlaceholder);
                }

                log.debug("Processing closing conditionals");
                processClosing(paragraphCoordinates);
                log.debug("Current paragraph contents: {}", paragraphCoordinates.getText());
                log.debug("Processing true conditionals");
                processTrueConditionals(paragraphCoordinates);
                log.debug("Current paragraph contents: {}", paragraphCoordinates.getText());
                log.debug("ProcessingOpeningConditionals");
                // Processed current paragraph or breaked when found falsed one conditional
                processOpeningConditionals(paragraphCoordinates);
                log.debug("Current paragraph contents: {}", paragraphCoordinates.getText());
                log.debug("Processing true conditionals");
                processTrueConditionals(paragraphCoordinates);
                log.debug("Current paragraph contents: {}", paragraphCoordinates.getText());
                log.debug("Processing closing conditionals");
                processClosing(paragraphCoordinates);
                log.debug("Current paragraph contents: {}", paragraphCoordinates.getText());

            } else if (currentHidingPlaceholder != null && currentHidingCondition != null) {
                documentContentIterator.remove();
            }
        }
    }

    private void processClosing(ParagraphCoordinates paragraphCoordinates) {

        String text = paragraphCoordinates.getText();

        log.debug("Processing closing conditionals on text {}", text);

        if (currentHidingPlaceholder != null && currentHidingCondition != null) {
            // If not collapsed conditional block
            // if not changed paragraph to next
            log.debug("Hiding placeholder is not null -> {}", currentHidingPlaceholder);
            Pair<Integer, Integer> searchForClosingPlaceholder = expressionUtil.findClosingExpressionInTextByOpeningPlaceholder(text, currentHidingPlaceholder);
            if (searchForClosingPlaceholder != null) {
                // found in current paragraph closing expression
                log.debug("Found closing expression");
                Integer startOfClosingExpression = searchForClosingPlaceholder.getFirst();
                Integer endOfClosingExpression = searchForClosingPlaceholder.getSecond();

                // Coordinates of closing expression
                Pair<Integer, Integer> beginAndCount = paragraphCoordinates.getBeginAndCount(startOfClosingExpression, endOfClosingExpression);

                if (currentHidingCondition.getThird().equals(paragraphCoordinates)) {
                    // If same paragraph
                    // Removing all between currentHidingCondition to current closing condition
                    log.debug("Got closed in same paragraph");
                    String textToRemove = paragraphCoordinates.getText(currentHidingBeginIndexInText, endOfClosingExpression);
                    log.debug("Removing text {}", textToRemove);
                    removeAllBetween(paragraphCoordinates, currentHidingCondition.getFirst(), beginAndCount.getFirst() + beginAndCount.getSecond(), textToRemove);
                } else {
                    // if not same paragraph
//                                        removeTill(paragraphCoordinates, beginAndCount.getFirst() + beginAndCount.getSecond());
                    log.debug("Not closed in same paragraph");
                    String textToRemove = paragraphCoordinates.getText(0, endOfClosingExpression);
                    removeAllBetween(paragraphCoordinates, 0, beginAndCount.getFirst() + beginAndCount.getSecond(), textToRemove);
                }
                // Clearing things that manage hiding behaviour
                currentHidingCondition = null;
                currentHidingPlaceholder = null;
            } else {
                // if not found closing
                log.debug("Not found closing expression in given paragraph!");
                if (currentHidingCondition.getThird().equals(paragraphCoordinates)) {
//                                        removeFrom(paragraphCoordinates, currentHidingCondition.getFirst());
//                    String textToRemove = paragraphCoordinates.getText(currentHidingBeginIndexInText, paragraphCoordinates.getText().length());
                    removeAllBetween(paragraphCoordinates, currentHidingCondition.getFirst(), paragraphCoordinates.getSizeOfContents());
                } else {
                    removeParagraph(paragraphCoordinates);
                }
            }
        }
    }

    private void processOpeningConditionals(ParagraphCoordinates paragraphCoordinates) {
        int fromIndex = 0;

        String text = paragraphCoordinates.getText();
        List<String> openingConditionalExpressions = expressionUtil.findOpeningConditionalExpressions(text);

        log.debug("Processing opening conditionals in text: {}", text);

        // Processing current paragraph conditionals
        for (String openingPlaceholder : openingConditionalExpressions) {
            log.debug("Searching for opening placeholder {}", openingPlaceholder);
            Object givenValue = expressionResolver.resolveConditionalExpression(openingPlaceholder, expressionContext);
            if (givenValue instanceof Boolean) {
                log.debug("Given placeholder {} ==== got ====> {}\n", openingPlaceholder, givenValue);
                if (!((Boolean) givenValue)) {
                    // Found falsed conditional
                    fromIndex = text.indexOf(openingPlaceholder, fromIndex);
                    Pair<Integer, Integer> beginAndCount = paragraphCoordinates.getBeginAndCount(fromIndex, fromIndex + openingPlaceholder.length());

                    Integer indexOfRun = beginAndCount.getFirst();
                    Integer count = beginAndCount.getSecond();

                    // Saved it
                    currentHidingCondition = Triplet.of(indexOfRun, count, paragraphCoordinates);
                    log.debug("Found hiding condition {} from index {}", currentHidingCondition, fromIndex);
                    currentHidingPlaceholder = openingPlaceholder;
                    currentHidingBeginIndexInText = fromIndex;

                    Pair<Integer, Integer> closingExpression = expressionUtil.findClosingExpressionInTextByOpeningPlaceholder(text, openingPlaceholder);

                    if (closingExpression != null) {
                        // Found in this paragraph
                        // Then I must delete all between opening expression and closing expression
                        // And continue processing
                        log.debug("Found closing expression in given paragraph! Deleting");
                        processClosing(paragraphCoordinates);
                        text = paragraphCoordinates.getText();
                    } else {
                        break;
                    }
                } else {
                    // Found trued conditional
                    // Must clean its opening statement and closing one

                    // moving fromIndex
                    fromIndex = text.indexOf(openingPlaceholder, fromIndex);
                    // Getting runs responsible for this placeholder
                    Pair<Integer, Integer> beginAndCount = paragraphCoordinates.getBeginAndCount(fromIndex, fromIndex + openingPlaceholder.length());

                    Integer indexOfRun = beginAndCount.getFirst();
                    Integer count = beginAndCount.getSecond();

                    // Deleting placeholder from paragraph at its coordinates
                    removeAllBetween(paragraphCoordinates, indexOfRun, indexOfRun + count, openingPlaceholder);

                    // Update current state of text
                    log.debug("Found true condition {}. Removing from {}, index of run {}, count {}", openingPlaceholder, fromIndex, indexOfRun, count);
                    text = paragraphCoordinates.getText();
                    log.debug("Removed {}", text);
                    // Attempt to find closing expression
                    Pair<Integer, Integer> closingExpressionInTextByPlaceholder = expressionUtil.findClosingExpressionInTextByOpeningPlaceholder(text, openingPlaceholder);

                    if (closingExpressionInTextByPlaceholder != null) {
                        // if found closing one here
                        // removing it
                        log.debug("Found closing expression for {} in same paragraph", openingPlaceholder);
                        beginAndCount = paragraphCoordinates.getBeginAndCount(closingExpressionInTextByPlaceholder.getFirst(), closingExpressionInTextByPlaceholder.getSecond());

                        // indexOfRun where first occurrence found
                        indexOfRun = beginAndCount.getFirst();
                        // Count of runs containing this placeholder
                        count = beginAndCount.getSecond();

                        log.debug("Removing for {}. Removing from {}, index of run {}, count {}", openingPlaceholder, fromIndex, indexOfRun, count);
                        removeAllBetween(paragraphCoordinates, indexOfRun, indexOfRun + count, expressionUtil.getClosingConditionalExpressionByPlaceholder(openingPlaceholder));
                    } else {
                        log.debug("Not found closing conditional. Saving for further search");
                        // if not found
                        // saving it for future search
                        stackOfTrueConditionals.push(expressionUtil.stripConditionalExpression(openingPlaceholder));
                    }
                }
            }
        }
    }

    private void processTrueConditionals(ParagraphCoordinates paragraphCoordinates) {
        String text = paragraphCoordinates.getText();

        if (!stackOfTrueConditionals.empty()) {
            // if not empty
            String latestTrueConditionalMarker = stackOfTrueConditionals.peek();
            Pair<Integer, Integer> closingExpressionInText = expressionUtil.findClosingExpressionInText(text, latestTrueConditionalMarker);
            while (closingExpressionInText != null) {
                // if found closing
                // Delete this expression
                stackOfTrueConditionals.pop();

                // Getting runs responsible for this placeholder
                Pair<Integer, Integer> beginAndCount = paragraphCoordinates.getBeginAndCount(closingExpressionInText.getFirst(), closingExpressionInText.getSecond());

                Integer indexOfRun = beginAndCount.getFirst();
                Integer count = beginAndCount.getSecond();

                // Removing closing expression from paragraph
                removeAllBetween(paragraphCoordinates, indexOfRun, indexOfRun + count, expressionUtil.getClosingConditionalExpressionByMarker(latestTrueConditionalMarker));

                if (stackOfTrueConditionals.empty()) {
                    // will exit
                    closingExpressionInText = null;
                } else {
                    latestTrueConditionalMarker = stackOfTrueConditionals.peek();
                    closingExpressionInText = expressionUtil.findClosingExpressionInText(text, latestTrueConditionalMarker);
                }
            }

        }
    }

    private String commonMaxSubString(String largerString, String smallerString) {
        StringBuilder maxstr = new StringBuilder();
        int largeIndex;
        int smallIndex = 0;
        int len = largerString.length();
        for (largeIndex = 0; largeIndex < len && smallIndex < smallerString.length(); largeIndex++) {
            if (largerString.charAt(largeIndex) == smallerString.charAt(smallIndex)) {
                // Collision
                smallIndex++;
                maxstr.append(largerString.charAt(largeIndex));
            } else {
                if (maxstr.length() != 0) {
                    return maxstr.toString();
                }
            }
        }
        return maxstr.toString();
    }

    private void removeParagraph(ParagraphCoordinates paragraphCoordinates) {
        paragraphCoordinates.getParentIterator().remove();
    }

    private void removeAllBetween(ParagraphCoordinates paragraphCoordinates, int from, int to, String textToRemove) {

        log.debug("Removing text {} from: {} to: {}", textToRemove, from, to);

        ListIterator<Object> objectListIterator = paragraphCoordinates.getParagraph().getContent().listIterator(from);

        while (to - from > 0 && objectListIterator.hasNext()) {
            Object next = XmlUtils.unwrap(objectListIterator.next());
            to--;
            if (next instanceof R) {
                // Getting text from current R object
                String text = RunUtil.getText((R) next);
                log.debug("Processing text, while removing: {}", text);
                // Most common string
                String mostCommonString = commonMaxSubString(text, textToRemove);
                log.debug("Most common string {}", mostCommonString);

                if (mostCommonString.length() == text.length()) {
                    // Fully succeeded search!
                    // removing non-necessary R object
                    log.debug("Fully succeeded search");
                    objectListIterator.remove();
                } else {
                    int indexOfMostCommonString = text.indexOf(mostCommonString);
                    String s = text.substring(0, indexOfMostCommonString) + text.substring(indexOfMostCommonString + mostCommonString.length());
                    RunUtil.setText((R) next, s);
                }
                // Updating text to remove
                textToRemove = textToRemove.substring(textToRemove.indexOf(mostCommonString) + mostCommonString.length());
            } else {
                objectListIterator.remove();
            }
        }
        paragraphCleanUp(paragraphCoordinates);

        log.debug("After removing got : {}", paragraphCoordinates.getText());
    }

    private void paragraphCleanUp(ParagraphCoordinates paragraphCoordinates) {
        if (paragraphCoordinates.getText().trim().length() == 0) {
            paragraphCoordinates.getParentIterator().remove();
        }
    }

    private void removeAllBetween(ParagraphCoordinates paragraphCoordinates, int from, int to) {
        List<R> runsByIndexes = paragraphCoordinates.getRunsByIndexes(from, to);
        String text = RunUtil.getText(runsByIndexes);

        removeAllBetween(paragraphCoordinates, from, to, text);
    }
}
