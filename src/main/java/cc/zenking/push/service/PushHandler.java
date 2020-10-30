package cc.zenking.push.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import cc.zenking.push.vo.PushResult;

/**
 * 通知推送处理器
 *
 */
public abstract class PushHandler<T> {
	
	private volatile boolean initialized = false;
	
	private boolean online = false;
	
	protected boolean terminate = false;
	
	protected String edupAppId;
	
	protected String edupAppKey;
	
	protected String edupAppSecret;
	
	protected String edutAppId;
	
	protected String edutAppKey;
	
	protected String edutAppSecret;
	
	@Autowired
	protected ObjectMapper objectMapper;
	
	@Autowired
	private RestTemplate restTemplate;
	
	

	public String getEdupAppId() {
		return edupAppId;
	}

	public void setEdupAppId(String edupAppId) {
		this.edupAppId = edupAppId;
	}

	public String getEdupAppKey() {
		return edupAppKey;
	}

	public void setEdupAppKey(String edupAppKey) {
		this.edupAppKey = edupAppKey;
	}

	public String getEdupAppSecret() {
		return edupAppSecret;
	}

	public void setEdupAppSecret(String edupAppSecret) {
		this.edupAppSecret = edupAppSecret;
	}

	public String getEdutAppId() {
		return edutAppId;
	}

	public void setEdutAppId(String edutAppId) {
		this.edutAppId = edutAppId;
	}

	public String getEdutAppKey() {
		return edutAppKey;
	}

	public void setEdutAppKey(String edutAppKey) {
		this.edutAppKey = edutAppKey;
	}

	public String getEdutAppSecret() {
		return edutAppSecret;
	}

	public void setEdutAppSecret(String edutAppSecret) {
		this.edutAppSecret = edutAppSecret;
	}
	
	public RestTemplate getRestTemplate() {
		return restTemplate;
	}
	
	/**
	 * 初始化方法
	 * @param online  是否为生产环境
	 * @return  初始化是否成功
	 */
	public abstract boolean init(boolean online);
	
	/**
	 * 字符串的匹配模式
	 * @return  匹配模式
	 */
	public abstract String matchPattern();
	
	/**
	 * 构建对应的消息体
	 * @param notify  原始通知
	 * @return
	 */
	protected abstract T build(AppType appType, Map<String, Object> notify , String restrictedPackageName);
	
	/**
	 * 发送消息
	 * @param payload  转换后的消息体
	 * @return
	 */
	protected abstract PushResult send(AppType appType , T message ,List<String> regIds);
	
	/**
	 * 推送通知,公共调用方法
	 * @param notify 原始通知内容
	 * @return
	 */
	 public PushResult push(AppType appType , Map<String, Object> notify , String restrictedPackageName , List<String> regIds) {
		 if(!initialized) {
			 initialized = init(this.online);
			 if(!initialized) {
				 throw new RuntimeException("初始化推送服务失败！");
			 }
		 }
		 return send(appType , build(appType , notify,restrictedPackageName),regIds);
	 }
	
	/**
	 * 推送通知,公共调用方法
	 * @param notify 原始通知内容
	 * @return
	 */
	 public PushResult push(AppType appType , Map<String, Object> notify , String restrictedPackageName , String... regIds) {
		 return push(appType, notify, restrictedPackageName, Arrays.asList(regIds));
	 }

	 
	@PreDestroy
	public void destory() {
		this.terminate = true;
	}
	 
}
