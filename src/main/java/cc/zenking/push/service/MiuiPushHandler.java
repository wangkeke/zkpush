package cc.zenking.push.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.json.simple.parser.ParseException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.xiaomi.push.sdk.ErrorCode;
import com.xiaomi.xmpush.server.Constants;
import com.xiaomi.xmpush.server.Message;
import com.xiaomi.xmpush.server.Message.Builder;
import com.xiaomi.xmpush.server.Result;
import com.xiaomi.xmpush.server.Sender;

import cc.zenking.push.vo.MiuiPayload;
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
public class MiuiPushHandler extends PushHandler<Message> {
	
	public boolean init(boolean online) {
		if(online) {
			Constants.useOfficial();
		}else {
			Constants.useSandbox();
		}
		return true;
	}
	
	@Override
	public String matchPattern() {
		return ".*((xiaomi)|(redmi)|(miui)).*";
	}

	@Override
	protected PushResult send(Message message, List<String> regIds) {
		if(regIds.size()==0)
			return PushResult.ok();
		Sender sender = new Sender(getAppSecret());
		try {
			Result result = null;
			result = sender.send(message, regIds, 3);
			ErrorCode errorCode = result.getErrorCode();
			if(errorCode.getValue() == ErrorCode.Success.getValue()) {				
				return PushResult.ok();
			}
			log.warning("小米通知发送失败！" + message.toString() + " : " + errorCode.getDescription());
			return PushResult.fail("小米通知发送失败：" + result.getErrorCode());
		} catch (Exception e) {
			e.printStackTrace();
			return PushResult.fail("小米通知发送失败！");
		}
	}

	@Override
	protected Message build(Map<String, Object> notify, String restrictedPackageName) {
		String title = (String) notify.get("title");
		String desc = (String)notify.get("description");
		Integer notifyId = (desc+System.currentTimeMillis()).hashCode();
		return new Message.Builder()
				 .title(title)
				 .description(desc)
				 .notifyId(notifyId)
				 .restrictedPackageName(restrictedPackageName)
				 .passThrough(0)
				 .notifyType(-1)
				 .extra(Constants.EXTRA_PARAM_NOTIFY_FOREGROUND, "0")
				 .extra(Constants.EXTRA_PARAM_NOTIFY_EFFECT, Constants.NOTIFY_LAUNCHER_ACTIVITY)
				 .build();
	}

}
