



package com.addison.common.cached.redis;

import redis.clients.jedis.JedisPool;

/** 
 * @ClassName: ElongJedisPool 
 * @Description 
 * @author Addison.Li
 * @date 2014年5月27日 下午4:13:14 
 * @version V1.0
 */

public class AddisonJedisPool {
	
	private JedisPool jedisPoll;
	private int quality;
	private String ip;
	private Integer port;
	public JedisPool getJedisPoll() {
		return jedisPoll;
	}
	public void setJedisPoll(JedisPool jedisPoll) {
		this.jedisPoll = jedisPoll;
	}
	public int getQuality() {
		return quality;
	}
	public void setQuality(int quality) {
		this.quality = quality;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	
	
	
	

}
