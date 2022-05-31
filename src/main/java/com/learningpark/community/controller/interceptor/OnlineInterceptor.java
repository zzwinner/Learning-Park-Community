package com.learningpark.community.controller.interceptor;

import com.learningpark.community.entity.User;
import com.learningpark.community.service.OnlineService;
import com.learningpark.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class OnlineInterceptor implements HandlerInterceptor {

    @Autowired
    private OnlineService onlineService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 统计UV
        String ip = request.getRemoteHost();
        onlineService.recordUV(ip);

        // 统计DAU
        User user = hostHolder.getUser();
        if (user != null) {
            onlineService.recordDAU(user.getId());
        }

        return true;
    }

}
