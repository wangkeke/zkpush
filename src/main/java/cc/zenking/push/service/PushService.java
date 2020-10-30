package cc.zenking.push.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
	
	@Value("${env.mode.online:false}")
	private boolean online;
	
	@Value("${edup.packageName:}")
	private String edupPackageName;
	
	@Value("${edut.packageName:}")
	private String edutPackageName;
	
	@Autowired
	private ApplicationContext context;
	
	
	
	@PostConstruct
	public void init() {
		context.getBeansOfType(PushHandler.class).values().forEach(h -> {
			h.init(this.online);
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
	
	private String getRestrictedPackageName(AppType appType) {
		if(AppType.EDUP == appType) {
			return edupPackageName;
		}
		if(AppType.EDUT == appType) {
			return edutPackageName;
		}
		return null;
	}
	
	/**
	 * 针对用户token的推送
	 * @param app   APP标识  
	 * @param notify
	 * @param token
	 * @return
	 */
	public PushResult push(AppType appType , Map<String, Object> notify , String... token) {
		String restrictedPackageName = getRestrictedPackageName(appType);
		if(restrictedPackageName==null) {
			log.warning("不存在的APP应用：" + appType);
			return PushResult.fail("不存在的APP应用：" + appType);
		}
		if(token.length==1) {			
			for (String t : token) {				
				PushHandler<?> pushHandler = getPushHandler(t);
				if(pushHandler==null) {
					log.warning("未找到手机服务厂商，用户手机系统 " + token + " ,要发送的通知内容：" + notify.toString());
					continue;
				}
				PushResult result = pushHandler.push(appType , notify, restrictedPackageName, t);
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
				return PushResult.fail("未找到手机服务厂商，用户手机系统 " + token + " ,要发送的" + appType + "通知内容：" + notify.toString());
			}
			pushTokenMap.keySet().forEach(h -> {
				h.push(appType , notify, restrictedPackageName, pushTokenMap.get(h));
			});
			return PushResult.ok();
		}
	}
	
}
