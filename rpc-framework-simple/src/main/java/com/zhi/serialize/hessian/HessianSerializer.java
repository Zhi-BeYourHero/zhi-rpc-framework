package com.zhi.serialize.hessian;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import com.zhi.exception.SerializeException;
import com.zhi.serialize.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @Description Hessian序列化框架
 * @Author WenZhiLuo
 * @Date 2021-01-24 17:02
 */
public class HessianSerializer implements Serializer {

    @Override
    public <T> byte[] serialize(T obj) {
        if (obj == null) {
            throw new NullPointerException();
        }
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            HessianOutput hessianOutput = new HessianOutput(byteArrayOutputStream);
            hessianOutput.writeObject(obj);
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            throw new SerializeException("序列化失败");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        if (data == null) {
            throw new NullPointerException();
        }
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data)) {
            HessianInput hessianInput = new HessianInput(byteArrayInputStream);
            return (T) hessianInput.readObject();
        } catch (Exception exception) {
            throw new SerializeException("反序列化失败");
        }
    }
}