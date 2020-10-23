package cc.zenking.push.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import cc.zenking.push.vo.PushResult;
import lombok.Getter;

/**
 * 通知推送处理器
 *
 */
public abstract class PushHandler<T> {
	
	private volatile boolean initialized = false;
	
	private boolean online = false;
	
	protected String AppId;
	
	protected String AppKey;
	
	protected String AppSecret;
	
	public String getAppId() {
		return AppId;
	}
	
	public String getAppKey() {
		return AppKey;
	}
	
	public String getAppSecret() {
		return AppSecret;
	}
	
	public void setOnline(boolean online) {
		this.online = online;
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
	protected abstract T build(Map<String, Object> notify , String restrictedPackageName);
	
	/**
	 * 发送消息
	 * @param payload  转换后的消息体
	 * @return
	 */
	protected abstract PushResult send(T message ,List<String> regIds);
	
	/**
	 * 推送通知,公共调用方法
	 * @param notify 原始通知内容
	 * @return
	 */
	 public PushResult push(Map<String, Object> notify , String restrictedPackageName , List<String> regIds) {
		 if(!initialized) {
			 initialized = init(this.online);
			 if(!initialized) {
				 throw new RuntimeException("初始化推送服务失败！");
			 }
		 }
		 return send(build(notify,restrictedPackageName),regIds);
	 }
	
	/**
	 * 推送通知,公共调用方法
	 * @param notify 原始通知内容
	 * @return
	 */
	 public PushResult push(Map<String, Object> notify , String restrictedPackageName , String... regIds) {
		 if(!initialized) {
			 initialized = init(this.online);
			 if(!initialized) {
				 throw new RuntimeException("初始化推送服务失败！");
			 }
		 }
		 return send(build(notify,restrictedPackageName),Arrays.asList(regIds));
	 }
	
}
