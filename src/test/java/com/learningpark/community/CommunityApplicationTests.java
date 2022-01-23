package com.learningpark.community;

import com.learningpark.community.dao.UserMapper;
import com.learningpark.community.entity.User;
import com.learningpark.community.util.MailSendUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


@SpringBootTest
class CommunityApplicationTests {

	@Test
	void contextLoads() {
	}

	@Autowired
	private UserMapper userMapper;

	@Autowired
	private MailSendUtil mailSendUtil;

	@Autowired
	private TemplateEngine templateEngine;

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
	public void testTextMail(){
		mailSendUtil.sendMail("zuzhibugongshi17@163.com","测试","Hello Mail.");
	}

	@Test
	public void testHtmlMail() {
		Context context = new Context();
		context.setVariable("username", "sunday");

		String content = templateEngine.process("/mail/demo", context);
		System.out.println(content);

		mailSendUtil.sendMail("zuzhibugongshi17@163.com", "HTML", content);
	}
}
