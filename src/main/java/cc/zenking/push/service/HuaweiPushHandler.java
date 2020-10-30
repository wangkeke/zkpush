package cc.zenking.push.service;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import cc.zenking.push.vo.HuaweiPayload;
import cc.zenking.push.vo.HuaweiPayload.Message;
import cc.zenking.push.vo.HuaweiPayload.Notification;
import cc.zenking.push.vo.PushResult;
import lombok.extern.java.Log;

/**
 * 华为
 * @author 
 *
 */
@Log
@ConfigurationProperties("huawei")
@Component
public class HuaweiPushHandler extends PushHandler<HuaweiPayload>{
	
	private static final String HUAWEI_CLIENTCREDENTIALS_URL = "https://oauth-login.cloud.huawei.com/oauth2/v3/token";
	
	private static final String HUAWEI_PUSHMESSAGE_URL = "https://push-api.cloud.huawei.com/v1/{appid}/messages:send";
	
	private static Map<AppType, HuaweiATTask> atTaskMap = new HashMap<>(2);
	
	private boolean validate_only = true;

	
	@Override
	public String matchPattern() {
		return ".*((huawei)|(honor)|(emiu)).*";
	}
	
	@Override
	public boolean init(boolean online) {
		if(online) {
			validate_only = false;
		}
		HuaweiATTask edupATTask = new HuaweiATTask(getEdupAppId(), getEdupAppSecret());
		edupATTask.start();
		atTaskMap.put(AppType.EDUP, edupATTask);
		HuaweiATTask edutATTask = new HuaweiATTask(getEdutAppId(), getEdutAppSecret());
		edutATTask.start();
		atTaskMap.put(AppType.EDUT, edutATTask);
		return true;
	}

	@Override
	protected HuaweiPayload build(AppType appType , Map<String, Object> notify, String restrictedPackageName) {
		HuaweiPayload payload = new HuaweiPayload();
		payload.setValidate_only(validate_only);
		Message message = new Message();
		payload.setMessage(message);
		String title = (String) notify.get("title");
		String desc = (String)notify.get("description");
		Notification notification = new Notification();
		message.setNotification(notification);
		notification.setTitle(title);
		notification.setBody(desc);
		return payload;
	}

	@Override
	protected PushResult send(AppType appType , HuaweiPayload payload, List<String> regIds) {
		payload.getMessage().setToken(regIds);
		HuaweiATTask atTask = atTaskMap.get(appType);
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>(2);
		headers.add("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE);
		headers.add("Authorization", atTask.getToken_type()+" " + atTask.getAccess_token());
		HttpEntity<String> httpEntity;
		try {
			httpEntity = new HttpEntity<String>(objectMapper.writeValueAsString(payload), headers);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		ResponseEntity<String> result = getRestTemplate().postForEntity(HUAWEI_PUSHMESSAGE_URL, httpEntity, String.class,AppType.EDUP==appType?getEdupAppId():getEdutAppId());
		try {
			JsonNode root = objectMapper.readTree(result.getBody());
			String code = root.get("code").asText();
			if("200".equals(code) || "80000000".equals(code)) {						
				return PushResult.ok();			
			}else {
				return PushResult.fail(root.get("msg").asText("推送失败！"));
			}
		} catch (IOException e) {
			e.printStackTrace();
			return PushResult.fail("Huawei推送失败！");
		}
	}
	
	private class HuaweiATTask extends Thread{
		private HttpEntity<String> httpEntity;
		private String access_token;
		private String token_type;
		private int retry , maxRetryLimit = 10;
		
		public HuaweiATTask(String appId , String appSecret) {
			HttpHeaders headers = new HttpHeaders();
			headers.add("grant_type", "client_credentials");
			headers.add("client_id", appId);
			headers.add("client_secret", appSecret);
			this.httpEntity = new HttpEntity<String>(headers);
		}
		
		public String getAccess_token() {
			return access_token;
		}
		
		public String getToken_type() {
			return token_type;
		}

		@Override
		public void run() {
			while(!terminate) {
				try {						
					String result = getRestTemplate().postForObject(new URI(HUAWEI_CLIENTCREDENTIALS_URL), httpEntity, String.class);
					JsonNode root = objectMapper.readTree(result);
					String code = root.get("code").asText();
					if("200".equals(code) || "80000000".equals(code)) {						
						access_token = root.get("access_token").asText();
						token_type = root.get("token_type").asText();
						int expires_in = root.get("expires_in").asInt();
						this.retry = 0;
						Thread.sleep((expires_in-3)*1000);
					}else {
						log.warning("刷新Huawei access_token失败：" + result);
						this.retry++;
						if(this.retry>maxRetryLimit) {
							log.warning("因超过" + maxRetryLimit + "次最大尝试，不再刷新Huawei access_token；请检查Huawei推送配置信息！");
							break;
						}
					}
				}catch (InterruptedException e) {
					log.info("中断休眠！");
				}catch (HttpClientErrorException e) {
					log.warning("刷新Huawei access_token失败：" + e.getResponseBodyAsString());
					this.retry++;
					if(this.retry>maxRetryLimit) {
						log.warning("因超过" + maxRetryLimit + "次最大尝试，不再刷新Huawei access_token；请检查Huawei推送配置信息！");
						break;
					}
				}				
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
	}

}
