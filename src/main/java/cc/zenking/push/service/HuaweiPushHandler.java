package cc.zenking.push.service;

import java.util.List;
import java.util.Map;

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
	public boolean init(boolean online) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected HuaweiPayload build(Map<String, Object> notify, String restrictedPackageName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PushResult send(HuaweiPayload message, List<String> regIds) {
		// TODO Auto-generated method stub
		return null;
	}




}
