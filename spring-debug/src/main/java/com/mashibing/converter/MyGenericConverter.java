package com.mashibing.converter;

import org.springframework.core.ResolvableType;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.core.convert.support.DefaultConversionService;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MyGenericConverter implements GenericConverter {
    /**
     * 告诉转换器总部，我这个转换器支持什么样的转换，可以支持多个转换
     * 此处支持 string -> list 和 string -> map
     */
    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        Set<ConvertiblePair> convertiblePairs = Collections.newSetFromMap(new ConcurrentHashMap<>());
        ConvertiblePair stringToArray = new ConvertiblePair(String.class, List.class);
        ConvertiblePair stringToMap = new ConvertiblePair(String.class, Map.class);
        convertiblePairs.add(stringToArray);
        convertiblePairs.add(stringToMap);
        return convertiblePairs;
    }

    /**
     * @param source     被转换的东西
     * @param sourceType 被转换的东西的上下文，可以用于设置条件，具体咋玩看typeDescriptor，比如可以用来判断转换源是否拥有某些注解
     *                   @see TypeDescriptor
     * @param targetType 转换目标类型的上下文
     * @return
     * 案例场景：如果转换源被@Resource标识，并且目标类型为List，则转换list
     *                                         目标类型为map，则转换为map
     */
    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        Resource annotation = sourceType.getAnnotation(Resource.class);
        if (annotation!= null){
            if (targetType.getType() == List.class){
                return Arrays.asList(source.toString().split(":"));
            }
            if (targetType.getType() == Map.class){
                Map<String, String> map = new HashMap<>();
                map.put("a",source.toString().split(":")[0]);
                map.put("b",source.toString().split(":")[1]);
                return map;
            }
        }
        return null;
    }

    public static void main(String[] args) throws NoSuchFieldException {
        DefaultConversionService defaultConversionService = new DefaultConversionService();
        defaultConversionService.addConverter(new MyGenericConverter());

        Student student = new Student();
        student.setName("1:2");


        System.out.println( defaultConversionService.convert(student.getName(),
                new TypeDescriptor(Student.class.getDeclaredField("name")),
                new TypeDescriptor(ResolvableType.forRawClass(List.class),null,null)));
    }
}
