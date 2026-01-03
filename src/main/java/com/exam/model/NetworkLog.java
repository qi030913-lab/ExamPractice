package com.exam.model;

import java.time.LocalDateTime;

/**
 * 网络通信日志实体类
 * 
 * @author 在线考试系统开发组
 * @version 1.0
 */
public class NetworkLog {
    
    private Integer logId;              // 日志ID
    private Integer studentId;          // 学生ID
    private String messageType;         // 消息类型：SEND/RECEIVE/SYSTEM
    private String messageContent;      // 消息内容
    private LocalDateTime createTime;   // 创建时间
    
    public NetworkLog() {
    }
    
    public NetworkLog(Integer studentId, String messageType, String messageContent) {
        this.studentId = studentId;
        this.messageType = messageType;
        this.messageContent = messageContent;
    }
    
    public Integer getLogId() {
        return logId;
    }
    
    public void setLogId(Integer logId) {
        this.logId = logId;
    }
    
    public Integer getStudentId() {
        return studentId;
    }
    
    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }
    
    public String getMessageType() {
        return messageType;
    }
    
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
    
    public String getMessageContent() {
        return messageContent;
    }
    
    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    @Override
    public String toString() {
        return "NetworkLog{" +
                "logId=" + logId +
                ", studentId=" + studentId +
                ", messageType='" + messageType + '\'' +
                ", messageContent='" + messageContent + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
