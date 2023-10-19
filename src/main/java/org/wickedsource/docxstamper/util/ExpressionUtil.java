package org.wickedsource.docxstamper.util;

import org.wickedsource.docxstamper.model.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/*
This class works with expressions
 */
@SuppressWarnings("unused")
public class ExpressionUtil {

    /**
     * Finds all variable expressions in a text and returns them as list. Example expression: "${myObject.property}".
     *
     * @param text the text to find expressions in.
     * @return a list of expressions (including the starting "${" and trailing "}").
     */
    public List<String> findVariableExpressions(String text) {
        return findExpressions(text, "\\$\\{.*?\\}");
    }

    public List<String> findOpeningConditionalExpressions(String text) {
        return findExpressions(text, "\\$if\\{.*?\\}");
    }

    public String getClosingConditionalExpressionByMarker(String marker) {
        return "$endIf{" + marker + "}";
    }

    public String getClosingConditionalPatternByMarker(String marker) {
        return "\\$endIf\\{" + marker + "}";
    }

    public String getClosingConditionalExpressionByPlaceholder(String placeholder) {
        Pattern markerPattern = Pattern.compile("\\$if\\{(.*)}");
        Matcher markerMatcher = markerPattern.matcher(placeholder);

        if (!markerMatcher.matches() || markerMatcher.groupCount() != 1) {
            return null;
        }

        String marker = markerMatcher.group(1);

        return getClosingConditionalExpressionByMarker(marker);
    }

    public Pair<Integer, Integer> findClosingExpressionInText(String text, String marker) {
        String closingConditionalExpressionByMarker = getClosingConditionalPatternByMarker(marker);

        Pattern pattern = Pattern.compile(closingConditionalExpressionByMarker);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return Pair.of(matcher.start(), matcher.end());
        }

        return null;
    }

    public Pair<Integer, Integer> findClosingExpressionInTextByOpeningPlaceholder(String text, String placeholder) {
        Pattern markerPattern = Pattern.compile("\\$if\\{(.*)}");
        Matcher markerMatcher = markerPattern.matcher(placeholder);

        if (!markerMatcher.matches() || markerMatcher.groupCount() != 1) {
            return null;
        }

        String marker = markerMatcher.group(1);

        return findClosingExpressionInText(text, marker);
    }

    /**
     * Finds all processor expressions in a text and returns them as list. Example expression: "#{myObject.property}".
     *
     * @param text the text to find expressions in.
     * @return a list of expressions (including the starting "#{" and trailing "}").
     */
    public List<String> findProcessorExpressions(String text) {
        return findExpressions(text, "\\#\\{.*?\\}");
    }

    private List<String> findExpressions(String text, String expressionPattern) {
        List<String> matches = new ArrayList<>();
        if ("".equals(text) || text == null) {
            return matches;
        }
        Pattern pattern = Pattern.compile(expressionPattern);
        Matcher matcher = pattern.matcher(text);
        int index = 0;
        while (matcher.find(index)) {
            String match = matcher.group();
            matches.add(match);
            index = matcher.end();
        }
        return matches;
    }

    /**
     * Strips an expression of the leading "${" or "#{" and the trailing "}".
     *
     * @param expression the expression to strip.
     * @return the expression without the leading "${" or "#{" and the trailing "}".
     */
    public String stripExpression(String expression) {
        if (expression == null) {
            throw new IllegalArgumentException("Cannot strip NULL expression!");
        }
        expression = expression.replaceAll("^\\$\\{", "").replaceAll("}$", "");
        expression = expression.replaceAll("^#\\{", "").replaceAll("}$", "");
        return expression;
    }

    public String stripConditionalExpression(String expression) {
        if (expression == null) {
            throw new IllegalArgumentException("Cannot strip NULL expression!");
        }
        expression = expression.replaceAll("^\\$if\\{", "").replaceAll("}$", "");
        return expression;
    }

    public String getParent(String expression) {
        String[] path = expression.split("\\.");
        return path.length > 1 ? path[path.length - 2] : null;
    }

}
