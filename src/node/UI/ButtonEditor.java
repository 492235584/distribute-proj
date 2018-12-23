package node.UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ButtonEditor extends DefaultCellEditor {
    private MyButton button;
    private MyEvent event;

    public ButtonEditor() {
        super(new JTextField());
    }

    public ButtonEditor(MyEvent e, String name) {
        this();
        this.event = e;
        button = new MyButton(name);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //这里调用自定义的事件处理方法
                event.invoke(e);
            }

        });
    }

    /*
    重写编辑器方法，返回一个按钮给JTable
    */
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
//      setClickCountToStart(1);
//将这个被点击的按钮所在的行和列放进button里面
        button.setRow(row);
        button.setColumn(column);
        return button;
    }
}
