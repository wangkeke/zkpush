package cc.zenking.push.service;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.JsonNode;

import cc.zenking.push.vo.OppoPayload;
import cc.zenking.push.vo.OppoPayload.Notification;
import cc.zenking.push.vo.PushResult;
import lombok.extern.java.Log;

/**
 * oppo/一加
 * @author k
 *
 */
@Log
@ConfigurationProperties("oppo")
@Component
public class OppoPushHandler extends PushHandler<OppoPayload>{
	
	public final static String ROOT_URL = "https://api.push.oppomobile.com";
	
	public final static String AUTH_PATH = "/server/v1/auth";
	
	public final static String PUSH_PATH = "/server/v1/message/notification/unicast";
	
	public final static String PATCH_PUSH_PATH = "/server/v1/message/notification/unicast_batch";
	
	private static Map<AppType, OppoATTask> atTaskMap = new HashMap<>(2);
	
	@Override
	public String matchPattern() {
		return "*((oneplus)|(oppo))*";
	}

	@Override
	public boolean init(boolean online) {
		OppoATTask edupTask = new OppoATTask(getEdupAppKey(), getEdupAppSecret());
		edupTask.start();
		atTaskMap.put(AppType.EDUP, edupTask);
		OppoATTask edutTask = new OppoATTask(getEdutAppKey(), getEdutAppSecret());
		edutTask.start();
		atTaskMap.put(AppType.EDUT, edutTask);
		return true;
	}

	@Override
	protected OppoPayload build(AppType appType, Map<String, Object> notify, String restrictedPackageName) {
		String title = (String) notify.get("title");
		String desc = (String)notify.get("description");
		OppoPayload oppoPayload = new OppoPayload();
		Notification notification = new Notification();
		oppoPayload.setNotification(notification);
		notification.setTitle(title);
		notification.setContent(desc);
		return oppoPayload;
	}

	@Override
	protected PushResult send(AppType appType, OppoPayload message, List<String> regIds) {
		if(regIds==null || regIds.isEmpty()) {
			return PushResult.ok();
		}
		OppoATTask atTask = atTaskMap.get(appType);
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>(2);
		headers.add("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE);
		headers.add("auth_token", atTask.getAuth_token());
		if(regIds.size()==1) {
			message.setTarget_value(regIds.get(0));
			try {
				String body = "target_type="+message.getTarget_type()+"&target_value="+message.getTarget_value() + 
						"&notification=" + objectMapper.writeValueAsString(message.getNotification());
				HttpEntity<String> httpEntity = new HttpEntity<String>(body, headers);
				String result = getRestTemplate().postForObject(ROOT_URL+PUSH_PATH, httpEntity, String.class);
				JsonNode jsonNode = objectMapper.readTree(result);
				int code = jsonNode.get("code").asInt();
				if(code!=0) {
					log.warning("推送Oppo通知失败：" + result);
					return PushResult.fail(jsonNode.get("message").asText());
				}
				return PushResult.ok();
			} catch (IOException e) {
				e.printStackTrace();
				return PushResult.fail("Oppo推送失败！");
			}
		}else {
			try {
				List<OppoPayload> oppoPayloads = new ArrayList<OppoPayload>(regIds.size());
				regIds.stream().distinct().forEach(r -> {
					OppoPayload oppoPayload = new OppoPayload();
					oppoPayload.setTarget_value(r);
					oppoPayload.setNotification(message.getNotification());
					oppoPayloads.add(oppoPayload);
				});
				String body = objectMapper.writeValueAsString(oppoPayloads);
				body = "messages=" + body;
				HttpEntity<String> httpEntity = new HttpEntity<String>(body , headers);
				String result = getRestTemplate().postForObject(ROOT_URL+PATCH_PUSH_PATH, httpEntity, String.class);
				JsonNode jsonNode = objectMapper.readTree(result);
				int code = jsonNode.get("code").asInt();
				if(code!=0) {
					log.warning("推送Oppo通知失败：" + result);
					return PushResult.fail(jsonNode.get("message").asText());
				}
				return PushResult.ok();
			} catch (IOException e) {
				e.printStackTrace();
				return PushResult.fail("Oppo推送失败！");
			}
		}
	}
	
	
	private class OppoATTask extends Thread{
		private String auth_token;
		private String appKey;
		private String appSecret;
		private int retry , maxRetryLimit = 10;
		
		public OppoATTask(String appKey , String appSecret) {
			this.appKey = appKey;
			this.appSecret = appSecret;
		}
		
		public String getAuth_token() {
			return auth_token;
		}

		@Override
		public void run() {
			MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>(1);
			headers.add("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE);
			while(!terminate) {
				try {						
					Long timestamp = System.currentTimeMillis();
					String sign = this.appKey + timestamp + this.appSecret;
					sign = DigestUtils.sha256Hex(sign);
					StringBuilder params = new StringBuilder()
					.append("app_key=" + this.appKey)
					.append("&sign="+sign)
					.append("&timestamp=" + timestamp);
					HttpEntity<String> httpEntity = new HttpEntity<String>(params.toString(), headers);
					String result = getRestTemplate().postForObject(new URI(ROOT_URL+AUTH_PATH), httpEntity, String.class);
					JsonNode root = objectMapper.readTree(result);
					Integer code = root.get("code").asInt();
					if(code!=0) {
						log.warning("刷新Oppo auth_token失败：" + result);
						this.retry++;
						if(this.retry>maxRetryLimit) {
							log.warning("因超过" + maxRetryLimit + "次最大尝试，不再刷新Oppo auth_token；请检查Oppo推送配置信息！");
							break;
						}
					}else {						
						this.auth_token = root.get("auth_token").asText();
						String create_time = root.get("create_time").asText();
						Long sleepTime = 24*60*60*1000L-(System.currentTimeMillis()-Long.parseLong(create_time));
						this.retry=0;
						if(sleepTime>=7200*1000) {							
							Thread.sleep(7200*1000);
						}else if(sleepTime>0){
							Thread.sleep(sleepTime);
						}
					}
				}catch (InterruptedException e) {
					log.info("中断休眠！");
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
}
