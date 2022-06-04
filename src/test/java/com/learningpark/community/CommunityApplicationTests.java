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
        mailSendUtil.sendMail("zuzhibugongshi17@163.com", "测试", "Hello Mail.");
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
        String text = "我要赌博，我要嫖娼，我要吸毒！";
        text = sensitiveFilter.filter(text);
        System.out.println(text);

        text = "fabc";
        text = sensitiveFilter.filter(text);
        System.out.println(text);

        text = "☆f☆a☆b☆c☆";
        text = sensitiveFilter.filter(text);
        System.out.println(text);

        text = "☆f☆a☆b☆c";
        text = sensitiveFilter.filter(text);
        System.out.println(text);
    }

    /**
     * elasticSearch测试方法
     */
    //判断某id的文档（数据库中的行）是否存在
    @Test
    public void testExist() {
        System.out.println(discussPostRepository.existsById(101));
    }

    //一次保存一条数据
    @Test
    public void testInsert() {
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(241));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(242));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(243));
    }

    //一次保存多条数据
    @Test
    public void testInsertList() {
        //把id为101的用户发的前100条帖子（List<DiscussPost>）存入es的discusspost索引（es的索引相当于数据库的表）
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

    //通过覆盖原内容，来修改一条数据
    @Test
    public void testUpdate() {
        DiscussPost post = discussPostMapper.selectDiscussPostById(230);
        post.setContent("我是新人,使劲灌水。");
        post.setTitle(null);//es中的title会设为null
        discussPostRepository.save(post);
    }

    //修改一条数据
    //覆盖es里的原内容 与 修改es中的内容 的区别：String类型的title被设为null，覆盖的话，会把es里的该对象的title也设为null；UpdateRequest，修改后该对象的title不变
    @Test
    void testUpdateDocument() throws IOException {
        UpdateRequest request = new UpdateRequest("discusspost", "109");
        request.timeout("1s");
        DiscussPost post = discussPostMapper.selectDiscussPostById(230);
        post.setContent("我是新人,使劲灌水.");
        post.setTitle(null);//es中的title会保存原内容不变
        request.doc(JSON.toJSONString(post), XContentType.JSON);
        UpdateResponse updateResponse = restHighLevelClient.update(request, RequestOptions.DEFAULT);
        System.out.println(updateResponse.status());
    }

    // 删除数据
    @Test
    public void testDelete() {
//        discussPostRepository.deleteById(231);//删除一条数据
        discussPostRepository.deleteAll();//删除所有数据
    }

    //不带高亮的查询
    @Test
    public void noHighlightQuery() throws IOException {
        SearchRequest searchRequest = new SearchRequest("discusspost");

        //构建搜索条件
        SearchSourceBuilder builder = new SearchSourceBuilder()
                //在discusspost索引的title和content字段中都查询“互联网寒冬”
                .query(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                // matchQuery是模糊查询，会对key进行分词：searchSourceBuilder.query(QueryBuilders.matchQuery(key,value));
                // termQuery是精准查询：searchSourceBuilder.query(QueryBuilders.termQuery(key,value));
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                //一个可选项，用于控制允许搜索的时间：searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
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

    //带高亮的查询
    @Test
    public void highlightQuery() throws Exception {
        SearchRequest searchRequest = new SearchRequest("discusspost");//discusspost是索引名，就是表名
        Map<String, Object> res = new HashMap<>();

        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.field("content");
        highlightBuilder.requireFieldMatch(false);
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");

        //构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .from(0)// 指定从哪条开始查询
                .size(10)// 需要查出的总记录条数
                .highlighter(highlightBuilder);//高亮
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);//进行同步请求
        List<DiscussPost> list = new ArrayList<>();
        long total = searchResponse.getHits().getTotalHits().value;//分页查询
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            DiscussPost discussPost = JSONObject.parseObject(hit.getSourceAsString(), DiscussPost.class);

            // 处理高亮显示的结果
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
