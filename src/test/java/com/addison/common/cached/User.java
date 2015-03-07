
/**   
 * @Title: User.java 
 * @Package: org.zlex.jedis.dto 
 * @Description: TODO
 * @author Addison.Li 
 * @date 2014年5月27日 下午2:15:52 
 * @version 1.0
 */


package com.addison.common.cached;

import java.io.Serializable;

/** 
 * @ClassName: User 
 * @Description 
 * @author Addison.Li
 * @date 2014年5月27日 下午2:15:52 
 * @version V1.0
 */

public class User implements Serializable {
	
	/** @Fields serialVersionUID: */
	  	
	private static final long serialVersionUID = -4130852794015226451L;
	private String name;
	private String address;
	private int age;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	

}
