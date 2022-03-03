package com.learningpark.community.controller;

import com.learningpark.community.entity.DiscussPost;
import com.learningpark.community.entity.Page;
import com.learningpark.community.service.ElasticsearchService;
import com.learningpark.community.service.LikeService;
import com.learningpark.community.service.UserService;
import com.learningpark.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
public class SearchController implements CommunityConstant {

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @RequestMapping(path = "/search", method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model) throws IOException {
        //搜索帖子
        Map<String, Object> searchMap =
                elasticsearchService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());
        //聚合数据
        List<Map<String, Object>> searchVoList = new ArrayList<>();
        List<DiscussPost> list = (List<DiscussPost>) searchMap.get("list");
        if (list != null) {
            for (DiscussPost post : list) {
                Map<String, Object> map = new HashMap<>();
                // 帖子
                map.put("post", post);
                // 作者
                map.put("user", userService.findUserById(post.getUserId()));
                // 点赞数量
                map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));

                searchVoList.add(map);
            }
        }
        model.addAttribute("discussPosts", searchVoList);
        model.addAttribute("keyword", keyword);

        // 分页信息
        page.setPath("/search?keyword=" + keyword);
        long total = (Long) searchMap.get("total");
        page.setRows(list == null ? 0 : (int) total);

        return "/site/search";
    }

}
