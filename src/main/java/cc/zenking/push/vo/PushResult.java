package cc.zenking.push.vo;

import lombok.Data;

@Data
public class PushResult {
	
	public static final int SUCCESS = 0;
	
	public static final String SUCCESS_REASON = "success";
	
	public static final int FAILURE = 1;
	
	private int code;
	
	private String reason;
	
	private PushResult(int code , String reason){
		this.code = code;
		this.reason = reason;
	}
	
	public static PushResult ok() {
		return new PushResult(SUCCESS, SUCCESS_REASON);
	}
	
	public static PushResult fail(String reason) {
		return new PushResult(FAILURE, reason);
	}
	
}
