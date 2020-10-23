package cc.zenking.push.service;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import cc.zenking.push.vo.OppoPayload;
import cc.zenking.push.vo.PushResult;

/**
 * oppo/一加
 * @author 
 *
 */
@ConfigurationProperties("oppo")
@Component
public class OppoPushHandler extends PushHandler<OppoPayload>{

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
	protected OppoPayload build(Map<String, Object> notify, String restrictedPackageName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PushResult send(OppoPayload message, List<String> regIds) {
		// TODO Auto-generated method stub
		return null;
	}
	


}
