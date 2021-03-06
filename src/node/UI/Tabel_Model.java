package node.UI;

import javax.swing.table.AbstractTableModel;
import java.util.Vector;

class Table_Model extends AbstractTableModel {
    private static final long serialVersionUID = -3094977414157589758L;

    private Vector content = null;

    private String[] title_name = {"filename", "source", "size", "download", "update"};

    public Table_Model() {
        content = new Vector();
    }

    /**
     * 加入一行内容
     */
    public void addRow(String name, String ip, String size) {
        Vector v = new Vector();
        v.add(0, name);
        v.add(1, ip); // JCheckBox是Boolean的默认显示组件，这里仅仅为了看效果，其实用JComboBox显示***更合适
        v.add(2, size); // 本列在前面已经设置成了JComboBox组件，这里随便输入什么字符串都没关系
        v.add(3, 1); // 本列在前面已经设置成了JComboBox组件，这里随便输入什么字符串都没关系
        v.add(4, 1); // 本列在前面已经设置成了JComboBox组件，这里随便输入什么字符串都没关系
        content.add(v);
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    public void setValueAt(Object value, int row, int col) {
        ((Vector) content.get(row)).remove(col);
        ((Vector) content.get(row)).add(col, value);
        this.fireTableCellUpdated(row, col);
    }

    public String getColumnName(int col) {
        return title_name[col];
    }

    public int getColumnCount() {
        return title_name.length;
    }

    public int getRowCount() {
        return content.size();
    }

    public Object getValueAt(int row, int col) {
        return ((Vector) content.get(row)).get(col);
    }

    public Class getColumnClass(int col) {
        return getValueAt(0, col).getClass();
    }

}