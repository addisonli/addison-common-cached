package com.addison.common.cached.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;

public class Hessian2Serialize implements ByteSerializer{
@Override
	public   byte[] serialize(Object obj) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Hessian2Output ho = new Hessian2Output(os);
		try {
			ho.writeObject(obj);
			ho.flushBuffer();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return os.toByteArray();
	}
@Override
	public  Object deserialize(byte[] data) {
		ByteArrayInputStream is = new ByteArrayInputStream(data);
		Hessian2Input hi = new Hessian2Input(is);  
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
