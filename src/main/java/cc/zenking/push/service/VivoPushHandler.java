package cc.zenking.push.service;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cc.zenking.push.vo.PushResult;
import cc.zenking.push.vo.VivoPayload;
import lombok.extern.java.Log;

/**
 * vivo
 * @author k
 * 
 */
@Log
@ConfigurationProperties("vivo")
@Component
public class VivoPushHandler extends PushHandler<VivoPayload>{
	
	public final static String ROOT_URL = "https://api-push.vivo.com.cn";
	
	public final static String AUTH_PATH = "/message/auth";
	
	public final static String SAVELISTPAYLOAD_PATH = "/message/saveListPayload";
	
	public final static String PUSHTOLIST_PATH = "/message/pushToList";
	
	public final static String PUSH_PATH = "/message/send";
	
	private static Map<AppType, VivoATTask> atTaskMap = new HashMap<>(2);
	
	private int pushMode = 1;
	
	@Override
	public boolean init(boolean online) {
		if(online) {
			pushMode = 0;
		}else {
			pushMode = 1;
		}
		VivoATTask edupTask = new VivoATTask(Integer.parseInt(getEdupAppId()) , getEdupAppKey() , getEdupAppSecret());
		edupTask.start();
		atTaskMap.put(AppType.EDUP, edupTask);
		VivoATTask edutTask = new VivoATTask(Integer.parseInt(getEdutAppId()) , getEdutAppKey() , getEdutAppSecret());
		edutTask.start();
		atTaskMap.put(AppType.EDUT, edutTask);
		return true;
	}

	@Override
	public String matchPattern() {
		return ".*(vivo).*";
	}

	@Override
	protected VivoPayload build(AppType appType, Map<String, Object> notify, String restrictedPackageName) {
		String title = (String) notify.get("title");
		String desc = (String)notify.get("description");
		VivoPayload payload = new VivoPayload();
		payload.setContent(desc);
		payload.setTitle(title);
		payload.setPushMode(this.pushMode);
		payload.setRequestId(String.valueOf((title+desc+System.currentTimeMillis()).hashCode()));
		return payload;
	}

	@Override
	protected PushResult send(AppType appType, VivoPayload message, List<String> regIds) {
		if(regIds==null || regIds.isEmpty()) {
			return PushResult.ok();
		}
		VivoATTask atTask = atTaskMap.get(appType);
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>(2);
		headers.add("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE);
		headers.add("authToken", atTask.getAuthToken());
		if(regIds.size()==1) {
			message.setRegId(regIds.get(0));
			try {
				String body = objectMapper.writeValueAsString(message);
				HttpEntity<String> httpEntity = new HttpEntity<String>(body, headers);
				String result = getRestTemplate().postForObject(ROOT_URL+PUSH_PATH, httpEntity, String.class);
				JsonNode jsonNode = objectMapper.readTree(result);
				int code = jsonNode.get("result").asInt();
				if(code!=0) {
					log.warning("推送Vivo通知失败：" + result);
					return PushResult.fail(jsonNode.get("desc").asText());
				}
				return PushResult.ok();
			} catch (IOException e) {
				e.printStackTrace();
				return PushResult.fail("Vivo推送失败！");
			}
		}else {
			try {
				String body = objectMapper.writeValueAsString(message);
				HttpEntity<String> httpEntity = new HttpEntity<String>(body, headers);
				String result = getRestTemplate().postForObject(ROOT_URL+SAVELISTPAYLOAD_PATH, httpEntity, String.class);
				JsonNode jsonNode = objectMapper.readTree(result);
				int code = jsonNode.get("result").asInt();
				if(code!=0) {
					log.warning("推送Vivo通知失败：" + result);
					return PushResult.fail(jsonNode.get("desc").asText());
				}
				String taskId = jsonNode.get("taskId").asText();
				
				
				ObjectNode objectNode = objectMapper.createObjectNode();
				ArrayNode arrayNode = objectNode.putArray("regIds");
				regIds.stream().distinct().forEach(r -> {
					arrayNode.add(r);
				});
				objectNode.put("taskId", taskId);
				objectNode.put("requestId", (taskId+System.currentTimeMillis()).hashCode()+"");
				objectNode.put("pushMode", this.pushMode);
				httpEntity = new HttpEntity<String>(objectNode.toString() , headers);
				result = getRestTemplate().postForObject(ROOT_URL+PUSHTOLIST_PATH, httpEntity, String.class);
				jsonNode = objectMapper.readTree(result);
				code = jsonNode.get("result").asInt();
				if(code!=0) {
					log.warning("推送Vivo通知失败：" + result);
					return PushResult.fail(jsonNode.get("desc").asText());
				}
				return PushResult.ok();
			} catch (IOException e) {
				e.printStackTrace();
				return PushResult.fail("Vivo推送失败！");
			}
		}
	}
	
	private class VivoATTask extends Thread{
		private String authToken;
		private Integer appId;
		private String appKey;
		private String appSecret;
		private int retry , maxRetryLimit = 10;
		
		public VivoATTask(Integer appId , String appKey , String appSecret) {
			this.appId = appId;
			this.appKey = appKey;
			this.appSecret = appSecret;
		}
		
		public String getAuthToken() {
			return authToken;
		}

		@Override
		public void run() {
			ObjectNode objectNode = objectMapper.createObjectNode();
			objectNode.put("appId", this.appId);
			objectNode.put("appKey", this.appKey);
			while(!terminate) {
				try {						
					Long timestamp = System.currentTimeMillis();
					String sign = this.appId + this.appKey + timestamp + this.appSecret;
					sign = DigestUtils.md5DigestAsHex(sign.getBytes());
					objectNode.put("timestamp", timestamp);
					objectNode.put("sign", sign);
					
					MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>(2);
					headers.add("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE);
					HttpEntity<String> httpEntity = new HttpEntity<String>(objectNode.toString(), headers);
					String result = getRestTemplate().postForObject(new URI(ROOT_URL+AUTH_PATH), httpEntity, String.class);
					JsonNode root = objectMapper.readTree(result);
					Integer code = root.get("result").asInt();
					if(code!=0) {
						log.warning("刷新Vivo authToken失败：" + result);
						this.retry++;
						if(this.retry>maxRetryLimit) {
							log.warning("因超过" + maxRetryLimit + "次最大尝试，不再刷新Vivo authToken；请检查Vivo推送配置信息！");
							break;
						}
					}else {						
						this.authToken = root.get("authToken").asText();
						this.retry=0;
						Thread.sleep(7200*1000);
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
