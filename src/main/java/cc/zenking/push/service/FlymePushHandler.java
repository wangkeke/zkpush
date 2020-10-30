package cc.zenking.push.service;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.JsonNode;

import cc.zenking.push.vo.FlymePayload;
import cc.zenking.push.vo.FlymePayload.NoticeBarInfo;
import cc.zenking.push.vo.PushResult;
import lombok.extern.java.Log;

/**
 * 魅族
 * @author 
 *
 */
@Log
@ConfigurationProperties("flyme")
@Component
public class FlymePushHandler extends PushHandler<FlymePayload> {
	private static final String ROOT_URL = "http://server-api-push.meizu.com";
	private static final String PUSH_PATH = "/garcia/api/server/push/varnished/pushByPushId";
	private static MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>(1);
	
	@Override
	public String matchPattern() {
		return "*((flyme)|(meizu))*";
	}

	@Override
	public boolean init(boolean online) {
		headers.add("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE);
		return true;
	}

	@Override
	protected FlymePayload build(AppType appType, Map<String, Object> notify, String restrictedPackageName) {
		String title = (String) notify.get("title");
		String desc = (String)notify.get("description");
		FlymePayload payload = new FlymePayload();
		NoticeBarInfo noticeBarInfo = new NoticeBarInfo();
		noticeBarInfo.setContent(desc);
		noticeBarInfo.setTitle(title);
		payload.setNoticeBarInfo(noticeBarInfo);
		return payload;
	}

	@Override
	protected PushResult send(AppType appType, FlymePayload message, List<String> regIds) {
		if(regIds==null || regIds.isEmpty())
			return PushResult.ok();
		TreeMap<String, String> sortParams = new TreeMap<String, String>();
		String secret = null;
		if(appType==AppType.EDUP) {			
			sortParams.put("appId", getEdupAppId());
			secret = getEdupAppSecret();
		}else if(appType==AppType.EDUT) {
			sortParams.put("appId", getEdutAppId());
			secret = getEdutAppSecret();
		}else {
			throw new RuntimeException("不支持的APP类型：" + appType);
		}
		String pushIds = regIds.stream().distinct().collect(Collectors.joining(","));
		sortParams.put("pushIds", pushIds);
		String messageJson;
		try {
			messageJson = objectMapper.writeValueAsString(message);
			sortParams.put("messageJson", messageJson);
			String sign = getSignature(sortParams, secret);
			sortParams.put("sign", sign);
			String body = sortParams.keySet().stream().map(k -> k+"="+sortParams.get(k)).collect(Collectors.joining("&"));
			body = URLEncoder.encode(body,"UTF-8");
			HttpEntity<String> httpEntity = new HttpEntity<String>(body, headers);
			String result = getRestTemplate().postForObject(ROOT_URL+PUSH_PATH, httpEntity, String.class);
			JsonNode jsonNode = objectMapper.readTree(result);
			String code = jsonNode.get("code").asText();
			if(!"200".equals(code)) {
				log.warning("推送Flyme通知失败：" + result);
				return PushResult.fail(jsonNode.get("message").asText());
			}
			return PushResult.ok();
		} catch (IOException e) {
			e.printStackTrace();
			return PushResult.fail("Flyme推送失败！");
		}
	}
	
	private String getSignature(TreeMap<String, String> sortParams , String secret) {
		StringBuilder builder = new StringBuilder();
		sortParams.keySet().stream().forEach(k -> {
			builder.append(k).append("=").append(sortParams.get(k));
		});
		builder.append(secret);
		return MD5Encoder.encode(builder.toString().getBytes());
	}
	
}
