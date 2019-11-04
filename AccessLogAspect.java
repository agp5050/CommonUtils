/*

 */
package com.jffox.cloud.search.core.aop;/*

 */
package com.agp.cloud.search.core.aop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.agp.cloud.search.feign.clients.DataxRealTimeFeign;
import com.agp.cloud.search.feign.clients.SearchRedisFeign;
import com.agp.cloud.search.support.base.model.RequestConditions;
import com.agp.cloud.search.support.base.model.Result;
import com.agp.cloud.search.support.base.model.SearchParams;
import com.agp.cloud.search.support.dto.LogInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.agp.cloud.search.support.Constants.DEFAULT_REDIS_TEMPLATE_KEY;
import static com.agp.cloud.search.support.Constants.REDIS_PREFIX_EXCEPTION_LOG;

/**
 * 访问记录 切面.
 * 
 * @author L.Yang
 * @version v1.0 2017年4月11日 下午1:43:48
 */
@Slf4j
@Aspect
@Component
public class AccessLogAspect {

	private ThreadLocal<LogInfo> accessLog = new ThreadLocal<>();

	private ThreadLocal<RequestConditions> requestCondition = new ThreadLocal<>();

	private static final String[] INCLUSIONS = {"ApiController.queryData"};

	private ObjectMapper objectMapper;

	@Value("${app.to-kafka.url}")
	private String toKafkaUrl;

	@Value("${app.to-kafka.topic}")
	private String toKafkaTopic;

	@Value("${app.to-kafka.log-topic}")
	private String toKafkaLogTopic;

	private RestTemplate restTemplate;

	private SearchRedisFeign searchRedisFeign;

	private DataxRealTimeFeign dataxRealTimeFeign;

	@Pointcut("execution(public * com.agp.cloud.search.core.controller..*.*(..))")
	public void accessLog() {
	}

	/**
	 * 执行前操作.
	 * 
	 * @param joinPoint
	 *            切点对象
	 * @throws Throwable
	 *             方法异常
	 */
	@Before("accessLog()")
	public void doBefore(JoinPoint joinPoint) throws Throwable {
		String access = getClassMethod(joinPoint.getSignature());
		if (isNeedLog(access)) {
			LogInfo call = new LogInfo();
			call.setStartTime(System.currentTimeMillis());
            call.setTimeIndex(currentTimeIndex());
			HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
					.getRequestAttributes()).getRequest();
			call.setCallIp(request.getRemoteAddr());
			InetAddress address = InetAddress.getLocalHost();
			String uri = request.getScheme() + "://" + address.getHostAddress() + ":" + request.getServerPort();
			call.setServerIp(uri);
			call.setCallUrl(request.getServletPath());
			accessLog.set(call);
		}
	}

	/**
	 * 处理请求入参.
	 *
	 * @param access  切点
	 * @param args  入参数组
	 * @param call  日志对象
	 * @throws JsonProcessingException
	 */
	private void handleInParams(String access, Object[] args, LogInfo call) throws Exception {
		if (args[0] instanceof RequestConditions) {
			RequestConditions conditions = (RequestConditions) args[0];
			requestCondition.set(conditions);
			call.setRequestId(conditions.getRequestId());
			call.setAppId(conditions.getAppId());
			call.setCode(conditions.getSources().get(0).getCode());
		}
		call.setParams(objectMapper.writeValueAsString(args[0]));
	}

	/**
	 * 执行完成后操作.
	 * 
	 * @param ret
	 *            操作结果
	 * @throws Throwable
	 *             方法异常
	 */
	@AfterReturning(returning = "ret", pointcut = "accessLog()")
	public void doAfterReturning(JoinPoint point, Object ret) throws Throwable {
		String access = getClassMethod(point.getSignature());
		if (isNeedLog(access)) {
			LogInfo call = accessLog.get();
			long duration = System.currentTimeMillis() - call.getStartTime();
			call.setDuration((int) duration);
			new Thread(() -> {
				try {
					handleInParams(access, point.getArgs(), call);
					String requestId = call.getRequestId();
					log.info("请求ID：{}", requestId);
					String redisKey = REDIS_PREFIX_EXCEPTION_LOG + requestId;
					List<String> exceptionInfos = searchRedisFeign.listAll(DEFAULT_REDIS_TEMPLATE_KEY, redisKey);
					searchRedisFeign.delete(DEFAULT_REDIS_TEMPLATE_KEY, redisKey);
					if (CollectionUtils.isNotEmpty(exceptionInfos)) {
						call.setIsException((byte)1);
						call.setExceptionInfo(StringUtils.join(exceptionInfos, "\r\n"));
					}
					if (ret != null) {
						Result result = (Result)ret;
						try {
							if (!result.isSuccess() || result.getData() == null ||
									isEmptyData((Map<String, List<Map<String, String>>>) result.getData())) {
								call.setHasResult((byte)0);
							}
						} catch (Exception e) {
							log.error("{}", e);
							log.error("请求返回的结果：{}", ret.toString());
							call.setHasResult((byte)1);
						}
					} else {
						call.setHasResult((byte)0);
					}
					Map<String, Object> msg = new HashMap<>();
					RequestConditions condition = requestCondition.get();
					msg.put("request", condition);
					msg.put("response", ret);
					msg.put("apiKey", "765");
					msg.put("timestamp", System.currentTimeMillis());
					msg.put("transNo", condition.getRequestId());
					SearchParams params = condition.getSources().get(0);
					msg.putAll(params.getParams());
					sendMsgToKafka(toKafkaLogTopic, msg);
					dataxRealTimeFeign.addLog(call);
				} catch (Exception e) {
					log.error("{}", e);
				} finally {
					requestCondition.remove();
				}
			}).start();
			accessLog.remove();
		}
	}

	/**
	 * 往Kafka发送消息.
	 *
	 * @param topic
	 * @param message
	 */
	private void sendMsgToKafka(String topic, Object message) {
		Map<String, Object> data = new HashMap<>();
		data.put("topic", topic);
		data.put("msg", message);
		restTemplate.postForObject(toKafkaUrl, data, Map.class);
	}

	/**
	 * 判断是否真正返回数据.
	 *
	 * @param data
	 * @return
	 */
	private boolean isEmptyData(Map<String, List<Map<String, String>>> data) {
		if (MapUtils.isEmpty(data)) {
			return true;
		}
		for (Map.Entry<String, List<Map<String, String>>> detail : data.entrySet()) {
			List<Map<String, String>> detailData = detail.getValue();
			if (CollectionUtils.isNotEmpty(detailData) && MapUtils.isNotEmpty(detailData.get(0))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 获取方法调用名称.
	 * 
	 * @param signature
	 *            Represents the signature at a join point.
	 * @return 调用方法名称
	 */
	private String getClassMethod(Signature signature) {
		return signature.getDeclaringTypeName() + "." + signature.getName();
	}

	/**
	 * 是否需进行日志输出.
	 *
	 * <pre>
	 * 	如果在排除列表则进行输出。
	 * </pre>
	 *
	 * @param access
	 *            访问控制器名称
	 * @return 是否排除
	 */
	private boolean isNeedLog(String access) {
		boolean isInclude = false;
		for (String exclusion : INCLUSIONS) {
			isInclude = access.contains(exclusion);
			if (isInclude)
				break;
		}
		return isInclude;
	}

	/**
	 * 获取当前时间在96点时刻中的索引.
	 *
	 * @return
	 */
	private int currentTimeIndex() {
		Calendar now = Calendar.getInstance();
		int hour = now.get(Calendar.HOUR_OF_DAY);
		int minutes = now.get(Calendar.MINUTE);
        return hour * 4 + Math.floorDiv(minutes, 15) + 1;
	}

	@Autowired
	public AccessLogAspect(ObjectMapper objectMapper, RestTemplate restTemplate,
						   SearchRedisFeign searchRedisFeign, DataxRealTimeFeign dataxRealTimeFeign) {
		this.objectMapper = objectMapper;
		this.restTemplate = restTemplate;
		this.searchRedisFeign = searchRedisFeign;
		this.dataxRealTimeFeign = dataxRealTimeFeign;
	}
}


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jffox.cloud.search.feign.clients.DataxRealTimeFeign;
import com.jffox.cloud.search.feign.clients.SearchRedisFeign;
import com.jffox.cloud.search.support.base.model.RequestConditions;
import com.jffox.cloud.search.support.base.model.Result;
import com.jffox.cloud.search.support.base.model.SearchParams;
import com.jffox.cloud.search.support.dto.LogInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jffox.cloud.search.support.Constants.DEFAULT_REDIS_TEMPLATE_KEY;
import static com.jffox.cloud.search.support.Constants.REDIS_PREFIX_EXCEPTION_LOG;

/**
 * 访问记录 切面.
 * 
 * @author L.Yang
 * @version v1.0 2017年4月11日 下午1:43:48
 */
@Slf4j
@Aspect
@Component
public class AccessLogAspect {

	private ThreadLocal<LogInfo> accessLog = new ThreadLocal<>();

	private ThreadLocal<RequestConditions> requestCondition = new ThreadLocal<>();

	private static final String[] INCLUSIONS = {"ApiController.queryData"};

	private ObjectMapper objectMapper;

	@Value("${app.to-kafka.url}")
	private String toKafkaUrl;

	@Value("${app.to-kafka.topic}")
	private String toKafkaTopic;

	@Value("${app.to-kafka.log-topic}")
	private String toKafkaLogTopic;

	private RestTemplate restTemplate;

	private SearchRedisFeign searchRedisFeign;

	private DataxRealTimeFeign dataxRealTimeFeign;

	@Pointcut("execution(public * com.jffox.cloud.search.core.controller..*.*(..))")
	public void accessLog() {
	}

	/**
	 * 执行前操作.
	 * 
	 * @param joinPoint
	 *            切点对象
	 * @throws Throwable
	 *             方法异常
	 */
	@Before("accessLog()")
	public void doBefore(JoinPoint joinPoint) throws Throwable {
		String access = getClassMethod(joinPoint.getSignature());
		if (isNeedLog(access)) {
			LogInfo call = new LogInfo();
			call.setStartTime(System.currentTimeMillis());
            call.setTimeIndex(currentTimeIndex());
			HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
					.getRequestAttributes()).getRequest();
			call.setCallIp(request.getRemoteAddr());
			InetAddress address = InetAddress.getLocalHost();
			String uri = request.getScheme() + "://" + address.getHostAddress() + ":" + request.getServerPort();
			call.setServerIp(uri);
			call.setCallUrl(request.getServletPath());
			accessLog.set(call);
		}
	}

	/**
	 * 处理请求入参.
	 *
	 * @param access  切点
	 * @param args  入参数组
	 * @param call  日志对象
	 * @throws JsonProcessingException
	 */
	private void handleInParams(String access, Object[] args, LogInfo call) throws Exception {
		if (args[0] instanceof RequestConditions) {
			RequestConditions conditions = (RequestConditions) args[0];
			requestCondition.set(conditions);
			call.setRequestId(conditions.getRequestId());
			call.setAppId(conditions.getAppId());
			call.setCode(conditions.getSources().get(0).getCode());
		}
		call.setParams(objectMapper.writeValueAsString(args[0]));
	}

	/**
	 * 执行完成后操作.
	 * 
	 * @param ret
	 *            操作结果
	 * @throws Throwable
	 *             方法异常
	 */
	@AfterReturning(returning = "ret", pointcut = "accessLog()")
	public void doAfterReturning(JoinPoint point, Object ret) throws Throwable {
		String access = getClassMethod(point.getSignature());
		if (isNeedLog(access)) {
			LogInfo call = accessLog.get();
			long duration = System.currentTimeMillis() - call.getStartTime();
			call.setDuration((int) duration);
			new Thread(() -> {
				try {
					handleInParams(access, point.getArgs(), call);
					String requestId = call.getRequestId();
					log.info("请求ID：{}", requestId);
					String redisKey = REDIS_PREFIX_EXCEPTION_LOG + requestId;
					List<String> exceptionInfos = searchRedisFeign.listAll(DEFAULT_REDIS_TEMPLATE_KEY, redisKey);
					searchRedisFeign.delete(DEFAULT_REDIS_TEMPLATE_KEY, redisKey);
					if (CollectionUtils.isNotEmpty(exceptionInfos)) {
						call.setIsException((byte)1);
						call.setExceptionInfo(StringUtils.join(exceptionInfos, "\r\n"));
					}
					if (ret != null) {
						Result result = (Result)ret;
						try {
							if (!result.isSuccess() || result.getData() == null ||
									isEmptyData((Map<String, List<Map<String, String>>>) result.getData())) {
								call.setHasResult((byte)0);
							}
						} catch (Exception e) {
							log.error("{}", e);
							log.error("请求返回的结果：{}", ret.toString());
							call.setHasResult((byte)1);
						}
					} else {
						call.setHasResult((byte)0);
					}
					Map<String, Object> msg = new HashMap<>();
					RequestConditions condition = requestCondition.get();
					msg.put("request", condition);
					msg.put("response", ret);
					msg.put("apiKey", "765");
					msg.put("timestamp", System.currentTimeMillis());
					msg.put("transNo", condition.getRequestId());
					SearchParams params = condition.getSources().get(0);
					msg.putAll(params.getParams());
					sendMsgToKafka(toKafkaLogTopic, msg);
					dataxRealTimeFeign.addLog(call);
				} catch (Exception e) {
					log.error("{}", e);
				} finally {
					requestCondition.remove();
				}
			}).start();
			accessLog.remove();
		}
	}

	/**
	 * 往Kafka发送消息.
	 *
	 * @param topic
	 * @param message
	 */
	private void sendMsgToKafka(String topic, Object message) {
		Map<String, Object> data = new HashMap<>();
		data.put("topic", topic);
		data.put("msg", message);
		restTemplate.postForObject(toKafkaUrl, data, Map.class);
	}

	/**
	 * 判断是否真正返回数据.
	 *
	 * @param data
	 * @return
	 */
	private boolean isEmptyData(Map<String, List<Map<String, String>>> data) {
		if (MapUtils.isEmpty(data)) {
			return true;
		}
		for (Map.Entry<String, List<Map<String, String>>> detail : data.entrySet()) {
			List<Map<String, String>> detailData = detail.getValue();
			if (CollectionUtils.isNotEmpty(detailData) && MapUtils.isNotEmpty(detailData.get(0))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 获取方法调用名称.
	 * 
	 * @param signature
	 *            Represents the signature at a join point.
	 * @return 调用方法名称
	 */
	private String getClassMethod(Signature signature) {
		return signature.getDeclaringTypeName() + "." + signature.getName();
	}

	/**
	 * 是否需进行日志输出.
	 *
	 * <pre>
	 * 	如果在排除列表则进行输出。
	 * </pre>
	 *
	 * @param access
	 *            访问控制器名称
	 * @return 是否排除
	 */
	private boolean isNeedLog(String access) {
		boolean isInclude = false;
		for (String exclusion : INCLUSIONS) {
			isInclude = access.contains(exclusion);
			if (isInclude)
				break;
		}
		return isInclude;
	}

	/**
	 * 获取当前时间在96点时刻中的索引.
	 *
	 * @return
	 */
	private int currentTimeIndex() {
		Calendar now = Calendar.getInstance();
		int hour = now.get(Calendar.HOUR_OF_DAY);
		int minutes = now.get(Calendar.MINUTE);
        return hour * 4 + Math.floorDiv(minutes, 15) + 1;
	}

	@Autowired
	public AccessLogAspect(ObjectMapper objectMapper, RestTemplate restTemplate,
						   SearchRedisFeign searchRedisFeign, DataxRealTimeFeign dataxRealTimeFeign) {
		this.objectMapper = objectMapper;
		this.restTemplate = restTemplate;
		this.searchRedisFeign = searchRedisFeign;
		this.dataxRealTimeFeign = dataxRealTimeFeign;
	}
}
