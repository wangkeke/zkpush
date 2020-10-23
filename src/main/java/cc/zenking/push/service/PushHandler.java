package cc.zenking.push.service;

import cc.zenking.push.vo.Payload;
import cc.zenking.push.vo.PushResult;
import lombok.Data;

/**
 * 通知推送处理器
 *
 */
@Data
public abstract class PushHandler<T extends Payload> {
	
	protected String AppId;
	
	protected String AppKey;
	
	protected String AppSecret;
	
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
	protected abstract T build(Object notify);
	
	/**
	 * 发送消息
	 * @param payload  转换后的消息体
	 * @return
	 */
	protected abstract PushResult send(T payload);
	
	/**
	 * 推送通知,公共调用方法
	 * @param notify 原始通知内容
	 * @return
	 */
	 public PushResult push(Object notify) {
		 Object payload = build(notify);
		 return send((T)payload);
	 }
	
}
