package cc.zenking.push.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import cc.zenking.push.vo.HuaweiPayload;
import cc.zenking.push.vo.PushResult;

/**
 * 华为
 * @author 
 *
 */
@ConfigurationProperties("huawei")
@Component
public class HuaweiPushHandler extends PushHandler<HuaweiPayload> {

	@Override
	public String matchPattern() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected HuaweiPayload build(Object notify) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PushResult send(HuaweiPayload payload) {
		// TODO Auto-generated method stub
		return null;
	}



}
