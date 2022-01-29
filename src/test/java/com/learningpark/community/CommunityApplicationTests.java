package com.learningpark.community;

import com.learningpark.community.dao.UserMapper;
import com.learningpark.community.entity.User;
import com.learningpark.community.util.MailSendUtil;
import com.learningpark.community.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


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

}
