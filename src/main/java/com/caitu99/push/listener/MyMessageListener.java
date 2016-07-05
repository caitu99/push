/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.push.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.kafka.core.KafkaMessage;
import org.springframework.integration.kafka.listener.MessageListener;
import org.springframework.integration.kafka.serializer.common.StringDecoder;
import org.springframework.integration.kafka.util.MessageUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.caitu99.push.mipush.MiPushImpl;

/** 
 * 
 * @Description: (类职责详细描述,可空) 
 * @ClassName: MyMessageListener 
 * @author Hongbo Peng
 * @date 2015年11月30日 下午6:00:19 
 * @Copyright (c) 2015-2020 by caitu99 
 */
public class MyMessageListener implements MessageListener {
	private final static Logger logger = LoggerFactory.getLogger(MyMessageListener.class);
	@Autowired
	private MiPushImpl miPushImpl;
	
	@Override
	public void onMessage(KafkaMessage message) {
		try {
			String msgStr = MessageUtils.decodePayload(message, new StringDecoder());
			logger.info(msgStr);
			JSONObject jsonObject = JSON.parseObject(msgStr);
			if("PUSH_MESSAGE".equals(jsonObject.getString("pushType"))){
				String title = jsonObject.getString("title");
				String description = jsonObject.getString("description");
				String payload = jsonObject.getString("payload");
				String regId = jsonObject.getString("regId");
				Integer type = jsonObject.getInteger("type");//1.ios 2.android
				String messageCount = jsonObject.getString("messageCount");//未读消息总条数
				String redSpots = jsonObject.getString("redSpots");
				String messageId = jsonObject.getString("messageId");
				miPushImpl.sendMessageByRegId(title,description,payload, regId, type,messageCount,redSpots,messageId);
			} else if("PUSH_TOPIC".equals(jsonObject.getString("pushType"))){
				String title = jsonObject.getString("title");
				String description = jsonObject.getString("description");
				String payload = jsonObject.getString("payload");
				String messageCount = jsonObject.getString("messageCount");//未读消息总条数
				String redSpots = jsonObject.getString("redSpots");
				String messageId = jsonObject.getString("messageId");
				miPushImpl.sendBroadcast(title, description, payload,messageCount,redSpots,messageId);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("kafka消息消费发生异常：{}",e);
		}
	}

	
}
