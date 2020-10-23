package cc.zenking.push.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import cc.zenking.push.vo.FlymePayload;
import cc.zenking.push.vo.PushResult;

/**
 * 魅族
 * @author 
 *
 */
@ConfigurationProperties("flyme")
@Component
public class FlymePushHandler extends PushHandler<FlymePayload> {
	
	private String AppId;
	
	private String AppKey;
	
	private String AppSecret;

	@Override
	public String matchPattern() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected FlymePayload build(Object notify) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PushResult send(FlymePayload payload) {
		// TODO Auto-generated method stub
		return null;
	}	

	
	
	
	
}
