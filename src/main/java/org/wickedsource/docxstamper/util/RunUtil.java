package org.wickedsource.docxstamper.util;

import lombok.NonNull;
import org.docx4j.TextUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.model.styles.StyleUtil;
import org.docx4j.wml.*;

import javax.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.List;

public class RunUtil {

    private static final ObjectFactory factory = Context.getWmlObjectFactory();

    private RunUtil() {
    }

    public static String getText(@NonNull Tc cell) {
        StringBuilder result = new StringBuilder();
        List<Object> content = cell.getContent();
        for (Object e : content) {
            if (e instanceof JAXBElement) {
                JAXBElement<?> element = (JAXBElement<?>) e;
                Object value = element.getValue();
                if (value instanceof P || value instanceof R) {
                    result.append(TextUtils.getText(value));
                }
            } else {
                result.append(TextUtils.getText(e));
            }
        }
        return result.toString();
    }

    /**
     * Returns the text string of a run.
     *
     * @param run the run whose text to get.
     * @return String representation of the run.
     */
    public static String getText(R run) {
        StringBuilder result = new StringBuilder();
        for (Object content : run.getContent()) {
            if (content instanceof JAXBElement) {
                JAXBElement<?> element = (JAXBElement<?>) content;
                if (element.getValue() instanceof Text) {
                    Text textObj = (Text) element.getValue();
                    String text = textObj.getValue();
                    if (!"preserve".equals(textObj.getSpace())) {
                        // trimming text if spaces are not to be preserved (simulates behavior of Word; LibreOffice seems
                        // to ignore the "space" property and always preserves spaces)
                        text = text.trim();
                    }
                    result.append(text);
                } else if (element.getValue() instanceof R.Tab) {
                    result.append("\t");
                }
            } else if (content instanceof Text) {
                result.append(((Text) content).getValue());
            }
        }
        return result.toString();
    }

    public static String getText(List<R> runs) {
        StringBuilder result = new StringBuilder();
        for (R run : runs) {
            result.append(getText(run));
        }
        return result.toString();
    }

    /**
     * Applies the style of the given paragraph to the given content object (if the content object is a Run).
     *
     * @param p   the paragraph whose style to use.
     * @param run the Run to which the style should be applied.
     */
    public static void applyParagraphStyle(P p, R run) {
        if (p.getPPr() != null && p.getPPr().getRPr() != null) {
            RPr runProperties = new RPr();
            StyleUtil.apply(p.getPPr().getRPr(), runProperties);
            run.setRPr(runProperties);
        }
    }

    public static void applyRPR(R run, RPr rpr) {
        if (rpr != null && run != null) {
            run.setRPr(rpr);
        }
    }

    /**
     * Sets the text of the given run to the given value.
     *
     * @param run  the run whose text to change.
     * @param text the text to set.
     */
    public static void setText(R run, String text) {
        run.getContent().clear();
        Text textObj = factory.createText();
        textObj.setSpace("preserve");
        textObj.setValue(text);
        textObj.setSpace("preserve"); // make the text preserve spaces
        run.getContent().add(textObj);
    }

    /**
     * Creates a new run with the specified text.
     *
     * @param text the initial text of the run.
     * @return the newly created run.
     */
    public static R create(String text) {
        R run = factory.createR();
        setText(run, text);
        return run;
    }

    /**
     * Creates a new run with the given object as content.
     *
     * @param content the content of the run.
     * @return the newly created run.
     */
    public static R create(Object content) {
        R run = factory.createR();
        Text t = new Text();
        if (content != null) {
            t.setValue(content.toString());
        } else {
            t.setValue("null");
        }
        run.getContent().add(t);
        return run;
    }

    /**
     * Creates a new run with the specified text and inherits the style of the parent paragraph.
     *
     * @param text            the initial text of the run.
     * @param parentParagraph the parent paragraph whose style to inherit.
     * @return the newly created run.
     */
    public static R create(String text, P parentParagraph) {
        R run = create(text);
        applyParagraphStyle(parentParagraph, run);
        return run;
    }

    public static List<Object> createReplaceForString(String replacement) {
        if (replacement == null) {
            replacement = "";
        }
        List<Object> textRs = new ArrayList<>();
        int index = 0;
        for (char c : replacement.toCharArray()) {
            if (c == ' ') {
                R spaceR = new R();
                Text spaceT = new Text();
                spaceT.setSpace("preserve");
                spaceT.setValue(" ");
                spaceR.getContent().add(spaceT);
                textRs.add(spaceR);
            } else if (c == '\t') {
                R runTab = new R();
                runTab.getContent().add(new R.Tab());
                textRs.add(runTab);
            } else {
                break;
            }
            index++;
        }
        textRs.add(create(replacement.substring(index)));
        return textRs;
    }
}
