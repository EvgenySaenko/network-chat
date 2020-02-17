package com.geekbrains.chat.server.lesson04;

public class PrintABC {
    private final Object monitor = new Object();
    private char currentA = 'A';

    public static void main(String[] args) {

        PrintABC printABC = new PrintABC();

        new Thread(()-> printABC.printA()).start();
        new Thread(()-> printABC.printB()).start();
        new Thread(()-> printABC.printC()).start();
    }

    private void printA(){
        synchronized (monitor){
            try {
                for (int i = 0; i < 5; i++) {
                    while (currentA != 'A') {//если А печатаем и меняем на Б
                        monitor.wait();
                    }
                    System.out.print(currentA);
                    currentA = 'B';
                    monitor.notifyAll();
                }
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }

    }

    private void printB(){
        synchronized (monitor){
            try {
                for (int i = 0; i < 5; i++) {
                    while (currentA != 'B') {//если Б печатаем и меняем на С
                        monitor.wait();
                    }
                    System.out.print(currentA);
                    currentA = 'C';
                    monitor.notifyAll();
                }
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }

    }

    private void printC(){
        synchronized (monitor){
            try {
                for (int i = 0; i < 5; i++) {
                    while (currentA != 'C') {//если С печатаем и меняем на А
                        monitor.wait();
                    }
                    System.out.print(currentA+ " ");
                    currentA = 'A';
                    monitor.notifyAll();
                }
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }

    }
}