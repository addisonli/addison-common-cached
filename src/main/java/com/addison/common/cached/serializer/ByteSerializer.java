
package com.addison.common.cached.serializer;


/**
 * @ClassName: ByteSerializer
 * @Description
 * @author Addison.Li
 * @date 2014年5月27日 上午10:43:42
 * @version V1.0
 */

public abstract interface ByteSerializer {
	public abstract byte[] serialize(Object obj) ;
	public abstract Object deserialize(byte[] dataBytes);
}
