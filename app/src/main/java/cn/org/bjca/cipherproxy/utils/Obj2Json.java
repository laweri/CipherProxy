package cn.org.bjca.cipherproxy.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by YeLiang on 2017/7/3.
 */

public class Obj2Json {

    /**
     * 此方法会将一个对象转换成json字符串
     *
     * @param object 将要被转成json字符串的对象
     * @return json字符串
     */
    public static String toJson(Object object) {
        //JSON载体
        StringBuffer jsonBuffer = new StringBuffer();
        //判断是否是集合类型
        if (object instanceof List<?>) {
            jsonBuffer.append("[");
            List<?> list = (List<?>) object;
            //循环读取集合类型
            for (int i = 0; i < list.size(); i++) {
                addObjectToJson(jsonBuffer, list.get(i));
                //jsonArray添加 逗号分隔
                if (i < list.size() - 1) {
                    jsonBuffer.append(",");
                }
            }
            jsonBuffer.append("]");
        } else {
            addObjectToJson(jsonBuffer, object);
        }
        return jsonBuffer.toString();
    }

    /**
     * 解析单独的JSONobject类型
     * 递归
     *
     * @param jsonBuffer
     * @param object
     */
    private static void addObjectToJson(StringBuffer jsonBuffer, Object object) {
        jsonBuffer.append("{"); //{

        List<Field> fields = new ArrayList<>();
        getAllFields(object.getClass(), fields);
        for (int i = 0; i < fields.size(); i++) {
            Method method = null;
            //1 获取field
            Field field = fields.get(i);
            Object fieldVlaue = null;

            String fieldName = field.getName();
            String methodName = "get" + ((char) (fieldName.charAt(0) - 32) + fieldName.substring(1));

            try {
                //2 获取到Method对象
                method = object.getClass().getMethod(methodName);
            } catch (NoSuchMethodException e1) {
                if (!fieldName.startsWith("is")) {
                    methodName = "is" + ((char) (fieldName.charAt(0) - 32) + fieldName.substring(1));
                }
                try {
                    method = object.getClass().getMethod(methodName);
                } catch (NoSuchMethodException e2) {
                    replaceChar(i, fields, jsonBuffer);
                    continue;
                }
            }
            if (method != null) {
                try {
                    //3 通过get方法获取成员变量的值
                    fieldVlaue = method.invoke(object);
                } catch (Exception e) {
                    replaceChar(i, fields, jsonBuffer);
                    continue;
                }
            }

            if (fieldVlaue != null) {
                jsonBuffer.append("\"");      //{"
                jsonBuffer.append(fieldName); //{"name
                jsonBuffer.append("\":");     //{"name":
                if (fieldVlaue instanceof Integer || fieldVlaue instanceof Double ||
                        fieldVlaue instanceof Long || fieldVlaue instanceof Boolean) {
                    jsonBuffer.append(fieldVlaue.toString());// {"name":1
                } else if (fieldVlaue instanceof String) {
                    jsonBuffer.append("\"");
                    jsonBuffer.append(fieldVlaue.toString());// {"name":"张三"
                    jsonBuffer.append("\"");
                } else if (fieldVlaue instanceof List<?>) {
                    addListToBuffer(jsonBuffer, fieldVlaue);
                } else if (fieldVlaue instanceof Map) {
                    //TODO
                } else {
                    addObjectToJson(jsonBuffer, fieldVlaue);
                }
                jsonBuffer.append(",");   // {"name":"张三",
            }

            if (i == fields.size() - 1 && jsonBuffer.charAt(jsonBuffer.length() - 1) == ',') {
                jsonBuffer.deleteCharAt(jsonBuffer.length() - 1);
            }

        }
        jsonBuffer.append("}");           //{"name":"张三",}
    }


    /**
     * 如果最后一个字符是 , 就删除此字符
     *
     * @param i
     * @param fields
     * @param jsonBuffer
     */
    public static void replaceChar(int i, List<Field> fields, StringBuffer jsonBuffer) {
        if (i == fields.size() - 1 && jsonBuffer.charAt(jsonBuffer.length() - 1) == ',') {
            //删除最后一个字符
            jsonBuffer.deleteCharAt(jsonBuffer.length() - 1);
        }
    }

    /**
     * 获取当前类中的所有的成员变量
     *
     * @param aClass 成员变量所在的类
     * @param fields aClass中所有的成员变量
     */
    private static void getAllFields(Class<?> aClass, List<Field> fields) {
        if (fields == null) {
            fields = new ArrayList<>();
        }

        //排除Object类型
        if (aClass.getSuperclass() != null) {
            //获取到当前class的所有成员变量的Field
            Field[] fieldSelf = aClass.getDeclaredFields();
            for (Field field : fieldSelf) {
                //排除当前Class的所有成员变量的Field
                if (!Modifier.isFinal(field.getModifiers())) {
                    fields.add(field);
                }
            }

            //获取到父类的所有成员变量
            getAllFields(aClass.getSuperclass(), fields);
        }

    }

    /**
     * 当Json字符串中包含集合时
     *
     * @param jsonBuffer
     * @param fieldValue
     */
    private static void addListToBuffer(StringBuffer jsonBuffer, Object fieldValue) {
        List<?> list = (List<?>) fieldValue;
        jsonBuffer.append("[");
        for (int i = 0; i < list.size(); i++) {
            addObjectToJson(jsonBuffer, list.get(i));
            if (i < list.size() - 1) {
                jsonBuffer.append(",");
            }
        }
        jsonBuffer.append("]");
    }


}
