package cc.zenking.push.service;

import java.util.List;
import java.util.Map;

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
	public boolean init(boolean online) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected VivoPayload build(Map<String, Object> notify, String restrictedPackageName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PushResult send(VivoPayload message, List<String> regIds) {
		// TODO Auto-generated method stub
		return null;
	}



}
