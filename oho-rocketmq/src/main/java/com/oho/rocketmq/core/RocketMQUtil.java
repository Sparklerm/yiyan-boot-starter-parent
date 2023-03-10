package com.oho.rocketmq.core;

import com.oho.common.exception.Asserts;
import com.oho.common.utils.StringUtils;
import com.oho.rocketmq.core.model.MQMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RocketMQ 消息工具类
 *
 * @author MENGJIAO
 */
@Slf4j
@Component
public class RocketMQUtil {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 消息超时时间
     */
    private final long timeout = rocketMQTemplate.getProducer().getSendMsgTimeout();

    private String checkDestination(String destination) {
        if (StringUtils.isBlank(destination)) {
            Asserts.fail("ROCKETMQ 消息参数错误 ：destination 为空");
        }
        return destination;
    }

    /**
     * 发送同步消息，使用有序发送请设置HashKey
     *
     * @param messageBody the message body
     */
    public void syncSend(MQMessage messageBody) {
        String destination = checkDestination(messageBody.getDestination());
        log.info("ROCKETMQ 同步消息发送 ：[{}] - [{}] - [{}]", destination, messageBody.getMessageId(), messageBody);
        Message<Object> message = MessageBuilder.withPayload(messageBody.getContent()).build();
        SendResult sendResult;
        if (StringUtils.isNotBlank(messageBody.getHashKey())) {
            sendResult = rocketMQTemplate.syncSendOrderly(destination, message, messageBody.getHashKey(), timeout);
        } else {
            sendResult = rocketMQTemplate.syncSend(destination, message, timeout, messageBody.getDelayLevel().getLevel());
        }
        log.info("ROCKETMQ 同步消息发送结果 ：[{}] - [{}] - [{}]", destination, messageBody.getMessageId(), sendResult);
    }

    /**
     * 批量发送同步消息
     *
     * @param messageBody the message body
     */
    public void syncSendBatch(MQMessage messageBody) {
        String destination = checkDestination(messageBody.getDestination());
        log.info("ROCKETMQ 同步消息-批量发送 ：[{}] - [{}] - [{}]", destination, messageBody.getMessageId(), messageBody.getContents());
        List<Message<Object>> messageList = messageBody.getContents()
                .stream()
                .map(content -> MessageBuilder.withPayload(content).build())
                .collect(Collectors.toList());
        SendResult sendResult;
        if (StringUtils.isNotBlank(messageBody.getHashKey())) {
            sendResult = rocketMQTemplate.syncSendOrderly(destination, messageList, messageBody.getHashKey());
        } else {
            sendResult = rocketMQTemplate.syncSend(destination, messageList);
        }
        log.info("ROCKETMQ 同步消息-批量发送结果 ：[{}] - [{}] - [{}]", destination, messageBody.getMessageId(), sendResult);
    }

    /**
     * 异步发送消息，异步返回消息结果
     *
     * @param messageBody the message body
     */
    public void asyncSend(MQMessage messageBody) {
        String destination = checkDestination(messageBody.getDestination());
        log.info("ROCKETMQ 异步消息发送 ：[{}] - [{}] - [{}]", destination, messageBody.getMessageId(), messageBody);
        Message<Object> message = MessageBuilder.withPayload(messageBody.getContent()).build();
        if (StringUtils.isNotBlank(messageBody.getHashKey())) {
            rocketMQTemplate.asyncSendOrderly(destination, message, messageBody.getHashKey(),
                    asyncSendCallback(destination, messageBody.getMessageId()));
        } else {
            rocketMQTemplate.asyncSend(destination, message,
                    asyncSendCallback(destination, messageBody.getMessageId()), timeout, messageBody.getDelayLevel().getLevel());
        }
    }

    /**
     * 批量异步发送消息
     *
     * @param messageBody the message body
     */
    public void asyncSendBatch(MQMessage messageBody) {
        String destination = checkDestination(messageBody.getDestination());
        log.info("ROCKETMQ 异步消息-批量发送 ：[{}] - [{}] - [{}]", destination, messageBody.getMessageId(), messageBody.getContents());
        List<Message<Object>> messageList = messageBody.getContents()
                .stream()
                .map(content -> MessageBuilder.withPayload(content).build())
                .collect(Collectors.toList());

        if (StringUtils.isNotBlank(messageBody.getHashKey())) {
            rocketMQTemplate.asyncSendOrderly(destination, messageList, messageBody.getHashKey(),
                    asyncSendCallback(destination, messageBody.getMessageId()));
        } else {
            rocketMQTemplate.asyncSend(destination, messageList,
                    asyncSendCallback(destination, messageBody.getMessageId()));
        }
    }

    private static SendCallback asyncSendCallback(String destination, String messageId) {
        return new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("ROCKETMQ 异步消息发送成功 ： [{}] - [{}] - [{}]", destination, messageId, sendResult.getTransactionId());
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("ROCKETMQ 异步消息发送失败 ：[{}] - [{}] - [{}]", destination, messageId, throwable);
            }
        };
    }

    /**
     * 单向发送消息，不关心返回结果，容易消息丢失，适合日志收集、不精确统计等消息发送;
     *
     * @param messageBody the message body
     */
    public void sendOneWay(MQMessage messageBody) {
        String destination = checkDestination(messageBody.getDestination());
        log.info("ROCKETMQ 单向消息发送 ：[{}] - [{}] - [{}]", destination, messageBody.getMessageId(), messageBody);
        Message<Object> message = MessageBuilder.withPayload(messageBody.getContent()).build();
        if (StringUtils.isNotBlank(messageBody.getHashKey())) {
            rocketMQTemplate.sendOneWayOrderly(destination, message, messageBody.getHashKey());
        } else {
            rocketMQTemplate.sendOneWay(destination, message);
        }
    }

    /**
     * 批量单向发送消息
     *
     * @param messageList the message list
     */
    public void sendOneWayBatch(List<MQMessage> messageList) {
        for (MQMessage messageBody : messageList) {
            sendOneWay(messageBody);
        }
    }

}
