package node;

import node.requestpojo.DistributeCalculateMessage;
import node.responsepojo.CalculationResultResponse;
import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import rpc.common.RequestId;

import java.awt.*;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import static node.NodeContext.*;

public class DistributeCalculation {

    //统计平均通话次数
    public static void caculateResult(String path,int numberOfDays){
        String[] data=readCallingFile(path);
        if(data!=null){
            HashMap<String,Integer> result1;
            int[][] result2;
            HashMap<String,HashMap<String,Double>> result3;

            int length1=(int)Math.floor(data.length*59/60);
            String[] myPart= Arrays.copyOfRange(data,0,length1);

            HashMap<String,Integer> myResult1=callingTimes(myPart);
            int[][] myResult2=rateOfMobileCompy(myPart);
            HashMap<String,HashMap<String,Double>> myResult3=timeRate(myPart);

//            System.out.println(myResult);
            System.out.println(neighbors.size());

            String messageId = RequestId.next();
            messageSearched.put(messageId, 1);
            //
            String[] otherPart=Arrays.copyOfRange(data,length1,data.length);

            int length2=(int)Math.floor(otherPart.length/neighbors.size());
            Iterator entries = neighbors.entrySet().iterator();
            int id=1;
            String[] sendData;
            while (entries.hasNext()) {
                Map.Entry entry = (Map.Entry) entries.next();
                NodeClient client = (NodeClient)entry.getValue();
                if(entries.hasNext())
                    sendData=Arrays.copyOfRange(otherPart,(id-1)*length2,id*length2);
                else
                    sendData=Arrays.copyOfRange(otherPart,(id-1)*length2,otherPart.length);
                id++;

                String[] ad={"qfqf","fqwfqf"};

                CalculationResultResponse response = client.distributeCalculate(new DistributeCalculateMessage(messageId,LOCAL_IP,sendData));
                System.out.println(response);

                result1=response.getResult1();
                result2=response.getResult2();
                result3=response.getResult3();

                combineResult1(myResult1,result1);
                combineResult2(myResult2,result2);
                combineResult3(myResult3,result3);
            }

            //do write result into file
            writeResult(myResult1);

            generateChart((double)myResult2[0][0],(double)myResult2[0][1],(double)myResult2[0][2],"Local Call");
            generateChart((double)myResult2[1][0],(double)myResult2[1][1],(double)myResult2[1][2],"Long-distance Call");
            generateChart((double)myResult2[2][0],(double)myResult2[2][1],(double)myResult2[2][2],"International Call");

            writeResult3(myResult3);
        }
    }

    //计算结果1：写入文件
    public static void writeResult(HashMap<String,Integer> sets){
        try {
            FileWriter writer = new FileWriter("out1.txt",false);
            for(String key:sets.keySet())
            {
                writer.write("<"+key+","+(double)sets.get(key)/29+">\n");
            }
            writer.flush();//刷新内存，将内存中的数据立刻写出。
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //计算结果2：生成饼图
    public static void generateChart(Double v1,Double v2,Double v3,String imgName){
        DecimalFormat df = new DecimalFormat("0.00");
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Telecom  "+df.format(v1*100/(v1+v2+v3))+"%", v1);
        dataset.setValue("Mobile  "+df.format(v2*100/(v1+v2+v3))+"%", v2);
        dataset.setValue("Unicom  "+df.format(v3*100/(v1+v2+v3))+"%", v3);

        JFreeChart chart = ChartFactory.createPieChart(imgName, // chart
                dataset, // data
                true, // include legend
                true, false);
        setChart(chart);
//        PiePlot pieplot = (PiePlot) chart.getPlot();
//        pieplot.setSectionPaint("Telecom", Color.decode("#749f83"));
//        pieplot.setSectionPaint("Mobile", Color.decode("#2f4554"));
//        pieplot.setSectionPaint("Unicom", Color.decode("#61a0a8"));

        try {
//            // 创建图形显示面板
//            ChartFrame cf = new ChartFrame("柱状图", chart);
//            // cf.pack();
//            // // 设置图片大小
//            cf.setSize(600, 600);
//            // // 设置图形可见
//            cf.setVisible(true);
            // 保存图片到指定文件夹
            ChartUtilities.saveChartAsPNG(new File(imgName+".png"), chart, 1500, 800);
            System.err.println("成功");
        } catch (Exception e) {
            System.err.println("创建图形时出错");
        }
    }

    //设置饼图样式
    public static void setChart(JFreeChart chart) {
        chart.setTextAntiAlias(true);

        PiePlot pieplot = (PiePlot) chart.getPlot();
        // 设置图表背景颜色
        pieplot.setBackgroundPaint(ChartColor.WHITE);

        pieplot.setLabelBackgroundPaint(null);// 标签背景颜色

        pieplot.setLabelOutlinePaint(null);// 标签边框颜色

        pieplot.setLabelShadowPaint(null);// 标签阴影颜色

        pieplot.setOutlinePaint(null); // 设置绘图面板外边的填充颜色

        pieplot.setShadowPaint(null); // 设置绘图面板阴影的填充颜色

        pieplot.setSectionOutlinesVisible(false);

        pieplot.setNoDataMessage("没有可供使用的数据！");
    }

    //计算结果3：
    public static void writeResult3(HashMap<String,HashMap<String,Double>> sets){
        try {
            FileWriter writer = new FileWriter("out3.txt",false);
            for(String key:sets.keySet())
            {
                HashMap<String,Double> map=sets.get(key);
                Double sum=0.0;
                for(int i=1;i<=8;i++){
                    if(map.get(i+"")!=null)
                        sum+=map.get(i+"");
                    else
                        map.put(i+"",0.0);
                }
                if(sum>0){
                    writer.write("<"+key+"");
                    for(int i=1;i<=8;i++){
                        writer.write(", "+(map.get(i+"")/sum));
                    }
                    writer.write(">\n");
                }
            }
            writer.flush();//刷新内存，将内存中的数据立刻写出。
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    //合并统计结果1
    public static void combineResult1(HashMap<String,Integer> map1,HashMap<String,Integer> map2){
        for (Map.Entry<String, Integer> entry : map2.entrySet()) {
            if(map1.get(entry.getKey())==null)
                map1.put(entry.getKey(),entry.getValue());
            else
                map1.put(entry.getKey(),entry.getValue()+map1.get(entry.getKey()));
        }
    }

    //合并统计结果2
    public static void combineResult2(int[][] arr1,int[][]arr2){
        for(int i=0;i<3;i++)
            for(int j=0;j<3;j++){
                arr1[i][j]+=arr2[i][j];
            }
    }

    //合并统计结果3
    public static void combineResult3(HashMap<String,HashMap<String,Double>> map1,HashMap<String,HashMap<String,Double>> map2){
        for(Map.Entry<String,HashMap<String,Double>> entry:map2.entrySet()){
            if(map1.get(entry.getKey())==null)
                map1.put(entry.getKey(),entry.getValue());
            else{
                combineDoubleMap(map1.get(entry.getKey()),entry.getValue());
            }
        }
    }

    public static void combineDoubleMap(HashMap<String,Double> map1,HashMap<String,Double> map2){
        for (Map.Entry<String, Double> entry : map2.entrySet()) {
            if(map1.get(entry.getKey())==null)
                map1.put(entry.getKey(),entry.getValue());
            else
                map1.put(entry.getKey(),entry.getValue()+map1.get(entry.getKey()));
        }
    }

    //通话次数
    public static HashMap<String, Integer> callingTimes(String[] dataArr){
        HashMap<String,Integer> sets=new HashMap<>();
        for (int i=0;i<dataArr.length;i++) {
            String[] elements=dataArr[i].split("\\s+");
            if(sets.get(elements[1])==null){
                sets.put(elements[1],1);
            }else{
                sets.put(elements[1],sets.get(elements[1])+1);
            }
        }
        return sets;
    }

    //各运营商的占比
    public static int[][] rateOfMobileCompy(String[] dataArr){
        int[][] sets=new int[3][3];
        for(int i=0;i<dataArr.length;i++){
            String[] elements=dataArr[i].split("\\s+");
            if(elements[4]!=null && elements[12]!=null){
                sets[Integer.parseInt(elements[12])-1][Integer.parseInt(elements[4])-1]++;
            }
        }
        return sets;
    }

    //用户在各个时间段的通话时长
    public static HashMap timeRate(String[] dataArr){
        HashMap<String,HashMap<String,Double>> sets=new HashMap<>();
        for(int i=0;i<dataArr.length;i++){
            String[] elements=dataArr[i].split("\\s+");
            if(elements[1]!=null && elements[9]!=null && elements[11]!=null){
                if(sets.get(elements[1])==null){
                    HashMap<String,Double> set=new HashMap<>();
                    set.put(classify(elements[9]),Double.parseDouble(elements[11]));
                    sets.put(elements[1],set);
                }else{
                    HashMap<String,Double> set=sets.get(elements[1]);
                    String timePart=classify(elements[9]);
                    if(set.get(timePart)==null)
                        set.put(timePart,Double.parseDouble(elements[11]));
                    else
                        set.put(timePart,set.get(timePart)+Double.parseDouble(elements[11]));
                }
            }
        }
        return sets;
    }

    //    时间段 1  0:00-3:00
//    时间段 2  3:00-6:00
//    时间段 3  6:00-9:00
//    时间段 4  9:00-12:00
//    时间段 5  12:00-15:00
//    时间段 6  15:00-18:00
//    时间段 7  18:00-21:00
//    时间段 8  21:00-24:00
    public static String classify(String time){
        int hour=Integer.parseInt(time.substring(0,time.indexOf(":")));
        return ""+(hour/3+1);
    }


    //读取分布式计算文件
    public static String[] readCallingFile(String path){
        ArrayList<String> arrayList = new ArrayList<>();
        try {
            FileReader fr = new FileReader(path);
            BufferedReader bf = new BufferedReader(fr);
            String str;
            // 按行读取字符串
            while ((str = bf.readLine()) != null) {
                arrayList.add(str);
            }
            bf.close();
            fr.close();
        } catch (
                IOException e) {
            e.printStackTrace();
        }
        // 对ArrayList中存储的字符串进行处理
        int length = arrayList.size();
        String[] array = new String[length];
        for (int i = 0; i < length; i++) {
            String s = arrayList.get(i);
            array[i] = s;
        }
        // 返回数组
        return array;
    }

    //字符数组合并byte
    public static byte[] combineStringArr(String[] arr){
        String data="";
        for(String str:arr){
            data+=str;
            data+="\n";
        }
        return data.getBytes();
    }
}
