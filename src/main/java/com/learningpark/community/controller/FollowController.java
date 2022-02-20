package com.learningpark.community.controller;

import com.learningpark.community.annotation.LoginRequired;
import com.learningpark.community.entity.User;
import com.learningpark.community.service.FollowService;
import com.learningpark.community.util.CommunityUtil;
import com.learningpark.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class FollowController {

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    @ResponseBody
    @LoginRequired
    public String follow(int entityType, int entityId) {
        User user = hostHolder.getUser();

        followService.follow(user.getId(), entityType, entityId);

        if (followService.hasFollowed(user.getId(), entityType, entityId)) {
            return CommunityUtil.getJSONString(0, "已关注！");
        } else return CommunityUtil.getJSONString(0, "已取消关注！");
    }

}
