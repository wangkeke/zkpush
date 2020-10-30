package cc.zenking.push.vo;

import lombok.Data;

@Data
public class OppoPayload implements Payload {
	
	private Short target_type = 2;
	
	private String target_value;
	
	private Notification notification;
	
	@Data
	public static class Notification{
		private String app_message_id;
		private String title;
		private String content;
		/**
		 * 点击动作类型0，启动应用；1，打开应用内页（activity的intent action）；2，打开网页；4，打开应用内页（activity）；【非必填，默认值为0】;5,Intent scheme URL
		 */
		private Integer click_action_type = 0;
		/**
		 * 应用内页地址
		 */
		private String click_action_activity;
		/**
		 * 网页地址或【click_action_type为2与5时必填，长度500】
			示例：
			click_action_type为2时http://oppo.com?key1=val1&key2=val2
			
			click_action_type为5时command://test?key1=val1&key2=val2
		 */
		private String click_action_url;
		
		/**
		 * 离线消息的存活时间(time_to_live) (单位：秒), 【最长10天】
		 */
		private Integer off_line_ttl = 3600 * 24 * 7;
	}
	
}
