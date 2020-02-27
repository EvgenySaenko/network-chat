package com.geekbrains.chat.server.lesson07;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;

public class TestStarter {
    private static void start(Class testClass) throws Exception {
        TestClass fordMustang = new TestClass("Ford Mustang","Синий",7);
        Class c = fordMustang.getClass();
        //просто для тренировки
        Constructor constructor = c.getConstructor(String.class, String.class, int.class);
        Object ford = TestClass.class.getConstructor(String.class, String.class, int.class).newInstance("Ford Mustang","Красный",11);

        //достанем все методы
        Method [] methods = c.getDeclaredMethods();

        //создадим список ThreeSet -удобен объекты сохраняются в отсортированном порядке
        Map<Integer,Method> priorityListMethod = new TreeMap<>();

        for (Method m: methods){//переберем массив методов =>
            if (m.isAnnotationPresent(BeforeSuite.class)){// если методы с аннотацией  => BeforeSuite
                int priority = m.getAnnotation(BeforeSuite.class).priority();//получим их приоритет в инте
                if (priorityListMethod.containsKey(1)) throw new RuntimeException("Метод с аннотацией BeforeSuite должен быть один!");
                if (!priorityListMethod.containsKey(1)) {
                    priorityListMethod.put(priority, m);//положим в список пару(приоритет,метод)
                }
            }

            if (m.isAnnotationPresent(Test.class)){// если методы с аннотацией  => Test
                int priority = m.getAnnotation(Test.class).priority();//получим их приоритет в инте
                priorityListMethod.put(priority,m);//положим в список пару(приоритет,метод)
            }
            if (m.isAnnotationPresent(AfterSuite.class)){// если методы с аннотацией  => AfterSuite
                int priority = m.getAnnotation(AfterSuite.class).priority();//получим их приоритет в инте
                if (priorityListMethod.containsKey(10)) throw new RuntimeException("Метод с аннотацией AfterSuite должен быть один!");
                if (!priorityListMethod.containsKey(10)) {
                    priorityListMethod.put(priority, m);//положим в список пару(приоритет,метод)
                }
            }
        }
        for (Integer key : priorityListMethod.keySet()) {//переберем все ключи
            priorityListMethod.get(key).invoke(fordMustang);//и вызовем у каждого ключа метод
        }
//
    }

    public static void main(String[] args) throws Exception {
        start(TestClass.class);
    }
}
