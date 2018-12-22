package rpc.handler;

import io.netty.channel.ChannelHandlerContext;
import node.NodeClient;
import node.requestpojo.DistributeCalculateMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.common.IMessageHandler;
import rpc.common.MessageOutput;

import java.util.*;

import static node.DistributeCalculation.*;
import static node.NodeContext.*;

public class DistributeCalculateHandler implements IMessageHandler<DistributeCalculateMessage> {
    private final static Logger LOG = LoggerFactory.getLogger(DistributeCalculateMessage.class);

    @Override
    public void handle(ChannelHandlerContext ctx, String requestId, DistributeCalculateMessage message) {
        ArrayList finalResult=new ArrayList();

        HashMap<String,Integer> myResult1,result1;
        int[][] myResult2,result2;
        HashMap<String,HashMap> myResult3,result3;

        String messageId=message.getMessageId();
        String srcIp=message.getSrcIp();
        String[] data=message.getData();

        if(messageSearched.containsKey(messageId)){
            ctx.writeAndFlush(new MessageOutput(requestId, "callingTimes_res", null));
            return;
        }
        messageSearched.put(messageId, 1);

        //继续分割
        if(neighbors.size()>1){
            int length=(int)Math.floor(data.length/(neighbors.size()-1));
            //此节点处理部分
            String[] myPart= Arrays.copyOfRange(data,0,length);
            String[] otherPart=Arrays.copyOfRange(data,length,data.length);

            myResult1=callingTimes(myPart);
            myResult2=rateOfMobileCompy(myPart);
            myResult3=timeRate(myPart);
            //切割继续分发
            int id=1;
            Iterator entries = neighbors.entrySet().iterator();
            String[] sendData;

            while (entries.hasNext()) {
                Map.Entry entry = (Map.Entry) entries.next();
                //该节点为发送信息的节点
                if(entry.getKey()==srcIp)
                    continue;
                NodeClient client = (NodeClient)entry.getValue();
                //防止最后遍历的节点为发送信息的节点
                if(entries.hasNext()&& id!=neighbors.size()-1)
                    sendData=Arrays.copyOfRange(otherPart,(id-1)*length,id*length);
                else
                    sendData=Arrays.copyOfRange(otherPart,(id-1)*length,otherPart.length);
                id++;
                ArrayList result = client.distributeCalculate(new DistributeCalculateMessage(messageId,LOCAL_IP,sendData));
                if(result==null && sendData!=null)
                {
                    result1=callingTimes(sendData);
                    result2=rateOfMobileCompy(sendData);
                    result3=timeRate(sendData);
                }else{
                    result1=(HashMap<String,Integer>)result.get(0);
                    result2=(int[][]) result.get(1);
                    result3=(HashMap<String,HashMap>)result.get(2);
                }

                combineResult1(myResult1,result1);
                combineResult2(myResult2,result2);
                combineResult3(myResult3,result3);
            }
        }else{
            myResult1=callingTimes(data);
            myResult2=rateOfMobileCompy(data);
            myResult3=timeRate(data);
        }
        finalResult.add(myResult1);
        finalResult.add(myResult2);
        finalResult.add(myResult3);

        LOG.info("calculate calling complete");
        ctx.writeAndFlush(new MessageOutput(requestId, "callingTimes_res", finalResult));
    }

}
