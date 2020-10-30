package cc.zenking.push.service;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.JsonNode;

import cc.zenking.push.vo.PushResult;
import lombok.extern.java.Log;

/**
 * 小米
 * @author 
 *
 */
@Log
@ConfigurationProperties("miui")
@Component
public class MiuiPushHandler extends PushHandler<Map<String, String>> {
	
	private static final String ROOT_URL = "https://api.xmpush.xiaomi.com";
	private static final String PUSH_PATH = "/v3/message/regid";
	private static MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>(2);
	
	public boolean init(boolean online) {
		headers.add("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE);
		return true;
	}
	
	@Override
	public String matchPattern() {
		return ".*((xiaomi)|(redmi)|(miui)).*";
	}

	@Override
	protected PushResult send(AppType appType , Map<String, String> message, List<String> regIds) {
		if(regIds==null || regIds.size()==0)
			return PushResult.ok();
		if(appType == AppType.EDUP) {
			headers.add("Authorization", "key="+getEdupAppSecret());
		}else if(appType == AppType.EDUT) {
			headers.add("Authorization", "key="+getEdutAppSecret());
		}else {
			throw new RuntimeException("不支持的app类型：" + appType + "！");
		}
		String pushIds = regIds.stream().distinct().collect(Collectors.joining(","));
		message.put("registration_id", pushIds);
		try {
			String body = message.keySet().stream().map(k -> k+"="+message.get(k)).collect(Collectors.joining("&"));
			body = URLEncoder.encode(body,"UTF-8");
			HttpEntity<String> httpEntity = new HttpEntity<String>(body, headers);
			String result = getRestTemplate().postForObject(ROOT_URL+PUSH_PATH, httpEntity, String.class);
			JsonNode jsonNode = objectMapper.readTree(result);
			String code = jsonNode.get("result").asText();
			if(!"ok".equalsIgnoreCase(code)) {
				log.warning("推送Miui通知失败：" + result);
				return PushResult.fail(jsonNode.get("reason").asText());
			}
			return PushResult.ok();
		} catch (IOException e) {
			e.printStackTrace();
			return PushResult.fail("Miui推送失败！");
		}
		
	}

	@Override
	protected Map<String, String> build(AppType appType ,Map<String, Object> notify, String restrictedPackageName) {
		String title = (String) notify.get("title");
		String desc = (String)notify.get("description");
		Integer notifyId = (desc+"."+System.currentTimeMillis()).hashCode();
		Map<String, String> params = new HashMap<String, String>();
		params.put("restricted_package_name", restrictedPackageName);
		params.put("pass_through","0");
		params.put("title",title);
		params.put("description",desc);
		params.put("time_to_live",(1000*3600*24*7)+"");
		params.put("notify_id", notifyId+"");
		return params;
	}

}
