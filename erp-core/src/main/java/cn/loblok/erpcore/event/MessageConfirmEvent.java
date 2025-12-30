package cn.loblok.erpcore.event;

import lombok.Data;

/**
 * 消息确认事件
 */
@Data
public class MessageConfirmEvent {
    private final String bizId;
    private final boolean ack;
    private final String cause;

    public MessageConfirmEvent(String bizId, boolean ack, String cause) {
        this.bizId = bizId;
        this.ack = ack;
        this.cause = cause;
    }
}