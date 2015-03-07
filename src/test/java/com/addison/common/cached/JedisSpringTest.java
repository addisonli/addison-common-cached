/**
 * Aug 15, 2012
 */
package com.addison.common.cached;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.addison.common.cached.redis.RedisClient;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class JedisSpringTest {

	private ApplicationContext app;
	private RedisClient redisClient;

	@Before
	public void before() throws Exception {
		app = new ClassPathXmlApplicationContext("applicationContext.xml");
		redisClient = (RedisClient) app.getBean("redisClient");
	}


	
	@Test
	public void set(){
		redisClient.set("test123", "test123");
		User user = new User();
		user.setName("libin");
		redisClient.setObj("user---", user);
	}
	
	@Test
	public void get(){
		String str =redisClient.get("test123");
		System.out.println(str);
		User user =(User)redisClient.getObj("user---");
		System.out.println(user.getName());
		
	}
	
	@Test
	public void setList(){
		List<User> list = new ArrayList<User>();
		User user = new User();
		user.setAddress("星科大厦C座");
		user.setName("李彬");
		list.add(user);
		this.redisClient.setObj("userList", list);
		
		
	}
	
	@Test
	public void getList(){
		List<User> list =(List<User>)this.redisClient.getObj("userList");
		for(User u:list){
			System.out.println(u.getAddress());
		}
		
		
	}
	
	@Test
	public void clearAll(){
		Set<String> keys =redisClient.keys("*");
		for(String key:keys){
			redisClient.del(key);
		}
		
	}
}
