/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.push.mipush;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.caitu99.push.util.PropertiesUtil;
import com.caitu99.push.util.http.HttpClientUtils;
import com.xiaomi.push.sdk.ErrorCode;
import com.xiaomi.xmpush.server.Constants;
import com.xiaomi.xmpush.server.Message;
import com.xiaomi.xmpush.server.Result;
import com.xiaomi.xmpush.server.Sender;

/** 
 * 
 * @Description: (类职责详细描述,可空) 
 * @ClassName: MiPushImpl 
 * @author Hongbo Peng
 * @date 2015年12月1日 下午6:23:11 
 * @Copyright (c) 2015-2020 by caitu99 
 */
@Service("miPushImpl")
public class MiPushImpl {
	
	private final static Logger logger = LoggerFactory.getLogger(MiPushImpl.class);
	
	/**
	 * @Description: (单推消息)  
	 * @Title: sendMessageByRegId 
	 * @param title
	 * @param payload
	 * @param description
	 * @param regId
	 * @param type
	 * @throws Exception
	 * @date 2015年12月1日 下午10:12:35  
	 * @author Hongbo Peng
	 */
	public void sendMessageByRegId(String title,String description,String payload,String regId,Integer type,
			String messageCount, String redSpots,String messageId) throws Exception{
		logger.info("push title:{} description:{} payload:{} regId:{} type:{} messageCount:{} redSpots:{} messageId:{}",
				title,description, payload, regId, type,messageCount,redSpots,messageId);
		String env = PropertiesUtil.getContexrtParam("env");
		if("3".equals(env) || 2 == type){
			//正式环境
			Constants.useOfficial();
			logger.info("push message to product");
		} else {
			//测试环境
			Constants.useSandbox();
			logger.info("push message to dev");
		}
	    Sender sender = null;
	    Message msg = null;
	    if(1 == type){
	    	msg = buildIOSMessage(description,messageCount,redSpots,messageId);
	    	String iosAppSecretKey = PropertiesUtil.getContexrtParam("iosAppSecretKey");
	    	sender = new Sender(iosAppSecretKey);
	    } else {
	    	String androidAppSecretKey = PropertiesUtil.getContexrtParam("androidAppSecretKey");
	    	msg = buildMessage(title,description,payload,messageCount,redSpots,messageId);
	    	sender = new Sender(androidAppSecretKey);
	    }
	    Result result = sender.send(msg, regId,0);
	    if(ErrorCode.Success != result.getErrorCode()){
	    	logger.error("push message failed ! title:{} description:{} payload: {} regId:{} type:{} reason:{}",title,description,payload,regId,type,result.getReason());
	    	//TODO push失败，可能被卸载，需要尝试短信发送
	    	String url = PropertiesUtil.getContexrtParam("service.url") + PropertiesUtil.getContexrtParam("unify.sms.message.url");
			Map<String,String> paramMap = new HashMap<String,String>();
			paramMap.put("messageId", messageId);
			HttpClientUtils.getInstances().doPost(url, "UTF-8",paramMap);
	    }else{
	    	logger.info("push message success, MessageId:{}",result.getMessageId());
	    }
	}
	
	/**
	 * @Description: (群发消息)  
	 * @Title: sendBroadcast 
	 * @param message
	 * @throws Exception
	 * @date 2015年12月1日 下午8:03:30  
	 * @author Hongbo Peng
	 */
	public void sendBroadcast(String title,String description,String payload,
			String messageCount, String redSpots,String messageId) throws Exception{
		String env = PropertiesUtil.getContexrtParam("env");
		if("3".equals(env)){
			//正式环境
			Constants.useOfficial();
			logger.info("push topic message to product");
		} else {
			//测试环境
			Constants.useSandbox();
			logger.info("push topic message to dev");
		}
		String iosAppSecretKey = PropertiesUtil.getContexrtParam("iosAppSecretKey");
		Sender iosSender = new Sender(iosAppSecretKey);
		//IOS topic推送
	    Message iosMsg = buildIOSMessage(description,messageCount,redSpots,messageId);
	    String iosTopic = PropertiesUtil.getContexrtParam("iosTopic");
	    Result iosResult = iosSender.broadcast(iosMsg, iosTopic, 0);
	    if(ErrorCode.Success != iosResult.getErrorCode()){
	    	logger.error("push ios topic message failed ! description:{} reason:{}",description,iosResult.getReason());
	    }else{
	    	logger.info("push ios topic message success, MessageId:{}",iosResult.getMessageId());
	    }
	    String androidAppSecretKey = PropertiesUtil.getContexrtParam("androidAppSecretKey");
	    Sender androidSender = new Sender(androidAppSecretKey);
	    //android topic推送 TODO Android没有测试环境，根据topic区分正式测试
	    Constants.useOfficial();
	    Message androidMsg = buildMessage(title,description,payload,messageCount,redSpots,messageId);
	    String androidTopic = PropertiesUtil.getContexrtParam("androidTopic");
	    Result androidResult = androidSender.broadcast(androidMsg, androidTopic, 0);
	    if(ErrorCode.Success != androidResult.getErrorCode()){
	    	logger.error("push android topic message failed ! title:{} description:{} payload:{} reason:{}",title,description,payload,androidResult.getReason());
	    }else{
	    	logger.info("push android topic message success, MessageId:{}",androidResult.getMessageId());
	    }
	}
	
	/**
	 * @Description: (构建IOS消息)  
	 * @Title: buildIOSMessage 
	 * @param description 设置在通知栏展示的通知的描述
	 * @return
	 * @throws Exception
	 * @date 2015年12月1日 下午8:01:03  
	 * @author Hongbo Peng
	 */
	private Message buildIOSMessage(String description,String messageCount,String redSpots,String messageId) throws Exception {
	     Message message = new Message.IOSBuilder()
	             .description(description)
	             .soundURL("default")    // 消息铃声
	             .badge(Integer.valueOf(messageCount))               // 数字角标
	             .category("action")     // 快速回复类别
//	             .extra("callback", "callback")  //TODO 消息送达和点击回执 通知地址
//	             .extra("callback.type", "3") //送达和点击回执
	             .extra("messageCount", messageCount)//未读消息条数
	             .extra("redSpots", redSpots)//小红点
	             .extra("messageId", messageId)
	             .build();
	     return message;
	}
	/**
	 * @Description: (构建android消息)  
	 * @Title: buildMessage 
	 * @param title 设置在通知栏展示的通知的标题，不允许全是空白字符，长度小于16，中英文均以一个计算。
	 * @param description 设置在通知栏展示的通知的描述，不允许全是空白字符，长度小于128，中英文均以一个计算。
	 * @param payload 设置要发送的消息内容payload，不允许全是空白字符，长度小于4K，中英文均以一个计算
	 * @return
	 * @throws Exception
	 * @date 2015年12月1日 下午8:00:26  
	 * @author Hongbo Peng
	 */
	private Message buildMessage(String title,String description,String payload,String messageCount,String redSpots,String messageId) throws Exception{
		String appPackageName = PropertiesUtil.getContexrtParam("appPackageName");
	    Message message = new Message.Builder()
	             .title(title)
	             .description(description).payload(payload)
	             .restrictedPackageName(appPackageName)
	             .passThrough(0)  //消息使用1.透传方式 0.通知栏
	             .notifyType(1)     // 使用默认提示音提示
//	             .extra("callback", "callback")  //TODO 消息送达和点击回执 通知地址
//	             .extra("callback.type", "3") //送达和点击回执
	             .extra("messageCount", messageCount)//未读消息条数
	             .extra("redSpots", redSpots)//小红点
	             .extra("messageId", messageId)
	             .extra(Constants.EXTRA_PARAM_NOTIFY_EFFECT, Constants.NOTIFY_LAUNCHER_ACTIVITY)
	             .build();
	     return message;
	}
}
