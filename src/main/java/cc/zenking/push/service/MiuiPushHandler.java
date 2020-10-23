package cc.zenking.push.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import cc.zenking.push.vo.MiuiPayload;
import cc.zenking.push.vo.PushResult;

/**
 * 小米
 * @author 
 *
 */
@ConfigurationProperties("miui")
@Component
public class MiuiPushHandler extends PushHandler<MiuiPayload> {

	@Override
	public String matchPattern() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected MiuiPayload build(Object notify) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PushResult send(MiuiPayload payload) {
		// TODO Auto-generated method stub
		return null;
	}

}
