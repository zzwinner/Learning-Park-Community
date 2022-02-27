package com.learningpark.community.controller;

import com.learningpark.community.annotation.LoginRequired;
import com.learningpark.community.entity.Event;
import com.learningpark.community.entity.User;
import com.learningpark.community.event.EventProducer;
import com.learningpark.community.service.LikeService;
import com.learningpark.community.util.CommunityConstant;
import com.learningpark.community.util.CommunityUtil;
import com.learningpark.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController implements CommunityConstant {

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(path = "/like", method = RequestMethod.POST)
    @ResponseBody
    @LoginRequired
    public String like(int entityType, int entityId, int entityUserId, int postId) {
        User user = hostHolder.getUser();

        //点赞
        likeService.like(user.getId(), entityType, entityId, entityUserId);

        //点赞数量
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);

        //点赞状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);
        //返回结果
        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        //触发点赞事件
        if (likeStatus == 1) {
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId", postId);
            if (event.getUserId() != event.getEntityUserId()) {
                eventProducer.fireEvent(event);
            }
        }

        return CommunityUtil.getJSONString(0, null, map);
    }

}
