package cc.zenking.push.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import cc.zenking.push.vo.PushResult;
import cc.zenking.push.vo.VivoPayload;

/**
 * vivo
 * @author 
 *
 */
@ConfigurationProperties("vivo")
@Component
public class VivoPushHandler extends PushHandler<VivoPayload>{

	@Override
	public String matchPattern() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected VivoPayload build(Object notify) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PushResult send(VivoPayload payload) {
		// TODO Auto-generated method stub
		return null;
	}
	


}
