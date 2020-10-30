package cc.zenking.push.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
public class FlymePayload implements Payload {
	
	@Setter
	private NoticeBarInfo noticeBarInfo;
	
	@Setter
	private ClickTypeInfo clickTypeInfo;
	
	private PushTimeInfo pushTimeInfo = new PushTimeInfo();
	
	
	@Data
	public static class NoticeBarInfo{
		private String title;
		private String content;
	}
	
	@Data
	public static class ClickTypeInfo{
		/**
		 * 点击动作 打开应用 打开应用页面 打开 页面应用客户端自定义 【 非必填 默认为 】
		 */
		private Integer clickType = 0;
		private String url;
		private String parameters;
		private String activity;
	}
	
	@Data
	public static class PushTimeInfo{
		private Integer offLine = 1;
		/**
		 * 有效时长 到 小时内的正整数 【 值为 时，必填，默认 】
		 */
		private Integer validTime = 72;
	}
	
}
