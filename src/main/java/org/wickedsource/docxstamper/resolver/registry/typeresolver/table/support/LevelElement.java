package org.wickedsource.docxstamper.resolver.registry.typeresolver.table.support;

import java.util.List;

public class LevelElement {

    private static final int defaultLevel = -1;
    private static final int headLevel = -2;

    private int level;
    private List<String> values;

    public LevelElement(int level, List<String> values) {
        this.level = level;
        this.values = values;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public static int getDefaultLevel() {
        return defaultLevel;
    }

    public static int getHeadLevel() {
        return headLevel;
    }
}