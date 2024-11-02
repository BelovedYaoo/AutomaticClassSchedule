package top.belovedyaoo.acs;

import cn.dev33.satoken.jwt.SaJwtUtil;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import cn.hutool.jwt.JWT;
import com.ejlchina.okhttps.OkHttps;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * Sa-OAuth2 Client端 控制器 
 * @author click33 
 */
@RestController
public class SaOAuthClientController {

	// 相关参数配置 
	private final String clientId = "1001";								// 应用id
	private final String clientSecret = "aaaa-bbbb-cccc-dddd-eeee";		// 应用秘钥
	private final String serverUrl = "http://openiam.top:8090";	// 服务端接口

	/**
	 * 根据Code码进行登录，获取 Access-Token 和 openid
 	 * @param code
	 * @return
	 * @throws JsonProcessingException
	 */
	@RequestMapping("/codeLogin")
	public SaResult codeLogin(String code) throws JsonProcessingException {
		// 调用Server端接口，获取 Access-Token 以及其他信息 
		String str = OkHttps.sync(serverUrl + "/oauth2/token")
				.addBodyPara("grant_type", "authorization_code")
				.addBodyPara("code", code)
				.addBodyPara("client_id", clientId)
				.addBodyPara("client_secret", clientSecret)
				.post()
				.getBody()
				.toString();
		SoMap so = SoMap.getSoMap().setJsonString(str);
		System.out.println("返回结果: " + new ObjectMapper().writeValueAsString(so));
		SoMap result = new SoMap().setMap(JWT.of(so.getString("id_token")).getPayloads().getRaw());

		// code不等于200  代表请求失败
		if(so.getInt("code") != 200) {
			return SaResult.error(so.getString("msg"));
		}

		String open_id = result.getString("sub");
		so.set("open_id", open_id);
		
		// 返回相关参数 
		StpUtil.login(open_id);
		so.set("tokenValue", StpUtil.getTokenValue());
		return SaResult.data(so);
	}
	
	// 根据 Refresh-Token 去刷新 Access-Token 
	@RequestMapping("/refresh")
	public SaResult refresh(String refreshToken) throws JsonProcessingException {
		// 调用Server端接口，通过 Refresh-Token 刷新出一个新的 Access-Token 
		String str = OkHttps.sync(serverUrl + "/oauth2/refresh")
				.addBodyPara("grant_type", "refresh_token")
				.addBodyPara("client_id", clientId)
				.addBodyPara("client_secret", clientSecret)
				.addBodyPara("refresh_token", refreshToken)
				.post()
				.getBody()
				.toString();
		SoMap so = SoMap.getSoMap().setJsonString(str);
		System.out.println("返回结果: " + new ObjectMapper().writeValueAsString(so));
		
		// code不等于200  代表请求失败 
		if(so.getInt("code") != 200) {
			return SaResult.error(so.getString("msg"));
		}

		// 返回相关参数
		return SaResult.data(so);
	}
	
	// 模式三：密码式-授权登录
	@RequestMapping("/passwordLogin")
	public SaResult passwordLogin(String username, String password) throws JsonProcessingException {
		// 模式三：密码式-授权登录
		String str = OkHttps.sync(serverUrl + "/oauth2/token")
				.addBodyPara("grant_type", "password")
				.addBodyPara("client_id", clientId)
				.addBodyPara("client_secret", clientSecret)
				.addBodyPara("username", username)
				.addBodyPara("password", password)
				.post()
				.getBody()
				.toString();
		SoMap so = SoMap.getSoMap().setJsonString(str);
		System.out.println("返回结果: " + new ObjectMapper().writeValueAsString(so));
		
		// code不等于200  代表请求失败 
		if(so.getInt("code") != 200) {
			return SaResult.error(so.getString("msg"));
		}

		String open_id = so.getString("open_id");
		so.set("open_id", open_id);
		
		// 返回相关参数 
		StpUtil.login(open_id);
		return SaResult.data(so);
	}
	
	// 模式四：获取应用的 Client-Token 
	@RequestMapping("/clientToken")
	public SaResult clientToken() throws JsonProcessingException {
		// 调用Server端接口
		String str = OkHttps.sync(serverUrl + "/oauth2/client_token")
				.addBodyPara("grant_type", "client_credentials")
				.addBodyPara("client_id", clientId)
				.addBodyPara("client_secret", clientSecret)
				.post()
				.getBody()
				.toString();
		SoMap so = SoMap.getSoMap().setJsonString(str);
		System.out.println("返回结果: " + new ObjectMapper().writeValueAsString(so));
		
		// code不等于200  代表请求失败 
		if(so.getInt("code") != 200) {
			return SaResult.error(so.getString("msg"));
		}

		// 返回相关参数
		return SaResult.data(so);
	}
	
	// 注销登录 
	@RequestMapping("/logout")
	public SaResult logout() {
		StpUtil.logout();
		return SaResult.ok();
	}

	// 根据 Access-Token 置换相关的资源: 获取账号昵称、头像、性别等信息 
	@RequestMapping("/getUserinfo")
	public SaResult getUserinfo(String accessToken) throws JsonProcessingException {
		// 调用Server端接口，查询开放的资源 
		String str = OkHttps.sync(serverUrl + "/oauth2/userinfo")
				.addBodyPara("access_token", accessToken)
				.post()
				.getBody()
				.toString();
		SoMap so = SoMap.getSoMap().setJsonString(str);
		System.out.println("返回结果: " + new ObjectMapper().writeValueAsString(so));
		
		// code不等于200  代表请求失败 
		if(so.getInt("code") != 200) {
			return SaResult.error(so.getString("msg"));
		}

		// 返回相关参数 (data=获取到的资源 )
		return SaResult.data(so);
	}
	
	// 全局异常拦截 
	@ExceptionHandler
	public SaResult handlerException(Exception e) {
		e.printStackTrace(); 
		return SaResult.error(e.getMessage());
	}

}