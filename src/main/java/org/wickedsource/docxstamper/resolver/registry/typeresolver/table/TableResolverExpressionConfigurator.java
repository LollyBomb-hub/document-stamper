package org.wickedsource.docxstamper.resolver.registry.typeresolver.table;

import lombok.extern.slf4j.Slf4j;
import org.docx4j.wml.Body;
import org.docx4j.wml.P;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.wickedsource.docxstamper.core.replace.ExpressionConfigurator;
import org.wickedsource.docxstamper.core.walk.coordinates.ParagraphCoordinates;
import org.wickedsource.docxstamper.resolver.registry.TypeResolverRegistry;
import org.wickedsource.docxstamper.util.TableUtils;

import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public enum TableResolverExpressionConfigurator implements ExpressionConfigurator {

    HEADER("\\$\\{tableHeader}", (paragraphCoordinates, placeholder) -> {
        P paragraph = paragraphCoordinates.getParagraph();
        if (paragraph.getParent() != null) {
            if (paragraph.getParent() instanceof Body) {
                paragraphCoordinates.getParentIterator().remove();
                if (paragraphCoordinates.getParentIterator().hasNext()) {
                    Object toBeTable = paragraphCoordinates.getParentIterator().next();
                    Tbl headConfiguration = TableUtils.getTbl(toBeTable);
                    if (headConfiguration != null) {
                        TypeResolverRegistry.TblResolver.configure("TableHeader", headConfiguration);
                        paragraphCoordinates.getParentIterator().remove();
                    }
                }
            } else if (paragraph.getParent() instanceof Tc) {
                TypeResolverRegistry.TblResolver.configure("TableHeaderStyling", TableUtils.getTr((Tc) paragraph.getParent()));
            }
        }
    }),
    CONTENT("\\$\\{tableContent(\\d*)}", (paragraphCoordinates, placeholder) -> {
        Pattern p = Pattern.compile("\\$\\{tableContent(\\d*)}");
        Matcher matcher = p.matcher(placeholder);
        String group = null;
        if (matcher.matches()) {
            group = matcher.group(1);
        }
        String propertyName = "TableContent";
        if (group != null && !group.equals("")) {
            propertyName += "::" + group;
        }
        P paragraph = paragraphCoordinates.getParagraph();
        if (paragraph.getParent() != null) {
            if (paragraph.getParent() instanceof Body) {
                paragraphCoordinates.getParentIterator().remove();
                if (paragraphCoordinates.getParentIterator().hasNext()) {
                    Object toBeTable = paragraphCoordinates.getParentIterator().next();
                    Tbl contentConfiguration = TableUtils.getTbl(toBeTable);
                    if (contentConfiguration != null) {
                        TypeResolverRegistry.TblResolver.configure(propertyName, contentConfiguration.getContent().get(0));
                        paragraphCoordinates.getParentIterator().remove();
                    }
                }
            } else if (paragraph.getParent() instanceof Tc) {
                TypeResolverRegistry.TblResolver.configure(propertyName, TableUtils.getTr((Tc) paragraph.getParent()));
            }
        }
    }),
    TABLE_STYLING("\\$\\{tableStyle}", (paragraphCoordinates, placeholder) -> {
        P paragraph = paragraphCoordinates.getParagraph();
        if (paragraph.getParent() != null) {
            if (paragraph.getParent() instanceof Body) {
                paragraphCoordinates.getParentIterator().remove();
                if (paragraphCoordinates.getParentIterator().hasNext()) {
                    Object toBeTable = paragraphCoordinates.getParentIterator().next();
                    Tbl contentConfiguration = TableUtils.getTbl(toBeTable);
                    if (contentConfiguration != null) {
                        TypeResolverRegistry.TblResolver.configure("TableStyling", contentConfiguration);
                        paragraphCoordinates.getParentIterator().remove();
                    }
                }
            } else {
                log.error("Nested paragraph with control word is used! Not implemented yet!");
            }
        }
    }),
    ALL("\\$\\{table}", (paragraphCoordinates, placeholder) -> {
        P paragraph = paragraphCoordinates.getParagraph();
        if (paragraph.getParent() != null) {
            if (paragraph.getParent() instanceof Body) {
                paragraphCoordinates.getParentIterator().remove();
                if (paragraphCoordinates.getParentIterator().hasNext()) {
                    Object toBeTable = paragraphCoordinates.getParentIterator().next();
                    Tbl contentConfiguration = TableUtils.getTbl(toBeTable);
                    if (contentConfiguration != null) {
                        TypeResolverRegistry.TblResolver.configure("Table", contentConfiguration);
                        paragraphCoordinates.getParentIterator().remove();
                    }
                }
            }
        }
    });

    private final BiConsumer<ParagraphCoordinates, String> configurator;
    private final Pattern pattern;

    TableResolverExpressionConfigurator(String placeholder, BiConsumer<ParagraphCoordinates, String> configurator) {
        this.configurator = configurator;

        this.pattern = Pattern.compile(placeholder);
    }

    @Override
    public void configure(ParagraphCoordinates paragraphCoordinates, String placeholder) {
        configurator.accept(paragraphCoordinates, placeholder);
    }

    @Override
    public boolean supports(String placeholder) {
        return pattern.matcher(placeholder).matches();
    }
}