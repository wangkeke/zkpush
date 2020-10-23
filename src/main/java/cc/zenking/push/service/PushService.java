package cc.zenking.push.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
	
	private static Map<String, PushHandler<?>> pushHandlerMap = new HashMap<>(6);
	private static final String APP_FAMILY = "EDUP";
	private static final String APP_TEACHER = "EDUT";
	
	@Value("${env.mode.online:false}")
	private boolean online;
	
	@Value("${EDUP:}")
	private String EDUP;
	
	@Value("${EDUT:}")
	private String EDUT;
	
	@Autowired
	private ApplicationContext context;
	
	
	
	@PostConstruct
	public void init() {
		context.getBeansOfType(PushHandler.class).values().forEach(h -> {
			h.setOnline(online);
			pushHandlerMap.put(h.matchPattern(), h);
		});
	}
	
	private PushHandler<?> getPushHandler(String token){
		if(StringUtils.isEmpty(token))
			return null;
		for (String pattern : pushHandlerMap.keySet()) {
			if(token.matches(pattern)) {
				return pushHandlerMap.get(pattern);
			}
		}
		return null;
	}
	
	private String getRestrictedPackageName(String app) {
		if(APP_FAMILY.equalsIgnoreCase(app)) {
			return EDUP;
		}
		if(APP_TEACHER.equalsIgnoreCase(app)) {
			return EDUT;
		}
		return null;
	}
	
	public PushResult push(String app , Map<String, Object> notify , String... token) {
		String restrictedPackageName = getRestrictedPackageName(app);
		if(restrictedPackageName==null) {
			log.warning("不存在的APP应用：" + app);
			return PushResult.fail("不存在的APP应用：" + app);
		}
		if(token.length==1) {			
			for (String t : token) {				
				PushHandler<?> pushHandler = getPushHandler(t);
				if(pushHandler==null) {
					log.warning("未找到手机服务厂商，用户手机系统 " + token + " ,要发送的通知内容：" + notify.toString());
					continue;
				}
				PushResult result = pushHandler.push(notify, restrictedPackageName, t);
				log.info("推送的通知结果：" + result.toString());
			}
			return PushResult.ok();
		}else {
			Map<PushHandler<?>, List<String>> pushTokenMap = new HashMap<PushHandler<?>, List<String>>();
			for (String t : token) {
				PushHandler<?> pushHandler = getPushHandler(t);
				if(pushHandler==null) {
					log.warning("未找到手机服务厂商，用户手机系统 " + token + " ,要发送的通知内容：" + notify.toString());
					continue;
				}
				List<String> pushTokenList = pushTokenMap.get(pushHandler);
				if(pushTokenList == null) {
					pushTokenList = new ArrayList<String>();
					pushTokenMap.put(pushHandler, pushTokenList);
				}
				pushTokenList.add(t);
			}
			if(pushTokenMap.size()==0) {
				return PushResult.fail("未找到手机服务厂商，用户手机系统 " + token + " ,要发送的" + app + "通知内容：" + notify.toString());
			}
			pushTokenMap.keySet().forEach(h -> {
				h.push(notify, restrictedPackageName, pushTokenMap.get(h));
			});
			return PushResult.ok();
		}
	}
	
}
