package cc.zenking.push.vo;

import lombok.Data;

@Data
public class VivoPayload implements Payload {
	
	private String regId;
	
	private Integer notifyType = 1;
	
	private String title;
	
	private String content;
	
	/**
	 * 消息保留时长 单位：秒，取值至少60秒，最长7天。当值为空时，默认一天
	 */
	private Integer timeToLive = 3600*24*7;
	
	/**
	 * 点击跳转类型 1：打开APP首页 2：打开链接 3：自定义 4:打开app内指定页面
	 */
	private Integer skipType = 1;
	
	/**
	 * 跳转内容 跳转类型为2时，跳转内容最大1000个字符，跳转类型为3或4时，跳转内容最大1024个字符，skipType传3需要在onNotificationMessageClicked回调函数中自己写处理逻辑。
	 */
	private String skipContent;
	
	/**
	 * 
	 */
	private String requestId;
	
	/**
	 * 推送模式 0：正式推送；1：测试推送，不填默认为0（测试推送，只能给web界面录入的测试用户推送；审核中应用，只能用测试推送）
	 */
	private Integer pushMode;
	
}
