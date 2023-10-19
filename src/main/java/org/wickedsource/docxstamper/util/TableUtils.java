package org.wickedsource.docxstamper.util;

import org.docx4j.wml.*;
import org.wickedsource.docxstamper.resolver.registry.typeresolver.table.support.LevelElement;
import org.wickedsource.docxstamper.resolver.registry.typeresolver.table.support.Table;
import org.wickedsource.docxstamper.resolver.registry.typeresolver.table.support.TableDataHolder;
import org.wickedsource.docxstamper.resolver.registry.typeresolver.table.support.TableStylingHolder;

import javax.xml.bind.JAXBElement;
import java.util.List;

public class TableUtils {

    private final Table table;

    public TableUtils(Table table) {
        this.table = table;
    }

    public static Tbl getTbl(Object o) {
        if (o instanceof JAXBElement<?>) {
            if (((JAXBElement<?>) o).getDeclaredType().equals(Tbl.class)) {
                return (Tbl) ((JAXBElement<?>) o).getValue();
            }
        } else if (o instanceof Tbl) {
            return (Tbl) o;
        } else {
            Tr rowAttempt = getTr(o);
            if (rowAttempt != null) {
                return getTbl(rowAttempt);
            }
            Tc cellAttempt = getTc(o);
            if (cellAttempt != null) {
                return getTbl(cellAttempt);
            }
        }
        return null;
    }

    public static Tbl getTbl(Tr row) {
        if (row.getParent() instanceof Tbl) {
            return (Tbl) row.getParent();
        } else if (row.getParent() instanceof JAXBElement) {
            if (((JAXBElement<?>) row.getParent()).getDeclaredType().equals(Tbl.class)) {
                return (Tbl) ((JAXBElement<?>) row.getParent()).getValue();
            }
        }
        return null;
    }

    public static Tbl getTbl(Tc cell) {
        Tr row = getTr(cell);
        return row == null ? null : getTbl(row);
    }

    public static Tr getTr(Object o) {
        if (o instanceof JAXBElement<?>) {
            if (((JAXBElement<?>) o).getDeclaredType().equals(Tr.class)) {
                return (Tr) ((JAXBElement<?>) o).getValue();
            }
        } else if (o instanceof Tr) {
            return (Tr) o;
        } else {
            if (o instanceof Tc) {
                Tc cellAttempt = getTc(o);
                if (cellAttempt != null) {
                    getTr(cellAttempt);
                }
            }
        }
        return null;
    }

    public static Tr getTr(Tc cell) {
        if (cell.getParent() instanceof JAXBElement<?>) {
            if (((JAXBElement<?>) cell.getParent()).getDeclaredType().equals(Tr.class)) {
                return (Tr) ((JAXBElement<?>) cell.getParent()).getValue();
            }
        } else if (cell.getParent() instanceof Tr) {
            return (Tr) cell.getParent();
        }
        return null;
    }

    public static Tc getTc(Object o) {
        if (o instanceof JAXBElement<?>) {
            if (((JAXBElement<?>) o).getDeclaredType().equals(Tc.class)) {
                return (Tc) ((JAXBElement<?>) o).getValue();
            }
        } else if (o instanceof Tc) {
            return (Tc) o;
        }
        return null;
    }

    public static P getP(Object o) {
        if (o instanceof JAXBElement<?>) {
            if (((JAXBElement<?>) o).getDeclaredType().equals(P.class)) {
                return (P) ((JAXBElement<?>) o).getValue();
            }
        } else if (o instanceof P) {
            return (P) o;
        }
        return null;
    }

    public static R getR(Object o) {
        if (o instanceof JAXBElement<?>) {
            if (((JAXBElement<?>) o).getDeclaredType().equals(R.class)) {
                return (R) ((JAXBElement<?>) o).getValue();
            }
        } else if (o instanceof R) {
            return (R) o;
        }
        return null;
    }

    public Tbl fillTable() {
        Tbl result = new Tbl();

        TableDataHolder dataHolder = table.getTableDataPopulator();
        TableStylingHolder tableStylingHolder = table.getTableStylingPopulator();

        StyleUtils.copyStyle(result, tableStylingHolder.getTableStyle());

        if (dataHolder.isWithHead()) {
            // Iteration over header
            Tr head = new Tr();
            fillRow(head, dataHolder.getElementByLevel(LevelElement.getHeadLevel()).get(0), tableStylingHolder.getHeaderStyling());
            head.setParent(result);
            result.getContent().add(head);
        }

        {
            // Iteration over data rows
            for (LevelElement rowData : dataHolder.getData()) {
                Tr row = new Tr();
                fillRow(row, rowData, tableStylingHolder.getStylingByLevel(rowData.getLevel()));
                row.setParent(result);
                result.getContent().add(row);
            }
        }

        return result;
    }

    public void fillTable(Tbl generatedTable) {
        TableDataHolder dataHolder = table.getTableDataPopulator();
        TableStylingHolder tableStylingHolder = table.getTableStylingPopulator();
        {
            // Iteration over data rows
            for (LevelElement rowData : dataHolder.getData()) {
                Tr row = new Tr();
                fillRow(row, rowData, tableStylingHolder.getStylingByLevel(rowData.getLevel()));
                row.setParent(generatedTable);
                generatedTable.getContent().add(row);
            }
        }
    }

    private void fillCell(Tc cell, String dataHolder) {
        P paragraph = new P();

        {
            // Adding run object to paragraph and setting up its parent
            List<Object> replaceForString = RunUtil.createReplaceForString(dataHolder);
            paragraph.getContent().addAll(replaceForString);
            paragraph.setParent(cell);
        }

        {
            // Adding paragraph to it
            cell.getContent().add(paragraph);
        }
    }

    private void fillRow(Tr row, LevelElement rowData, Tr style) {
        int size = rowData.getValues().size();
        if (size == 0) {
            throw new IllegalStateException("No data to create row!");
        }
        for (int index = 0; index < size; index++) {
            String data = rowData.getValues().get(index);
            Tc cell = new Tc();
            fillCell(cell, data);
            cell.setParent(row);
            row.getContent().add(cell);
        }
        StyleUtils.applyStyle(row, style, table.getTableDataPopulator().getColumnCount());
    }
}
