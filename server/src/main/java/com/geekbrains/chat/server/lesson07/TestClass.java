package com.geekbrains.chat.server.lesson07;

public class TestClass {

    @Fields
    String name;

    @Fields
    String color;

    @Fields
    int position;

    public TestClass(String name, String color, int position) {
        this.name = name;
        this.color = color;
        this.position = position;
    }
    @BeforeSuite(priority = 1)
    public  void start() {
        System.out.println(color + " " + name + " начал гонку на позиции № " + position);
    }
//    @BeforeSuite(priority = 1)
//    public  void start2() {
//        System.out.println(color + " " + name + " начал гонку на позиции № " + position);
//    }

    @Test(priority = 3)
    public void round1(){
        System.out.println(color + " " + name + " на первом этапе занимает позицию № " + position);
    }

    @Test(priority = 5)
    public void round2(){
        this.position = position - 3;
        System.out.println(color + " " + name + " на втором этапе занимает позицию № " + position);
    }

    @Test(priority = 7)
    public void round3(){
        this.position = position - 3;
        System.out.println(color + " " + name + " на третьем этапе занимает позицию № " + position);
    }

    @AfterSuite(priority = 10)
    public  void finish() {
        System.out.println(color + " " + name + " завершает гонку на позиции № " + position);
    }
//    @AfterSuite(priority = 10)
//    public  void finish2() {
//        System.out.println(color + " " + name + " завершает гонку на позиции № " + position);
//    }
}
