package node;

import node.requestpojo.DistributeCalculateMessage;
import rpc.common.RequestId;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import static node.NodeContext.*;

public class DistributeCalculation {

    //统计平均通话次数
    public static void caculateResult(String path,int numberOfDays){
        String[] data=readCallingFile(path);
        if(data!=null){
            //第一份取1/4
            int length1=(int)Math.floor(data.length*5/6);
            String[] myPart= Arrays.copyOfRange(data,0,length1);

            HashMap<String,Integer> myResult1=callingTimes(myPart);
            int[][] myResult2=rateOfMobileCompy(myPart);
            HashMap<String,HashMap> myResult3=timeRate(myPart);

//            System.out.println(myResult);
//            System.out.println(neighbors.size());
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

                ArrayList result = client.distributeCalculate(new DistributeCalculateMessage(messageId,LOCAL_IP,sendData));
                System.out.println(result);
                //                combineMap(myResult,result);
            }

            //do write result into file
//            System.out.println(myResult);
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
    public static void combineResult3(HashMap<String,HashMap> map1,HashMap<String,HashMap> map2){
        for(Map.Entry<String,HashMap> entry:map2.entrySet()){
            if(map1.get(entry.getKey())==null)
                map1.put(entry.getKey(),entry.getValue());
            else{
                combineResult1(map1.get(entry.getKey()),entry.getValue());
            }
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
        HashMap<String,HashMap<String,Integer>> sets=new HashMap<>();
        for(int i=0;i<dataArr.length;i++){
            String[] elements=dataArr[i].split("\\s+");
            if(elements[1]!=null && elements[9]!=null){
                if(sets.get(elements[1])==null){
                    HashMap<String,Integer> set=new HashMap<>();
                    set.put(classify(elements[9]),1);
                    sets.put(elements[1],set);
                }else{
                    HashMap<String,Integer> set=sets.get(elements[1]);
                    String timePart=classify(elements[9]);
                    if(set.get(timePart)==null)
                        set.put(timePart,1);
                    else
                        set.put(timePart,set.get(timePart)+1);
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
        return ""+Math.floor(hour/3)+1;
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
