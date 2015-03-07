package com.addison.common.cached.redis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.impl.GenericObjectPool.Config;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.util.SafeEncoder;

import com.addison.common.cached.serializer.ByteSerializer;
import com.addison.common.cached.serializer.Hessian2Serialize;
import com.addison.common.cached.serializer.HessianSerialize;
import com.addison.common.cached.serializer.JavaSerialize;

public class RedisClient {
	private static Log log =LogFactory.getLog(RedisClient.class);
	private List<AddisonJedisPool> wPoolList = new ArrayList<AddisonJedisPool>();
	private List<AddisonJedisPool> rPoolList = new ArrayList<AddisonJedisPool>();
	private ByteSerializer serializer;
	private int rPoolSize=0;
	private int wPoolSize=0;
	 
 public RedisClient(final Config poolConfig, final String wAddresses,final String rAddresses,String serializer) {
		 
	      if(wAddresses==null||rAddresses==null){
	    	  throw new RuntimeException("elong-redis缺少初始化参数");  
	       }
	 
		 String[] wAddressArray =wAddresses.split(",");
		 String[] rAddressArray =rAddresses.split(",");
		 for(String address:wAddressArray){
			String[] singleArray= address.split(":");
			if(singleArray.length==2){
				String ip=singleArray[0];
				String port=singleArray[1];
				JedisPool pool = new JedisPool(poolConfig, ip,
						Integer.valueOf(port));
				AddisonJedisPool elongJedisPool = new AddisonJedisPool();
				elongJedisPool.setJedisPoll(pool);
				elongJedisPool.setQuality(1);
				elongJedisPool.setIp(ip);
				elongJedisPool.setPort(Integer.valueOf(port));
				wPoolList.add(elongJedisPool);
			}
			 
		 }
		 
		 for(String address:rAddressArray){
				String[] singleArray= address.split(":");
				if(singleArray.length==2){
					String ip=singleArray[0];
					String port=singleArray[1];
					JedisPool pool = new JedisPool(poolConfig, ip,
							Integer.valueOf(port));
					AddisonJedisPool elongJedisPool = new AddisonJedisPool();
					elongJedisPool.setJedisPoll(pool);
					elongJedisPool.setQuality(1);
					elongJedisPool.setIp(ip);
					elongJedisPool.setPort(Integer.valueOf(port));
					rPoolList.add(elongJedisPool);
				}
			 }
		 
	rPoolSize=rPoolList.size();	 
	wPoolSize=wPoolList.size();	 
	
	if(serializer==null){
		this.serializer= new HessianSerialize();	
	}
	else if(serializer.equals("hessian")){
		this.serializer= new HessianSerialize();
	}else if(serializer.equals("hessian2")){
		this.serializer= new Hessian2Serialize();
	}else if(serializer.equals("java")){
		this.serializer= new JavaSerialize();
	}else{
		throw new RuntimeException("elong-redis不支持的序列化协议");
	}
		log.info("elong-redis初始化"+wPoolSize+"个写库，"+rPoolSize+"个读库，序列化协议为："+serializer);	 
		 
	}

public AddisonJedisPool getElongJedisPool(String key){
	AddisonJedisPool jedisPool=null;
	int index=0;
	if(rPoolSize==1){
		index=0;
	}else{
	int random =(int)Math.round(Math.random()*100);
	int h =hash(random);
	 index =indexFor(h, rPoolSize);
	}
	jedisPool=rPoolList.get(index);
	log.info("获取redis读库:"+jedisPool.getIp()+":"+jedisPool.getPort());
	return jedisPool;
}

	public String set(String key,String value){
		String str= null;
		for(AddisonJedisPool pool:wPoolList){
			JedisPool jedisPool =pool.getJedisPoll();
			Jedis jedis=jedisPool.getResource();
			try {
				str =jedis.set(key, value);
			} catch (Exception e) {
				pool.setQuality(-1);
				log.error("redis:"+pool.getIp()+":"+pool.getPort()+"发生错误", e);
				e.printStackTrace();
			}finally{
			jedisPool.returnResource(jedis);
			}
			pool.setQuality(pool.getQuality()+1);
		}
		
		return str;
	}
	
	public String setex(String key,String value,int seconds){
		String str= null;
		for(AddisonJedisPool pool:wPoolList){
			JedisPool jedisPool =pool.getJedisPoll();
			Jedis jedis=jedisPool.getResource();
			try {
				str =jedis.setex(key,seconds,value);
			} catch (Exception e) {
				pool.setQuality(-1);
				log.error("redis:"+pool.getIp()+":"+pool.getPort()+"发生错误", e);
				e.printStackTrace();
			}finally{
			jedisPool.returnResource(jedis);
			}
			pool.setQuality(pool.getQuality()+1);
		}
		
		return str;
	}
	public String setObj(String key,Object value){
		String str=null;
		byte[] data =serializer.serialize(value);
		for(AddisonJedisPool pool:wPoolList){
			JedisPool jedisPool =pool.getJedisPoll();
			Jedis jedis=jedisPool.getResource();
			try {
				str =jedis.set(SafeEncoder.encode(key), data);
			} catch (Exception e) {
				pool.setQuality(-1);
				log.error("redis:"+pool.getIp()+":"+pool.getPort()+"发生错误", e);
				e.printStackTrace();
			}finally{
			jedisPool.returnResource(jedis);
			}
			pool.setQuality(pool.getQuality()+1);
		}
		return str;
	}
	
	public String setexObj(String key,Object value,int seconds){
		String str=null;
		byte[] data =serializer.serialize(value);
		for(AddisonJedisPool pool:wPoolList){
			JedisPool jedisPool =pool.getJedisPoll();
			Jedis jedis=jedisPool.getResource();
			try {
				str =jedis.setex(SafeEncoder.encode(key),seconds, data);
			} catch (Exception e) {
				pool.setQuality(-1);
				log.error("redis:"+pool.getIp()+":"+pool.getPort()+"发生错误", e);
				e.printStackTrace();
			}finally{
			jedisPool.returnResource(jedis);
			}
			pool.setQuality(pool.getQuality()+1);
		}
		return str;
	}
	
	public String  get(String key){
		AddisonJedisPool pool= getElongJedisPool(key);
		JedisPool jedisPool=pool.getJedisPoll();
		String value=null;
		Jedis jedis=jedisPool.getResource();
		try {
			value = jedis.get(key);
		} catch (Exception e) {
			pool.setQuality(pool.getQuality()-1);
			e.printStackTrace();
		}finally{
			jedisPool.returnResource(jedis);
		}
		return value;
	}
	public Object  getObj(String key){
		AddisonJedisPool pool= getElongJedisPool(key);
		JedisPool jedisPool=pool.getJedisPoll();
		Jedis jedis=	jedisPool.getResource();
		byte[] value=null;
		try {
			value = jedis.get(SafeEncoder.encode(key));
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			jedisPool.returnResource(jedis);	
		}
		if(value==null){
			return null;
		}
		Object o=serializer.deserialize(value);
		return o;
	}
	
	public void  del(String... keys){
		for(AddisonJedisPool pool:wPoolList){
			JedisPool jedisPool =pool.getJedisPoll();
			Jedis jedis=jedisPool.getResource();
			try {
				jedis.del(keys);
			} catch (Exception e) {
				pool.setQuality(pool.getQuality()-1);
				log.error("redis:"+pool.getIp()+":"+pool.getPort()+"发生错误", e);
				e.printStackTrace();
			}finally{
			jedisPool.returnResource(jedis);
			}
			pool.setQuality(pool.getQuality()+1);	
		}
	}
	public void  sadd(String key,String...members ){
		
		for(AddisonJedisPool pool:wPoolList){
			JedisPool jedisPool =pool.getJedisPoll();
			Jedis jedis=jedisPool.getResource();
			try {
				jedis.sadd(key, members);
			} catch (Exception e) {
				pool.setQuality(-1);
				log.error("redis:"+pool.getIp()+":"+pool.getPort()+"发生错误", e);
				e.printStackTrace();
			}finally{
			jedisPool.returnResource(jedis);
			}
			pool.setQuality(pool.getQuality()+1);	
		}
		
	}
	/**
	 * 
	 * 删除set元素
	 * @param   name
	 * @param  @return    设定文件
	 * @return String    DOM对象
	 * @Exception 异常对象
	 */
	public void  srem(String key,String...members ){
		
		for(AddisonJedisPool pool:wPoolList){
			JedisPool jedisPool =pool.getJedisPoll();
			Jedis jedis=jedisPool.getResource();
			try {
				jedis.srem(key, members);
			} catch (Exception e) {
				pool.setQuality(-1);
				log.error("redis:"+pool.getIp()+":"+pool.getPort()+"发生错误", e);
				e.printStackTrace();
			}finally{
			jedisPool.returnResource(jedis);
			}
			pool.setQuality(pool.getQuality()+1);	
		}
		
		
	}
	
	/**
	 * 
	 * sdiff(返回所有给定 key 与第一个 key 的差集)
	 * @param   name
	 * @param  @return    设定文件
	 * @return String    DOM对象
	 * @since   
	 */
	public Set<String>  sdiff(String... keys ){
		AddisonJedisPool pool= getElongJedisPool(null);
		JedisPool jedisPool=pool.getJedisPoll();
		Jedis jedis=	jedisPool.getResource();
		Set<String> set=null;
		try {
			set = jedis.sdiff(keys);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			jedisPool.returnResource(jedis);
		}
		return set;
	}
	/**
	 * 
	 * sinter(返回所有给定 key 的交集)
	 * 
	 */
	public Set<String>  sinter(String... keys ){
		AddisonJedisPool pool= getElongJedisPool(null);
		JedisPool jedisPool=pool.getJedisPoll();
		Jedis jedis=	jedisPool.getResource();
		Set<String> set=null;
		try {
			set = jedis.sinter(keys);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			jedisPool.returnResource(jedis);	
		}
		return set;
	}
	
	/**
	 * 
	 * sunion(返回所有给定 key 的并集)
	 * 
	 */
	public Set<String>  sunion(String... keys ){
		AddisonJedisPool pool= getElongJedisPool(null);
		JedisPool jedisPool=pool.getJedisPoll();
		Jedis jedis=	jedisPool.getResource();
		Set<String> set=null;
		try {
			set = jedis.sunion(keys);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
		jedisPool.returnResource(jedis);
		}
		return set;
	}
	
	public void  sunionstore(String dstkey,String... keys ){
		AddisonJedisPool pool= getElongJedisPool(null);
		JedisPool jedisPool=pool.getJedisPoll();
		Jedis jedis=	jedisPool.getResource();
		try {
			 jedis.sunionstore(dstkey, keys);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			jedisPool.returnResource(jedis);	
		}
		
	}
	
	
	public Set<String>  keys(String pattern ){
		AddisonJedisPool pool= getElongJedisPool(null);
		JedisPool jedisPool=pool.getJedisPoll();
		Jedis jedis=	jedisPool.getResource();
		Set<String> result=null;
		try {
			result = jedis.keys(pattern);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			jedisPool.returnResource(jedis);	
		}
		return result;
	}
	
	public boolean  exists(String key ){
		AddisonJedisPool pool= getElongJedisPool(null);
		JedisPool jedisPool=pool.getJedisPoll();
		Jedis jedis=	jedisPool.getResource();
		boolean result=false;
		try {
			result = jedis.exists(key);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
		    jedisPool.returnResource(jedis);
		}
		return result;
	}
	public void  del(String key ){
		for(AddisonJedisPool pool:wPoolList){
			JedisPool jedisPool =pool.getJedisPoll();
			Jedis jedis=jedisPool.getResource();
			try {
				jedis.del(key);
			} catch (Exception e) {
				pool.setQuality(-1);
				log.error("redis:"+pool.getIp()+":"+pool.getPort()+"发生错误", e);
				e.printStackTrace();
			}finally{
			jedisPool.returnResource(jedis);
			}
			pool.setQuality(pool.getQuality()+1);	
		}
		
		
	}
	public void  expire(String key,int seconds){
		for(AddisonJedisPool pool:wPoolList){
			JedisPool jedisPool =pool.getJedisPoll();
			Jedis jedis=jedisPool.getResource();
			try {
				jedis.expire(key, seconds);
			} catch (Exception e) {
				pool.setQuality(-1);
				log.error("redis:"+pool.getIp()+":"+pool.getPort()+"发生错误", e);
				e.printStackTrace();
			}finally{
			jedisPool.returnResource(jedis);
			}
			pool.setQuality(pool.getQuality()+1);	
		}
		
		
	}
	
	
	public void  rename(String oldkey,String newkey){
		for(AddisonJedisPool pool:wPoolList){
			JedisPool jedisPool =pool.getJedisPoll();
			Jedis jedis=jedisPool.getResource();
			try {
				jedis.rename(oldkey, newkey);
			} catch (Exception e) {
				pool.setQuality(-1);
				log.error("redis:"+pool.getIp()+":"+pool.getPort()+"发生错误", e);
				e.printStackTrace();
			}finally{
			jedisPool.returnResource(jedis);
			}
			pool.setQuality(pool.getQuality()+1);	
		}
		
	}
	
	public Long incr(String key){
		Long result =0l;
		for(AddisonJedisPool pool:wPoolList){
			JedisPool jedisPool =pool.getJedisPoll();
			Jedis jedis=jedisPool.getResource();
			try {
				 result =jedis.incr(key);
			} catch (Exception e) {
				pool.setQuality(-1);
				log.error("redis:"+pool.getIp()+":"+pool.getPort()+"发生错误", e);
				e.printStackTrace();
			}finally{
			jedisPool.returnResource(jedis);
			}
			pool.setQuality(pool.getQuality()+1);	
		}
		
		return result;
	}
	public Long incrBy(String key,Integer value){
		
		Long result =0l;
		for(AddisonJedisPool pool:wPoolList){
			JedisPool jedisPool =pool.getJedisPoll();
			Jedis jedis=jedisPool.getResource();
			try {
				 result =jedis.incr(key);
			} catch (Exception e) {
				pool.setQuality(-1);
				log.error("redis:"+pool.getIp()+":"+pool.getPort()+"发生错误", e);
				e.printStackTrace();
			}finally{
			jedisPool.returnResource(jedis);
			}
			pool.setQuality(pool.getQuality()+1);	
		}
		return result;
	}
	public Set<String> smembers(String key){
		AddisonJedisPool pool= getElongJedisPool(null);
		JedisPool jedisPool=pool.getJedisPoll();
		Jedis jedis=	jedisPool.getResource();
		Set<String> result=null;
		try {
			result = jedis.smembers(key);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			jedisPool.returnResource(jedis);
		}
		
		return result;
	}
	public boolean sismember(String key,String value){
		AddisonJedisPool pool= getElongJedisPool(null);
		JedisPool jedisPool=pool.getJedisPoll();
		Jedis jedis=	jedisPool.getResource();
		boolean result=false;
		try {
			result = jedis.sismember(key,value);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			jedisPool.returnResource(jedis);
		}
		
		return result;
	}
	
	public void sremAll(String key){
		
		for(AddisonJedisPool pool:wPoolList){
			JedisPool jedisPool =pool.getJedisPoll();
			Jedis jedis=jedisPool.getResource();
			try {
				Set<String> values =jedis.smembers(key);
				for(String value:values){
					jedis.srem(key, value);
				}
			} catch (Exception e) {
				pool.setQuality(-1);
				log.error("redis:"+pool.getIp()+":"+pool.getPort()+"发生错误", e);
				e.printStackTrace();
			}finally{
			jedisPool.returnResource(jedis);
			}
			pool.setQuality(pool.getQuality()+1);	
		}
		
	}
	
	
	public String lpop(String key){
		String str= null;
		for(AddisonJedisPool pool:wPoolList){
			JedisPool jedisPool =pool.getJedisPoll();
			Jedis jedis=jedisPool.getResource();
			try {
				str=	jedis.lpop(key);
			} catch (Exception e) {
				pool.setQuality(-1);
				log.error("redis:"+pool.getIp()+":"+pool.getPort()+"发生错误", e);
				e.printStackTrace();
			}finally{
			jedisPool.returnResource(jedis);
			}
			pool.setQuality(pool.getQuality()+1);
		}
		
		return str;
	}
	
	public String rpop(String key){
		String str= null;
		for(AddisonJedisPool pool:wPoolList){
			JedisPool jedisPool =pool.getJedisPoll();
			Jedis jedis=jedisPool.getResource();
			try {
				str=	jedis.rpop(key);
			} catch (Exception e) {
				pool.setQuality(-1);
				log.error("redis:"+pool.getIp()+":"+pool.getPort()+"发生错误", e);
				e.printStackTrace();
			}finally{
			jedisPool.returnResource(jedis);
			}
			pool.setQuality(pool.getQuality()+1);
		}
		
		return str;
	}
	
	public List<String> lrange(String key,int start,int end){
		List<String> strList= null;
		for(AddisonJedisPool pool:wPoolList){
			JedisPool jedisPool =pool.getJedisPoll();
			Jedis jedis=jedisPool.getResource();
			try {
				strList=	jedis.lrange(key, start, end);
			} catch (Exception e) {
				pool.setQuality(-1);
				log.error("redis:"+pool.getIp()+":"+pool.getPort()+"发生错误", e);
				e.printStackTrace();
			}finally{
			jedisPool.returnResource(jedis);
			}
			pool.setQuality(pool.getQuality()+1);
		}
		
		return strList;
	}
	
	
	public long rpush(String key,String value){
		long result= 0;
		for(AddisonJedisPool pool:wPoolList){
			JedisPool jedisPool =pool.getJedisPoll();
			Jedis jedis=jedisPool.getResource();
			try {
				result=	jedis.rpush(key,value);
			} catch (Exception e) {
				pool.setQuality(-1);
				log.error("redis:"+pool.getIp()+":"+pool.getPort()+"发生错误", e);
				e.printStackTrace();
			}finally{
			jedisPool.returnResource(jedis);
			}
			pool.setQuality(pool.getQuality()+1);
		}
		
		return result;
	}
	
	public long lpush(String key,String value){
		long result= 0;
		for(AddisonJedisPool pool:wPoolList){
			JedisPool jedisPool =pool.getJedisPoll();
			Jedis jedis=jedisPool.getResource();
			try {
				result=	jedis.lpush(key,value);
			} catch (Exception e) {
				pool.setQuality(-1);
				log.error("redis:"+pool.getIp()+":"+pool.getPort()+"发生错误", e);
				e.printStackTrace();
			}finally{
			jedisPool.returnResource(jedis);
			}
			pool.setQuality(pool.getQuality()+1);
		}
		
		return result;
	}
	
	
	public long  llen(String key){
		AddisonJedisPool pool= getElongJedisPool(key);
		JedisPool jedisPool=pool.getJedisPoll();
		long value=0;
		Jedis jedis=jedisPool.getResource();
		try {
			value = jedis.llen(key);
		} catch (Exception e) {
			pool.setQuality(pool.getQuality()-1);
			e.printStackTrace();
		}finally{
			jedisPool.returnResource(jedis);
		}
		return value;
	}
	
	final static int hash(Object k) {
        int h = 0;
        h ^= k.hashCode();
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }
	
	 static int indexFor(int h, int length) {
	        return h & (length-1);
	    }
	 
	 public static void main(String[] args){
		
		   for(int n=0;n<50;n++){
			   int random =(int)Math.round(Math.random()*100);
				int h =hash(random);
				int i =indexFor(h, 2);
			   System.out.println("key"+random+":"+i);
		   }
		  
		 
	 }
	

}
