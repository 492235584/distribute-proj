package node.UI;

import node.NodeClient;
import node.NodeContext;
import node.NodeServer;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static node.NodeContext.*;

public class UIPage {
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        initUI();
        initNode();

    }

    public static void initNode(){
        NodeServer.start(NodeContext.LOCAL_IP);
        NodeClient.start(NodeContext.START_IP, NodeContext.SERVER_POST);
        buildTopology();
        System.out.println(neighbors);
    }

    public static void initUI() {
        //3.在initUI方法中，实例化JFrame类的对象。
        JFrame frame = new JFrame("分布式系统");
        frame.setBackground(Color.white);
        // 4.设置窗体对象的属性值：标题、大小、显示位置、关闭操作、布局、禁止调整大小、可见、...
        frame.setSize(700, 600);// 设置窗体的大小，单位是像素
        frame.setDefaultCloseOperation(3);// 设置窗体的关闭操作；3表示关闭窗体退出程序；2、1、0
//        frame.setLocationRelativeTo(null);// 设置窗体相对于另一个组件的居中位置，参数null表示窗体相对于屏幕的中央位置
        frame.setResizable(false);// 设置禁止调整窗体大小

        // 实例化FlowLayout流式布局类的对象，指定对齐方式为居中对齐，组件之间的间隔为5个像素
        FlowLayout fl = new FlowLayout(FlowLayout.CENTER,50,30);
        // 实例化流式布局类的对象
        frame.setLayout(fl);

        // 实例化JLabel标签对象，该对象显示"账号："
        JLabel labName = new JLabel("分布式系统设计",JLabel.CENTER);
        labName.setFont(new Font("黑体", Font.BOLD, 30));
        // 将labName标签添加到窗体上
        frame.add(labName);

        JPanel mainPanel=new JPanel();
        mainPanel.setLayout(new BorderLayout(30,10));
        Dimension mainDim = new Dimension(660,200);
        mainPanel.setPreferredSize(mainDim);

        JPanel panel=new JPanel();
        panel.setLayout(new BorderLayout());
        // 5.实例化元素组件对象，将元素组件对象添加到窗体上（组件添加要在窗体可见之前完成）。
        // 实例化ImageIcon图标类的对象，该对象加载磁盘上的图片文件到内存中，这里的路径要用两个\
        ImageIcon icon = new ImageIcon("upload.png");
        // 用标签来接收图片，实例化JLabel标签对象，该对象显示icon图标
        JLabel labIcon = new JLabel(icon);
        //设置标签大小
        //labIcon.setSize(30,20);setSize方法只对窗体有效，如果想设置组件的大小只能用
        Dimension dim11 = new Dimension(200,137);
        labIcon.setPreferredSize(dim11);
        // 将labIcon标签添加到窗体上
        panel.add(labIcon,BorderLayout.NORTH);

        JButton button1=new JButton();
        Dimension dim12 = new Dimension(20,20);
        button1.setText("上传文件");
        //设置按钮的大小
        button1.setSize(dim12);
        panel.add(button1,BorderLayout.SOUTH);

        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //初始化文件选择框
                JFileChooser fDialog = new JFileChooser();
                //设置文件选择框的标题
                fDialog.setDialogTitle("请选择需要上传的文件");
                //弹出选择框
                int returnVal = fDialog.showOpenDialog(null);
                // 如果是选择了文件
                if(JFileChooser.APPROVE_OPTION == returnVal){
                    //打印出文件的路径，你可以修改位 把路径值 写到 textField 中
                    System.out.println(fDialog.getSelectedFile());
                    uploadFile(fDialog.getSelectedFile().toString());
                }
            }
        });

        mainPanel.add(panel,BorderLayout.WEST);


        JPanel panel2=new JPanel();
        panel2.setLayout(new BorderLayout());
        ImageIcon icon2 = new ImageIcon("download.png");
        JLabel labIcon2 = new JLabel(icon2);
        Dimension dim21 = new Dimension(200,137);
        labIcon2.setPreferredSize(dim21);
        panel2.add(labIcon2,BorderLayout.NORTH);

        JButton button2=new JButton();
        Dimension dim22 = new Dimension(20,20);
        button2.setText("搜索文件");
        button2.setSize(dim22);
        panel2.add(button2,BorderLayout.SOUTH);

        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String key = JOptionPane.showInputDialog("Please input filename");
//                Set<FileSearchResponse> searchResults=searchFile(key);
                if(key!=null)
                      generateTable();
            }
        });

        mainPanel.add(panel2,BorderLayout.CENTER);

        JPanel panel3=new JPanel();
        panel3.setLayout(new BorderLayout());
        ImageIcon icon3 = new ImageIcon("upload.png");
        JLabel labIcon3 = new JLabel(icon3);
        Dimension dim31 = new Dimension(200,137);
        labIcon3.setPreferredSize(dim31);
        panel3.add(labIcon3,BorderLayout.NORTH);

        JButton button3=new JButton();
        Dimension dim32 = new Dimension(20,20);
        button3.setText("分布式计算");
        button3.setSize(dim32);
        panel3.add(button3,BorderLayout.SOUTH);

        mainPanel.add(panel3,BorderLayout.EAST);

        frame.add(mainPanel);

        frame.setVisible(true);// 设置窗体为可见
    }



    public static void generateTable(){
        JFrame tableframe = new JFrame("searchResults");
        Table_Model model = new Table_Model();
        JTable table = new JTable(model);
        TableColumnModel tcm = table.getColumnModel();

        tcm.getColumn(3).setCellRenderer(new MyButtonRenderer("download"));
        tcm.getColumn(3).setCellEditor(new MyButtonEditor("download"));

        tcm.getColumn(4).setCellRenderer(new MyButtonRenderer("update"));
        tcm.getColumn(4).setCellEditor(new MyButtonEditor("update"));
        //禁止表格的选择功能,不然在点击按钮时表格的整行都会被选中
        table.setRowSelectionAllowed(false);

        model.addRow("jafjhwhj.txt", "qwfqf", "1212");

        JScrollPane s_pan = new JScrollPane(table);

        tableframe.getContentPane().add(s_pan, BorderLayout.CENTER);

        tableframe.setSize(500, 500);
        tableframe.setVisible(true);

    }
}




