package cn.kdream.netty.pojo;

/**
 * @Description 封装页面传过来的数据信息
 * @Author Bxsheng
 * @BoolAddress kdream.cn
 * @Date 2020-08-13
 */
public class DataContent {
    private Integer action;		// 动作类型
    private ChatMsg chatMsg;	// 用户的聊天内容entity
    private String extand;		// 扩展字段

    public Integer getAction() {
        return action;
    }

    public void setAction(Integer action) {
        this.action = action;
    }

    public ChatMsg getChatMsg() {
        return chatMsg;
    }

    public void setChatMsg(ChatMsg chatMsg) {
        this.chatMsg = chatMsg;
    }

    public String getExtand() {
        return extand;
    }

    public void setExtand(String extand) {
        this.extand = extand;
    }

    @Override
    public String toString() {
        return "DataContent{" +
                "action=" + action +
                ", chatMsg=" + chatMsg +
                ", extand='" + extand + '\'' +
                '}';
    }
}
