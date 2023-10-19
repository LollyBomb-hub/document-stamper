package org.wickedsource.docxstamper.util;

import org.docx4j.wml.*;
import org.wickedsource.docxstamper.core.ParagraphWrapper;

import javax.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.List;

public class StyleUtils {


    public static void applyStyle(List<Object> targetContents, List<Object> sourceContents) {
        if (sourceContents != null && targetContents != null) {
            if (targetContents.size() == sourceContents.size()) {
                if (targetContents.size() != 0) {
                    for (int elementIndex = 0; elementIndex < sourceContents.size(); elementIndex++) {

                        Object targetObjectElement = targetContents.get(elementIndex);
                        Object sourceObjectElement = sourceContents.get(elementIndex);

                        applyStyle(targetObjectElement, sourceObjectElement);
                    }
                }
            }
        }
    }

    public static List<Tc> applyStyle(List<Tc> row, List<Tc> style, int n) {
        if (row != null && style != null) {

            // Styles of targets array
            int t = row.size();

            // Size of styles array
            int phi = style.size();

            if (t != phi) {
                throw new RuntimeException("Styles and targets have different sizes");
            }

            for (int i = 0; i < phi; i++) {
                Tc target = row.get(i);
                Tc source = style.get(i);
                String text = RunUtil.getText(source);
                if (text.contains("$")) {
                    applyStyle(target, source);
                } else {
                    row.set(i, source);
                }
            }
        }
        return row;
    }

    public static <T, S> void applyStyle(T targetElement, S sourceElement, int columnCount) {
        if (sourceElement == null) {
            return;
        }
        if (targetElement != null) {
            if (targetElement.getClass().equals(JAXBElement.class)) {
                applyStyle(((JAXBElement<?>) targetElement).getValue(), sourceElement);
            }
            if (sourceElement.getClass().equals(JAXBElement.class)) {
                applyStyle(targetElement, ((JAXBElement<?>) sourceElement).getValue());
            }
            if (targetElement.getClass().equals(Tr.class) && sourceElement.getClass().equals(Tr.class)) {
                Tr targetWrappedRowElement = (Tr) targetElement;
                Tr sourceWrappedRowElement = (Tr) sourceElement;
                List<Tc> targetWrappedRowContents = new ArrayList<>();
                targetWrappedRowElement.getContent().forEach(
                        el -> {
                            if (el instanceof Tc) {
                                targetWrappedRowContents.add((Tc) el);
                            } else if (el instanceof JAXBElement) {
                                Object value = ((JAXBElement<?>) el).getValue();
                                if (value instanceof Tc) {
                                    targetWrappedRowContents.add((Tc) value);
                                }
                            }
                        }
                );
                List<Tc> sourceWrappedRowContents = new ArrayList<>();
                sourceWrappedRowElement.getContent().forEach(
                        el -> {
                            if (el instanceof Tc) {
                                sourceWrappedRowContents.add((Tc) el);
                            } else if (el instanceof JAXBElement) {
                                Object value = ((JAXBElement<?>) el).getValue();
                                if (value instanceof Tc) {
                                    sourceWrappedRowContents.add((Tc) value);
                                }
                            }
                        }
                );
                // Applying style to table row
                copyStyle(targetWrappedRowElement, sourceWrappedRowElement);
                List<Tc> tcs = applyStyle(
                        targetWrappedRowContents,
                        sourceWrappedRowContents,
                        columnCount
                );
                targetWrappedRowElement.getContent().clear();
                targetWrappedRowElement.getContent().addAll(tcs);
            }
        }
    }

    public static void applyStyle(List<Object> runs, R source) {
        for (Object targetObject : runs) {
            R target = TableUtils.getR(targetObject);
            copyStyle(target, source);
        }
    }

    public static <T, S> void applyStyle(T targetElement, S sourceElement) {
        if (targetElement != null && sourceElement != null) {
            if (targetElement.getClass().equals(Tc.class) && sourceElement.getClass().equals(Tc.class)) {
                Tc targetWrappedCellElement = (Tc) targetElement;
                Tc sourceWrappedCellElement = (Tc) sourceElement;
                copyStyle(targetWrappedCellElement, sourceWrappedCellElement);
                applyStyle(targetWrappedCellElement.getContent(), sourceWrappedCellElement.getContent());
            }
            if (targetElement.getClass().equals(P.class) && sourceElement.getClass().equals(P.class)) {
                P targetWrappedParagraphElement = (P) targetElement;
                P sourceWrappedParagraphElement = (P) sourceElement;
                copyStyle(targetWrappedParagraphElement, sourceWrappedParagraphElement);
                ParagraphWrapper pw = new ParagraphWrapper(sourceWrappedParagraphElement);
                R dolla$ign = pw.getRunByText("$");
                if (dolla$ign == null) {
                    dolla$ign = new R();
                    dolla$ign.setRPr(new RPr());
                }
                applyStyle(targetWrappedParagraphElement.getContent(), dolla$ign);
            }
        }
    }

    public static void copyStyle(Tbl target, Tbl source) {
        if (source != null && target != null) {
            target.setTblPr(source.getTblPr());
            target.setTblGrid(source.getTblGrid());
        }
    }

    public static void copyStyle(Tr target, Tr source) {
        if (source != null && target != null)
            target.setTrPr(source.getTrPr());
    }

    public static void copyStyle(Tc target, Tc source) {
        if (source != null && target != null)
            target.setTcPr(source.getTcPr());
    }

    public static void copyStyle(P target, P source) {
        if (source != null && target != null)
            target.setPPr(source.getPPr());
    }

    public static void copyStyle(R target, R source) {
        if (source != null && target != null)
            target.setRPr(source.getRPr());
    }
}
