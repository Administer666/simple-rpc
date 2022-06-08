package com.swpu.rpc.core;

import com.google.common.collect.Lists;
import com.swpu.rpc.core.entity.School;
import com.swpu.rpc.core.entity.Student;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @Author lms
 * @Date 2022/5/31 21:44
 * @Description
 */
public class SerializerTest {

    @Test
    public void testProtostuff() {
        Student zs = new Student("张三", 12);
        Student ls = new Student("李四", 13);
        Student ww = new Student("王五", 15);

        // 测试 List集合
        List<Student> list = Lists.newArrayList(zs, ls, ww);
        byte[] bytes = MyProtostuffUtils.serializer(list);
        List<Student> list1 = MyProtostuffUtils.deserialize(bytes, List.class);
        System.out.println(list1);

        // 测试对象
        School school = new School("新泰一中", list);
        byte[] bytes1 = MyProtostuffUtils.serializer(school);
        School school1 = MyProtostuffUtils.deserialize(bytes1, School.class);
        System.out.println(school1);
    }

}
