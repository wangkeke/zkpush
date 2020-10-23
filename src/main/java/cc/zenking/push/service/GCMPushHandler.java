package cc.zenking.push.service;

import java.util.List;
import java.util.Map;

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
	public boolean init(boolean online) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected GCMPayload build(Map<String, Object> notify, String restrictedPackageName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PushResult send(GCMPayload message, List<String> regIds) {
		// TODO Auto-generated method stub
		return null;
	}

	

	
	
	
	
}
