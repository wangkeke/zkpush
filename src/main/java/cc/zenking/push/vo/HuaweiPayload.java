package cc.zenking.push.vo;

import java.util.List;

import lombok.Data;

@Data
public class HuaweiPayload implements Payload {
	
	private boolean validate_only;
	private HuaweiPayload.Message message;
	
	
	@Data
	public static class Message{
		private HuaweiPayload.Notification notification;
		private Object data;
		private List<String> token;
	} 
	
	@Data
	public static class Notification{
		private String title;
		private String body;
	}
	
}
