package node.UI;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class MyButtonRenderer implements TableCellRenderer {
    private JPanel panel;
    private JButton button;

    public MyButtonRenderer(String text)
    {
        this.initButton(text);

        this.initPanel();

        // 添加按钮。
        this.panel.add(this.button);
    }

    private void initButton(String text)
    {
        this.button = new JButton(text);

        // 设置按钮的大小及位置。
        this.button.setBounds(0, 0, 100, 15);

        // 在渲染器里边添加按钮的事件是不会触发的
        // this.button.addActionListener(new ActionListener()
        // {
        //
        // public void actionPerformed(ActionEvent e)
        // {
        // // TODO Auto-generated method stub
        // }
        // });

    }

    private void initPanel()
    {
        this.panel = new JPanel();

        // panel使用绝对定位，这样button就不会充满整个单元格。
        this.panel.setLayout(null);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
                                                   int column)
    {
        // 只为按钮赋值即可。也可以作其它操作，如绘背景等。
//        this.button.setText(value == null ? "" : String.valueOf(value));

        return this.panel;
    }

}
