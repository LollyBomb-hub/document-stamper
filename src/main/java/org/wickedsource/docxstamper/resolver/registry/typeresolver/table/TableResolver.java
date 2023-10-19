package org.wickedsource.docxstamper.resolver.registry.typeresolver.table;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.*;
import org.wickedsource.docxstamper.core.replace.ExpressionConfigurator;
import org.wickedsource.docxstamper.resolver.registry.typeresolver.table.support.*;
import org.wickedsource.docxstamper.core.ParagraphWrapper;
import org.wickedsource.docxstamper.resolver.registry.typeresolver.AbstractTypeResolver;
import org.wickedsource.docxstamper.util.StyleUtils;
import org.wickedsource.docxstamper.util.TableUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class TableResolver extends AbstractTypeResolver<Table, Table> {

    private static final String tableElementType = "TableTypeElement";

    private final String[] properties = {"^TableHeaderStyling$", "^TableHeader$", "^TableContent$", "^TableContent::(\\d+)$", "^TableStyling$", "^Table$"};

    @Override
    public Table resolve(WordprocessingMLPackage document, Table expressionResult) {
        return expressionResult;
    }

    @Override
    public void process(WordprocessingMLPackage document, Table table, ParagraphWrapper paragraphWrapper, String placeholder) {
        P placeholderParagraph = paragraphWrapper.getParagraph();

        if (!(placeholderParagraph.getParent() instanceof Body))
            throw new IllegalStateException("Nested positioning isn't supported!");

        TableDataHolder dataHolder = table.getTableDataPopulator();
        TableStylingHolder tableStylingHolder = table.getTableStylingPopulator();

        TableUtils tableUtils = new TableUtils(table);

        Body body = (Body) placeholderParagraph.getParent();
        Tbl generatedTable = null;

        if (configuration.containsKey("Table")) {
            Object object = configuration.get("Table");
            Tbl config = TableUtils.getTbl(object);
            if (config != null) {
                generatedTable = new Tbl();
                fromTable(tableStylingHolder, config, generatedTable);
            }
        }

        if (generatedTable == null) {
            generatedTable = tableUtils.fillTable();
        } else {
            tableUtils.fillTable(generatedTable);
        }

        configuration.clear();

        generatedTable.setParent(body);

        int index = body.getContent().indexOf(placeholderParagraph);
        body.getContent().set(index, generatedTable);
    }

    @Override
    public void configure(String property, Object value) {
        super.configure(property, value);
    }

    public static String getTableElementType() {
        return tableElementType;
    }

    @Override
    public ExpressionConfigurator[] getConfigurators() {
        return TableResolverExpressionConfigurator.values();
    }

    private void fromTable(TableStylingHolder populator, Tbl config, Tbl target) {
        boolean headerConfigured = false;
        // Копируем стили
        StyleUtils.copyStyle(target, config);

        // Иду построчно
        if (config.getContent() != null && config.getContent().size() != 0) {
            for (Object tableElement : config.getContent()) {
                Tr row = TableUtils.getTr(tableElement);
                // NullPointer check
                if (row != null) {
                    // Устанавливаю заголовки таблицы
                    if (!headerConfigured) {
                        headerConfigured = true;
                        target.getContent().add(row);
                    } else {
                        Tc configurationCell = TableUtils.getTc(row.getContent().get(0));
                        if (configurationCell != null && config.getContent() != null && configurationCell.getContent().size() == 1) {
                            P paragraph = TableUtils.getP(configurationCell.getContent().get(0));

                            if (paragraph != null) {
                                ParagraphWrapper pw = new ParagraphWrapper(paragraph);

                                String text = pw.getText();
                                if (text.equals("${default}")) {
                                    populator.configure(LevelElement.getDefaultLevel(), row);
                                } else {
                                    Pattern p = Pattern.compile("\\$\\{level(\\d+)}");
                                    Matcher matcher = p.matcher(text);
                                    if (matcher.matches()) {
                                        populator.configure(Integer.valueOf(matcher.group(1)), row);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}