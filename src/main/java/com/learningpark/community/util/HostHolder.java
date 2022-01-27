package com.learningpark.community.util;

import com.learningpark.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * 持有用户信息，用于代替session对象，在保证线程安全的同时减轻服务器压力
 */
@Component
public class HostHolder {

    private ThreadLocal<User> users=new ThreadLocal<>();

    public void setUser(User user){
        users.set(user);
    }

    public User getUser(){
        return users.get();
    }

    public void clear(){
        users.remove();
    }

}
