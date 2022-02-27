package com.learningpark.community.aspect;

import com.learningpark.community.util.HostHolder;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 统一日志处理
 * 程序执行的顺序是先进入过滤器，再进入拦截器，最后进入切面
 */
@Component
@Aspect
public class ServiceLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);

    @Autowired
    private HostHolder hostHolder;

    @Pointcut("execution(* com.learningpark.community.service.*.*(..))")
    public void pointcut() {

    }

    @Before("pointcut()")
    public void serviceLog(JoinPoint joinPoint) {
        // 用户[1.2.3.4],在[xxx],访问了[com.nowcoder.community.service.xxx()].
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getRemoteHost();
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String target = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();

        if (hostHolder.getUser() == null) {
            logger.info(String.format("用户[%s],在[%s],访问了[%s]方法.", ip, now, target));
        } else {
            logger.info(String.format("用户ip为[%s]的[%s],在[%s],访问了[%s]方法.", ip, hostHolder.getUser().getUsername(), now, target));
        }

    }
}
