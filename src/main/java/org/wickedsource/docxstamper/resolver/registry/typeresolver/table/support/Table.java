package org.wickedsource.docxstamper.resolver.registry.typeresolver.table.support;

public class Table {

    private TableDataHolder tableDataPopulator;
    private TableStylingHolder tableStylingHolder = new TableStylingHolder();

    public Table(TableDataHolder tableDataPopulator) {
        this.tableDataPopulator = tableDataPopulator;
    }

    public Table(TableDataHolder tableDataPopulator, TableStylingHolder tableStylingHolder) {
        this.tableDataPopulator = tableDataPopulator;
        this.tableStylingHolder = tableStylingHolder;
    }

    public TableDataHolder getTableDataPopulator() {
        return tableDataPopulator;
    }

    public void setTableDataPopulator(TableDataHolder tableDataPopulator) {
        this.tableDataPopulator = tableDataPopulator;
    }

    public TableStylingHolder getTableStylingPopulator() {
        return tableStylingHolder;
    }

    public void setTableStylingPopulator(TableStylingHolder tableStylingHolder) {
        this.tableStylingHolder = tableStylingHolder;
    }

}
