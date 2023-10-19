package org.wickedsource.docxstamper.resolver.registry.typeresolver.table.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class TableDataHolder {

    private final List<LevelElement> leveledValuesList = new ArrayList<>();

    private boolean isWithHead = false;
    private boolean isWithContent = false;

    private int columnCount = 0;

    private void updateState(LevelElement element) {
        if (element.getValues().size() > columnCount) {
            columnCount = element.getValues().size();
        }
        if (element.getLevel() == LevelElement.getHeadLevel()) {
            isWithHead = true;
        }
        else {
            isWithContent = true;
        }
    }

    public void addItem(LevelElement element) {
        updateState(element);
        leveledValuesList.add(element);
    }

    public void addItem(int index, LevelElement element) {
        updateState(element);
        leveledValuesList.add(index, element);
    }

    public List<LevelElement> getLeveledValuesList() {
        return leveledValuesList;
    }

    public void addValueToRow(int row, int level, List<String> values) {
        addItem(row, new LevelElement(level, values));
    }

    public void addValueToRow(int row, List<String> values) {
        addValueToRow(row, LevelElement.getDefaultLevel(), values);
    }

    public void addValueToRow(List<String> values) {
        addItem(new LevelElement(LevelElement.getDefaultLevel(), values));
    }

    public void addValueToRow(String value) {
        addValueToRow(List.of(value));
    }

    public void addValueToRow(String... values) {
        addValueToRow(List.of(values));
    }

    public void addValueToRow(List<String> values, int level) {
        addItem(new LevelElement(level, values));
    }

    public void addValueToRow(String value, int level) {
        addValueToRow(List.of(value), level);
    }

    public void addValueToRow(int row, String value) {
        addValueToRow(row, List.of(value));
    }

    public void addValueToRow(int row, String... values) {
        addValueToRow(row, List.of(values));
    }

    public Map<Integer, LevelElement> getElementByLevel(int level) {
        Map<Integer, LevelElement> result = new HashMap<>();

        leveledValuesList.stream().filter(levelElement -> levelElement.getLevel() == level).collect(Collectors.toList()).forEach(
                levelElement -> result.put(leveledValuesList.indexOf(levelElement), levelElement)
        );

        return result;
    }

    public boolean isWithHead() {
        return isWithHead;
    }

    public boolean isWithContent() {
        return isWithContent;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public List<LevelElement> getData() {
        return leveledValuesList.stream().filter(level -> level.getLevel() != LevelElement.getHeadLevel()).collect(Collectors.toList());
    }
}
