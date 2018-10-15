package com.jiufu.outbound.authorization.interceptor;

import com.jiufu.outbound.authorization.config.Constants;
import com.jiufu.outbound.authorization.manager.TokenManager;
import com.jiufu.outbound.authorization.model.TokenModel;
import com.jiufu.outbound.constant.LogConstant;
import com.jiufu.outbound.enums.PermissionEnum;
import com.jiufu.outbound.modules.system.entity.po.Permission;
import com.jiufu.outbound.modules.system.entity.vo.PermissionVo;
import com.jiufu.outbound.modules.system.mapper.PermissionMapper;
import com.jiufu.outbound.modules.system.service.IUserSerivce;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * 按钮级拦截器，判断此次请求是否有权限
 * @author hbj
 */
@Component
public class ButtonInterceptor  extends HandlerInterceptorAdapter{

    private Logger logger = LogConstant.commonLog;
    @Autowired
    IUserSerivce userService;

    @Autowired
    private TokenManager manager;

    private Set<String> authWhiteSet = new HashSet<>();

    private volatile boolean  inited = false;

    private ReentrantLock  lock = new ReentrantLock();

    private List<Permission> needFiltedPermisson = new ArrayList<>();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if(!inited){
            init();
        }

        // 获取请求uri
        String requestURI = request.getRequestURI();
        // 将uri中的参数变量替换
        String new_URI = replaceParamFromURI(requestURI);
        String method = request.getMethod();
        logger.info("requestURI: " + new_URI + "\tMethodType: " + method);

        if(authWhiteSet.contains(new_URI)){
            return true;
        }

        // 获取需要拦截的权限名单 只针对uri不为空的的权限进行拦截
        if(needFiltedPermisson == null || needFiltedPermisson.size() == 0){
            loadAllFilterPermission();
        }

        long count = needFiltedPermisson.stream()
                                                .filter(permission -> Objects.equals(permission.getAccessUrl(), new_URI) && Objects.equals(permission.getMethod(), method))
                                                    .count();
        if(count > 0){  // 请求需要拦截

            // 从header中得到token
            String authorization = request.getHeader(Constants.AUTHORIZATION);

            if(StringUtils.isEmpty(authorization)){
                Cookie[] cookies = request.getCookies();
                for(Cookie cookie : cookies){
                    if(Objects.equals(cookie.getName(), "token"))
                    {
                        authorization = cookie.getValue();
                    }
                }
            }
            TokenModel model = manager.getToken(authorization);

            // 获取用户拥有的权限
            List<Permission> permissions = userService.getUserAccessUrls(model.getUserId());

            // 菜单级权限
            Long menu_accessCount = permissions.stream()
                    .filter(permission -> Objects.equals(permission.getAccessUrl(), new_URI) && Objects.equals(permission.getMethod(), method)
                            && Objects.equals(permission.getAuthType(), PermissionEnum.AUTH_TYPE_MENU.getName()))
                    .count();

            if(menu_accessCount > 0 ){
                Long  virtualCount = permissions.stream()
                                                    .filter(permission -> Objects.equals(permission.getAuthType(), PermissionEnum.AUTH_TYPE_VIRTUAL.getName()))
                                                          .filter(permission -> Objects.equals(permission.getAccessUrl(), new_URI) && Objects.equals(permission.getMethod(), method))
                                                            .count();
                if(virtualCount > 0 ){ // 用户拥有该权限
                     return  true;
                }
            }

            Long button_accessCount = permissions.stream()
                    .filter(permission -> Objects.equals(permission.getAccessUrl(), new_URI) && Objects.equals(permission.getMethod(), method)
                            && Objects.equals(permission.getAuthType(), PermissionEnum.AUTH_TYPE_BUTTON.getName()))
                    .count();

            // 按钮级权限
            if(button_accessCount > 0){
                return true;
            }
            // 用户没有权限
            logger.error("无权访问的资源：" + requestURI + "requestMethod: " + method);
            response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
            //response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/html; charset=utf-8");
            response.sendError(HttpServletResponse.SC_FORBIDDEN,"无权访问该资源，请联系系统管理员！");
            return false;
        }
        return true;


    }

    private String replaceParamFromURI(String oldUri) {
        if (StringUtils.isEmpty(oldUri)){
            return null;
        }
        String newUri = oldUri;
        String[] segments = oldUri.split("/");
        for(String segment : segments){
            if(segment.length() == 32){
                newUri = oldUri.replace(segment,"?");
            }
        }
        return newUri;
    }

    // 初始化白名单
    private void init(){
        try {
            lock.lock();
            authWhiteSet.add("/api/user/current");
            authWhiteSet.add("/api/login");
            authWhiteSet.add("/api/loginout");
            authWhiteSet.add("/api/project");
            authWhiteSet.add("/error");
            authWhiteSet.add("/api/user/currentAuthorization");
            inited = true;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }

    public void loadAllFilterPermission(){
        needFiltedPermisson = userService.getAllUrlPermisson();
    }

}
