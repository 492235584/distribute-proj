package rpc.handler;

import io.netty.channel.ChannelHandlerContext;
import node.NodeClient;
import node.requestpojo.caculateCallingMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.common.IMessageHandler;
import rpc.common.MessageOutput;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static node.NodeContext.*;

public class caculateCallingHandler implements IMessageHandler<caculateCallingMessage> {
    private final static Logger LOG = LoggerFactory.getLogger(caculateCallingHandler.class);

    @Override
    public void handle(ChannelHandlerContext ctx, String requestId, caculateCallingMessage message) {
        HashMap<String,Integer> myResult;

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
            myResult=callingTimes(myPart);
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
                HashMap<String,Integer> result = client.caculateCallingTimes(new caculateCallingMessage(messageId,LOCAL_IP,sendData));
                if(result==null && sendData!=null)
                {
                    result=callingTimes(sendData);
                }
                combineMap(myResult,result);
            }

        }else{
            myResult=callingTimes(data);
        }

        LOG.info("caculate calling complete");
        ctx.writeAndFlush(new MessageOutput(requestId, "callingTimes_res", myResult));
    }

}

