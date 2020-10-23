package cc.zenking.push.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import cc.zenking.push.vo.GCMPayload;
import cc.zenking.push.vo.PushResult;

/**
 * 谷歌cloud 消息推送
 * @author 
 *
 */
@ConfigurationProperties("gcm")
@Component
public class GCMPushHandler extends PushHandler<GCMPayload> {

	@Override
	public String matchPattern() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected GCMPayload build(Object notify) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PushResult send(GCMPayload payload) {
		// TODO Auto-generated method stub
		return null;
	}

	

	
	
	
	
}
