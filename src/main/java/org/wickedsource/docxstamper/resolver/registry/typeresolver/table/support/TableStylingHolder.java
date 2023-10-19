package org.wickedsource.docxstamper.resolver.registry.typeresolver.table.support;

import lombok.extern.slf4j.Slf4j;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblPr;
import org.docx4j.wml.Tr;
import org.docx4j.wml.TrPr;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class TableStylingHolder {

    private final Tr defaultStyle = new Tr();
    private final Map<Integer, Tr> styles = new HashMap<>();
    private Tbl defaultTableStyle = new Tbl();
    private Tbl tableStyle;

    {
        defaultStyle.setTrPr(new TrPr());
    }

    {
        defaultTableStyle.setTblPr(new TblPr());
    }

    public void configure(Integer level, Tr config) {
        if (config != null) {
            styles.put(level, config);
        } else {
            log.debug("Attempt to set null as style for level {}", level);
        }
    }

    public Tr getHeaderStyling() {
        return getStylingByLevel(LevelElement.getHeadLevel());
    }

    public Tr getDefaultStyling() {
        return styles.getOrDefault(LevelElement.getDefaultLevel(), null);
    }

    public Tr getStylingByLevel(Integer level) {
        return styles.getOrDefault(level, getDefaultStyling());
    }

    public Tbl getTableStyle() {
        return tableStyle == null ? defaultTableStyle : tableStyle;
    }

    public void setTableStyle(Tbl tableStyle) {
        this.tableStyle = tableStyle;
    }

    public Map<Integer, Tr> getStyles() {
        return styles;
    }
}
