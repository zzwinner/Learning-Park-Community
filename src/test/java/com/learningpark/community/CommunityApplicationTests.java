package com.learningpark.community;

import com.learningpark.community.dao.UserMapper;
import com.learningpark.community.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CommunityApplicationTests {

	@Test
	void contextLoads() {
	}

	@Autowired
	private UserMapper userMapper;

	@Test
	public void testSelectUser() {
		User user = userMapper.selectById(101);
		System.out.println(user);

		user = userMapper.selectByName("liubei");
		System.out.println(user);

		user = userMapper.selectByEmail("nowcoder101@sina.com");
		System.out.println(user);
	}

}
