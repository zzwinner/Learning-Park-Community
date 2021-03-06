package com.learningpark.community;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.learningpark.community.dao.DiscussPostMapper;
import com.learningpark.community.dao.UserMapper;
import com.learningpark.community.dao.elasticsearch.DiscussPostRepository;
import com.learningpark.community.entity.DiscussPost;
import com.learningpark.community.entity.User;
import com.learningpark.community.util.MailSendUtil;
import com.learningpark.community.util.SensitiveFilter;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


@SpringBootTest
public class CommunityApplicationTests {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailSendUtil mailSendUtil;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    @Qualifier("client")
    private RestHighLevelClient restHighLevelClient;

    @Test
    public void testSelectUser() {
        User user = userMapper.selectById(101);
        System.out.println(user);

        user = userMapper.selectByName("liubei");
        System.out.println(user);

        user = userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println(user);
    }

    @Test
    public void testTextMail() {
        mailSendUtil.sendMail("zuzhibugongshi17@163.com", "??????", "Hello Mail.");
    }

    @Test
    public void testHtmlMail() {
        Context context = new Context();
        context.setVariable("username", "sunday");

        String content = templateEngine.process("/mail/demo", context);
        System.out.println(content);

        mailSendUtil.sendMail("zuzhibugongshi17@163.com", "HTML", content);
    }

    @Test
    public void testSensitiveWords() {
        String text = "?????????????????????????????????????????????";
        text = sensitiveFilter.filter(text);
        System.out.println(text);

        text = "fabc";
        text = sensitiveFilter.filter(text);
        System.out.println(text);

        text = "???f???a???b???c???";
        text = sensitiveFilter.filter(text);
        System.out.println(text);

        text = "???f???a???b???c";
        text = sensitiveFilter.filter(text);
        System.out.println(text);
    }

    /**
     * elasticSearch????????????
     */
    //?????????id?????????????????????????????????????????????
    @Test
    public void testExist() {
        System.out.println(discussPostRepository.existsById(101));
    }

    //????????????????????????
    @Test
    public void testInsert() {
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(241));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(242));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(243));
    }

    //????????????????????????
    @Test
    public void testInsertList() {
        //???id???101??????????????????100????????????List<DiscussPost>?????????es???discusspost?????????es????????????????????????????????????
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(101, 0, 100));
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(102, 0, 100));
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(103, 0, 100));
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(111, 0, 100));
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(112, 0, 100));
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(131, 0, 100));
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(132, 0, 100));
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(133, 0, 100));
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(134, 0, 100));
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(150, 0, 100));
    }

    //?????????????????????????????????????????????
    @Test
    public void testUpdate() {
        DiscussPost post = discussPostMapper.selectDiscussPostById(230);
        post.setContent("????????????,???????????????");
        post.setTitle(null);//es??????title?????????null
        discussPostRepository.save(post);
    }

    //??????????????????
    //??????es??????????????? ??? ??????es???????????? ????????????String?????????title?????????null????????????????????????es??????????????????title?????????null???UpdateRequest????????????????????????title??????
    @Test
    void testUpdateDocument() throws IOException {
        UpdateRequest request = new UpdateRequest("discusspost", "109");
        request.timeout("1s");
        DiscussPost post = discussPostMapper.selectDiscussPostById(230);
        post.setContent("????????????,????????????.");
        post.setTitle(null);//es??????title????????????????????????
        request.doc(JSON.toJSONString(post), XContentType.JSON);
        UpdateResponse updateResponse = restHighLevelClient.update(request, RequestOptions.DEFAULT);
        System.out.println(updateResponse.status());
    }

    // ????????????
    @Test
    public void testDelete() {
//        discussPostRepository.deleteById(231);//??????????????????
        discussPostRepository.deleteAll();//??????????????????
    }

    //?????????????????????
    @Test
    public void noHighlightQuery() throws IOException {
        SearchRequest searchRequest = new SearchRequest("discusspost");

        //??????????????????
        SearchSourceBuilder builder = new SearchSourceBuilder()
                //???discusspost?????????title???content???????????????????????????????????????
                .query(QueryBuilders.multiMatchQuery("???????????????", "title", "content"))
                // matchQuery????????????????????????key???????????????searchSourceBuilder.query(QueryBuilders.matchQuery(key,value));
                // termQuery??????????????????searchSourceBuilder.query(QueryBuilders.termQuery(key,value));
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                //??????????????????????????????????????????????????????searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
                .from(0)
                .size(10);
        searchRequest.source(builder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        List<DiscussPost> list = new LinkedList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            DiscussPost discussPost = JSONObject.parseObject(hit.getSourceAsString(), DiscussPost.class);
//            System.out.println(discussPost);
            list.add(discussPost);
        }
        System.out.println(list.size());
        for (DiscussPost post : list) {
            System.out.println(post);
        }

    }

    //??????????????????
    @Test
    public void highlightQuery() throws Exception {
        SearchRequest searchRequest = new SearchRequest("discusspost");//discusspost???????????????????????????
        Map<String, Object> res = new HashMap<>();

        //??????
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.field("content");
        highlightBuilder.requireFieldMatch(false);
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");

        //??????????????????
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.multiMatchQuery("???????????????", "title", "content"))
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .from(0)// ???????????????????????????
                .size(10)// ??????????????????????????????
                .highlighter(highlightBuilder);//??????
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);//??????????????????
        List<DiscussPost> list = new ArrayList<>();
        long total = searchResponse.getHits().getTotalHits().value;//????????????
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            DiscussPost discussPost = JSONObject.parseObject(hit.getSourceAsString(), DiscussPost.class);

            // ???????????????????????????
            HighlightField titleField = hit.getHighlightFields().get("title");
            if (titleField != null) {
                discussPost.setTitle(titleField.getFragments()[0].toString());
            }
            HighlightField contentField = hit.getHighlightFields().get("content");
            if (contentField != null) {
                discussPost.setContent(contentField.getFragments()[0].toString());
            }
//            System.out.println(discussPost);
            list.add(discussPost);
        }
        res.put("list", list);
        res.put("total", total);
        if (res.get("list") != null) {
            for (DiscussPost post : list = (List<DiscussPost>) res.get("list")) {
                System.out.println(post);
            }
            System.out.println(res.get("total"));
        }
    }
}
