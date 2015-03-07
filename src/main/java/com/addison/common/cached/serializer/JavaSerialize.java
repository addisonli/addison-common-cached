package com.addison.common.cached.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class JavaSerialize implements ByteSerializer{
	
	@Override
	public  byte[] serialize(Object obj){
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ObjectOutputStream osStream;
		try {
			osStream = new ObjectOutputStream(os);
			osStream.writeObject(obj);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return os.toByteArray();
	}
	@Override
	public Object deserialize(byte[] data) {
		ByteArrayInputStream is = new ByteArrayInputStream(data);
		ObjectInputStream oi;
		Object obj=null;
		try {
			oi = new ObjectInputStream(is);
			 obj =oi.readObject();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return obj;
	}


}
