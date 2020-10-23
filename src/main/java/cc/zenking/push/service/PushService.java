package cc.zenking.push.service;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import cc.zenking.push.vo.Payload;
import cc.zenking.push.vo.PushResult;
import lombok.extern.java.Log;
/**
 * android手机通知推送服务
 * @author 
 *
 */
@ConditionalOnBean(PushHandler.class)
@Service
@Log
public class PushService {
	
	private static Map<String, PushHandler<? extends Payload>> pushHandlerMap = new HashMap<>();
	
	@Autowired
	private ApplicationContext context;
	
	
	
	@PostConstruct
	public void init() {
		context.getBeansOfType(PushHandler.class).values().forEach(h -> {
			pushHandlerMap.put(h.matchPattern(), h);
		});
	}
	
	private PushHandler<? extends Payload> getPushHandler(String systemTag){
		if(StringUtils.isEmpty(systemTag))
			return null;
		for (String pattern : pushHandlerMap.keySet()) {
			if(systemTag.matches(pattern)) {
				return pushHandlerMap.get(pattern);
			}
		}
		return null;
	}
	
	public void push(String systemTag , Object notify) {
		PushHandler<? extends Payload> pushHandler = getPushHandler(systemTag);
		if(pushHandler==null) {
			log.warning("未找到手机服务厂商，用户手机系统 " + systemTag + " ,要发送的通知内容：" + notify.toString());
			return;
		}
		PushResult result = pushHandler.push(notify);
		log.info("推送的通知结果：" + result.toString());
	}
	
}
