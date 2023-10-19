package org.wickedsource.docxstamper.core.walk.coordinates;

import org.docx4j.wml.Tbl;

import java.util.Iterator;

public class TableCoordinates extends AbstractCoordinates {

    private final Tbl table;

    public Iterator<Object> getParentIterator() {
        return parentIterator;
    }

    private final Iterator<Object> parentIterator;

    public TableCoordinates(Tbl table, Iterator<Object> parentIterator) {
        this.table = table;
        this.parentIterator = parentIterator;
    }

    public String toString() {
        return "table";
    }

    public Tbl getTable() {
        return table;
    }
}
