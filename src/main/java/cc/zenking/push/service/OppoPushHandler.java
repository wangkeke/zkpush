package cc.zenking.push.service;

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
	protected OppoPayload build(Object notify) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PushResult send(OppoPayload payload) {
		// TODO Auto-generated method stub
		return null;
	}
	


}
