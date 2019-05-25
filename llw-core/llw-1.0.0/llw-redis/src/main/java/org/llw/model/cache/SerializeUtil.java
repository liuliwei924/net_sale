package org.llw.model.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class SerializeUtil {
	
	public static byte[] serialize(Object object) {
		ObjectOutputStream oos = null;
		ByteArrayOutputStream baos = null;
		try {
			// 序列化
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(object);
			byte[] bytes = baos.toByteArray();
			return bytes;
		} catch (Exception e) {
			
			
		}finally{
			try {
				oos.close();
				baos.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return null;
	}

	public static Object unserialize(byte[] bytes) {
		ByteArrayInputStream bais = null;
		try {
			// 反序列化
			bais = new ByteArrayInputStream(bytes);
			ObjectInputStream ois = new ObjectInputStream(bais);
			return ois.readObject();
		} catch (Exception e) {
			
		}finally{
			try {
				bais.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return null;
	}
	
	
	 /**
     * 将List<byte[]>反序列化为list<T> 集合
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> unserializeList(List<byte[]> bytes)
    {
        if (bytes == null)
        {
            return null;
        }
        
        List<T> list = new ArrayList<T>();
        
        for (byte[] ele : bytes)
        {
            T t = (T)SerializeUtil.unserialize(ele);
            
            list.add(t);
        }
        
        return list;
    }
}
