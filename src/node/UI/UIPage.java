package node.UI;

import node.NodeClient;
import node.NodeContext;
import node.NodeServer;
import node.responsepojo.FileSearchResponse;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.List;

import static node.NodeContext.*;

public class UIPage {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        initUI();
        initNode();

    }

    public static void initNode() {
        NodeServer.start(NodeContext.LOCAL_IP);
        NodeClient.start(NodeContext.START_IP, NodeContext.SERVER_POST);

        buildTopology();
        System.out.println(neighbors);
    }

    public static void initUI() {
        try {
            // 是windows
            if (System.getProperty("os.name").toUpperCase().indexOf("WINDOWS") != -1) {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            }
        } catch (Exception e) {
            System.out.println("设置界面感官异常!");
            e.printStackTrace();
        }

        //3.在initUI方法中，实例化JFrame类的对象。
        JFrame frame = new JFrame("分布式系统");
        frame.setBackground(Color.white);
        // 4.设置窗体对象的属性值：标题、大小、显示位置、关闭操作、布局、禁止调整大小、可见、...
        frame.setSize(700, 600);// 设置窗体的大小，单位是像素
        // close event
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                NodeContext.quit();
                System.exit(0);
            }
        });
//        frame.setLocationRelativeTo(null);// 设置窗体相对于另一个组件的居中位置，参数null表示窗体相对于屏幕的中央位置
        frame.setResizable(false);// 设置禁止调整窗体大小

        // 实例化FlowLayout流式布局类的对象，指定对齐方式为居中对齐，组件之间的间隔为5个像素
        FlowLayout fl = new FlowLayout(FlowLayout.CENTER, 50, 30);
        // 实例化流式布局类的对象
        frame.setLayout(fl);

        // 实例化JLabel标签对象，该对象显示"账号："
        JLabel labName = new JLabel("分布式系统设计", JLabel.CENTER);
        labName.setFont(new Font("黑体", Font.BOLD, 30));
        // 将labName标签添加到窗体上
        frame.add(labName);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(30, 10));
        Dimension mainDim = new Dimension(660, 200);
        mainPanel.setPreferredSize(mainDim);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        // 5.实例化元素组件对象，将元素组件对象添加到窗体上（组件添加要在窗体可见之前完成）。
        // 实例化ImageIcon图标类的对象，该对象加载磁盘上的图片文件到内存中，这里的路径要用两个\
        ImageIcon icon = new ImageIcon("upload.png");
        // 用标签来接收图片，实例化JLabel标签对象，该对象显示icon图标
        JLabel labIcon = new JLabel(icon);
        //设置标签大小
        //labIcon.setSize(30,20);setSize方法只对窗体有效，如果想设置组件的大小只能用
        Dimension dim11 = new Dimension(200, 137);
        labIcon.setPreferredSize(dim11);
        // 将labIcon标签添加到窗体上
        panel.add(labIcon, BorderLayout.NORTH);

        JButton button1 = new JButton();
        Dimension dim12 = new Dimension(20, 20);
        button1.setText("上传文件");
        //设置按钮的大小
        button1.setSize(dim12);
        panel.add(button1, BorderLayout.SOUTH);

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
                if (JFileChooser.APPROVE_OPTION == returnVal) {
                    //打印出文件的路径，你可以修改位 把路径值 写到 textField 中
                    if (fDialog.getSelectedFile() != null)
                        uploadFile(fDialog.getSelectedFile().toString());
                }
            }
        });

        mainPanel.add(panel, BorderLayout.WEST);

        JPanel panel2 = new JPanel();
        panel2.setLayout(new BorderLayout());
        ImageIcon icon2 = new ImageIcon("download.png");
        JLabel labIcon2 = new JLabel(icon2);
        Dimension dim21 = new Dimension(200, 137);
        labIcon2.setPreferredSize(dim21);
        panel2.add(labIcon2, BorderLayout.NORTH);

        JButton button2 = new JButton();
        Dimension dim22 = new Dimension(20, 20);
        button2.setText("搜索文件");
        button2.setSize(dim22);
        panel2.add(button2, BorderLayout.SOUTH);

        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String key = JOptionPane.showInputDialog("Please input filename");
                if (key != null) {
                    Set<FileSearchResponse> searchResults = searchFile(key);
                    generateTable(searchResults);
                }
            }
        });

        mainPanel.add(panel2, BorderLayout.CENTER);

        JPanel panel3 = new JPanel();
        panel3.setLayout(new BorderLayout());
        ImageIcon icon3 = new ImageIcon("upload.png");
        JLabel labIcon3 = new JLabel(icon3);
        Dimension dim31 = new Dimension(200, 137);
        labIcon3.setPreferredSize(dim31);
        panel3.add(labIcon3, BorderLayout.NORTH);

        JButton button3 = new JButton();
        Dimension dim32 = new Dimension(20, 20);
        button3.setText("分布式计算");
        button3.setSize(dim32);
        panel3.add(button3, BorderLayout.SOUTH);

        mainPanel.add(panel3, BorderLayout.EAST);

        frame.add(mainPanel);

        frame.setVisible(true);// 设置窗体为可见
    }

    public static void generateTable(Set<FileSearchResponse> result) {
        HashMap<Integer, List<FileSearchResponse>> list = new HashMap<>();

        JFrame tableframe = new JFrame("searchResults");
        Table_Model model = new Table_Model();
        JTable table = new JTable(model);
        TableColumnModel tcm = table.getColumnModel();

        MyEvent e = new MyEvent() {
            @Override
            public void invoke(ActionEvent e) {
                MyButton button = (MyButton) e.getSource();
                //打印被点击的行和列
                if (button.getColumn() == 3)
                    download(list.get(button.getRow()));
                else if (button.getColumn() == 4) {
                    //初始化文件选择框
                    JFileChooser fDialog = new JFileChooser();
                    //设置文件选择框的标题
                    fDialog.setDialogTitle("请选择需要上传的文件");
                    //弹出选择框
                    int returnVal = fDialog.showOpenDialog(null);
                    // 如果是选择了文件
                    if (JFileChooser.APPROVE_OPTION == returnVal) {
                        //打印出文件的路径，你可以修改位 把路径值 写到 textField 中
                        if (fDialog.getSelectedFile() != null)
                            update(fDialog.getSelectedFile().toString(), list.get(button.getRow()));
                    }
                }

            }
        };

        tcm.getColumn(3).setCellRenderer(new ButtonRender("download"));
        tcm.getColumn(3).setCellEditor(new ButtonEditor(e, "download"));

        tcm.getColumn(4).setCellRenderer(new ButtonRender("update"));
        tcm.getColumn(4).setCellEditor(new ButtonEditor(e, "update"));
        table.selectAll();
        JScrollPane s_pan = new JScrollPane(table);
        tableframe.getContentPane().add(s_pan);
        tableframe.setSize(600, 600);
        tableframe.setVisible(true);

        /**--------*/
        HashMap<FilenameAndIp, List<FileSearchResponse>> fileAndAddress = splitResponse(result);
        int i = 0;
        for (Map.Entry<FilenameAndIp, List<FileSearchResponse>> entry : fileAndAddress.entrySet()) {
            long size = containAllFiles(entry.getValue());
            if (size > 0) {
                FilenameAndIp info = entry.getKey();
                model.addRow(info.getFilename(), info.getIp(), String.valueOf(size));

                list.put(i, entry.getValue());
            }
            i++;
        }
    }

    /**
     * 处理FileSearchResponse,相同的文件响应放在一起
     */
    private static HashMap<FilenameAndIp, List<FileSearchResponse>> splitResponse(Set<FileSearchResponse> searchResults) {
        HashMap<FilenameAndIp, List<FileSearchResponse>> fileAndAddress = new HashMap<>();
        for (FileSearchResponse response : searchResults) {
            FilenameAndIp resourceInfo = new FilenameAndIp(response.getFilename(), response.getSourceIp());
            if (fileAndAddress.containsKey(resourceInfo)) {
                List<FileSearchResponse> list = fileAndAddress.get(resourceInfo);
                list.add(response);
            } else {
                List<FileSearchResponse> list = new ArrayList<>();
                list.add(response);
                fileAndAddress.put(resourceInfo, list);
            }
        }
        return fileAndAddress;
    }

    /**
     * judge whether a list contain all file parts or not
     * if the file is complete,return the size
     *
     * @param list
     * @return size of the file
     */
    private static long containAllFiles(List<FileSearchResponse> list) {
        if (list == null || list.size() == 0) {
            return -1;
        }

        int total = list.get(0).getTotalPart();

        // just have one file
        if (total == 1) {
            return list.get(0).getSize();
        }

        long totalSize = 0;
        Set<Integer> allpart = new HashSet<>();
        for (int i = 1; i <= total; i++) {
            allpart.add(i);
        }

        for (FileSearchResponse response : list) {
            if (allpart.contains(response.getPart())) {
                allpart.remove(response.getPart());
                totalSize += response.getSize();
            }
        }

        return allpart.size() != 0 ? -1 : totalSize;
    }

    /**
     * download from list
     *
     * @param list
     */
    private static void download(List<FileSearchResponse> list) {
        if (list == null || list.size() == 0) {
            return;
        }

        // single file
        if (list.get(0).getTotalPart() == 1) {
            FileSearchResponse response = list.get(0);
            NodeContext.downloadFile(response.completeName(), response.getSaveIp());
        } else { // totalPart > 1
            int total = list.get(0).getTotalPart();
            Map<Integer, FileSearchResponse> parts = new TreeMap<>();
            for (FileSearchResponse response : list) {
                if (response.getTotalPart() > 1) {
                    parts.put(response.getPart(), response);
                }
            }

            // download every parts
            for (FileSearchResponse response : parts.values()) {
                NodeContext.downloadFile(response.completeName(), response.getSaveIp());
            }

            // combine all parts to a file
            byte[][] splitedDatas = new byte[parts.size()][];
            for (FileSearchResponse response : parts.values()) {
                // wait until received file
                while (!NodeContext.filenameAndStatus.containsKey(response.completeName())) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                // read part
                byte[] datas = NodeContext.readFile(NodeContext.DIR_PATH + '/' + response.completeName());
                splitedDatas[response.getPart() - 1] = datas;
            }
            List<Byte> allData = new ArrayList<>();
            for (byte[] datas : splitedDatas) {
                for (byte b : datas) {
                    allData.add(b);
                }
            }
            byte[] bytes = new byte[allData.size()];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = allData.get(i);
            }

            // save complete file
            NodeContext.saveFile(list.get(0).getSourceIp() + NodeContext.NAMESPLIT + list.get(0).getFilename(), bytes, null);
            // unpate filenameAndStatus
            for (FileSearchResponse response : parts.values()) {
                NodeContext.filenameAndStatus.remove(response.completeName());
            }
        }
    }

    /**
     * update file in list
     *
     * @param list
     */
    private static void update(String newfile, List<FileSearchResponse> list) {
        if (list == null || list.size() == 0) {
            return;
        }
        byte[] bytes = NodeContext.readFile(newfile);
        int byteNum = bytes.length / list.size();
        for (FileSearchResponse response : list) {
            if (response.getTotalPart() == 1) {
                NodeContext.updateFile(response.getSaveIp(), response.completeName(), bytes);
            } else {
                int part = response.getPart();
                // get subdata
                int start = (part - 1) * byteNum;
                int end = part * byteNum;
                if (part == response.getTotalPart()) {
                    end = bytes.length;
                }
                byte[] sub = NodeContext.subBytes(bytes, start, end);
                // update
                NodeContext.updateFile(response.getSaveIp(), response.completeName(), bytes);
            }
        }
    }
}