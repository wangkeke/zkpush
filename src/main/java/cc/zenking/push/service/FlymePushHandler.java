package cc.zenking.push.service;

import java.util.List;
import java.util.Map;

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
	protected FlymePayload build(Map<String, Object> notify, String restrictedPackageName) {
		return null;
	}

	@Override
	protected PushResult send(FlymePayload message, List<String> regIds) {
		return null;
	}

	
	
	
	
}
