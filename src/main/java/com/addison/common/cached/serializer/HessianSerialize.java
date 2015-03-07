package com.addison.common.cached.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;

public class HessianSerialize implements ByteSerializer{
	@Override
	public   byte[] serialize(Object obj) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		HessianOutput ho = new HessianOutput(os);
		try {
			ho.writeObject(obj);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return os.toByteArray();
	}
	@Override
	public  Object deserialize(byte[] data) {
		ByteArrayInputStream is = new ByteArrayInputStream(data);
		HessianInput hi = new HessianInput(is);  
		Object obj=null;
		try {
			obj = hi.readObject();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return obj;
	}
}
