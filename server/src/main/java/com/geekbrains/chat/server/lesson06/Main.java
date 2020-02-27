package com.geekbrains.chat.server.lesson06;

import org.apache.logging.log4j.core.util.JsonUtils;

import java.util.Arrays;



public class Main {
    //Trace < Debug < Info < Warn <Error < Fatal
    private static int position;
    private static int[] arr = {1, 2, 4, 4, 2, 3, 4, 6, 7};
    private static int[] arr1 = {1, 5, 4, 4, 5, 3, 4, 1, 5};
    private static int[] arr2 = {1, 7, 5, 7, 2, 3, 5, 7, 1};
    private static int[] arr4 = {1, 1, 1, 4, 4, 1, 4, 4};
    private static int[] arr5 = {1, 1, 1, 1, 1, 1, 1, 1};
    private static int[] arr6 = {4, 4, 4, 4, 4, 4, 4, 4};
    private static int[] arr7 = {1, 4, 1, 4, 1, 4, 3, 4};
    private static int[] arr8 = {1, 4, 1, 0, 1, 4, 0, 4};

    private static int count;

    //1.Написать метод, которому в качестве аргумента передается не пустой одномерный целочисленный массив.
    //  Метод должен вернуть новый массив, который получен путем вытаскивания из исходного массива элементов,
    //  идущих после последней четверки. Входной массив должен содержать хотя бы одну четверку,
    //  иначе в методе необходимо выбросить RuntimeException.

    public int[] sortOutArray(int[] arr, int item) {
        int index = 0;
        if (arr == null || arr.length == 0) {
            throw new RuntimeException("Массив пустой");
        }
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == item) {
                index = arr[i];
                position = i + 1;
                count = arr.length - position;
            }
        }
        if (index != item) {
            throw new RuntimeException("Массив не содержит " + item);
        }
        int[] newArr = new int[count];
        System.arraycopy(arr, position, newArr, 0, count);
        return newArr;
    }

    //Написать метод, который проверяет состав массива из чисел 1 и 4.
    // Если в нем нет хоть одной четверки или единицы, то метод вернет false
    public boolean searchOneAndFour(int[] arr, int item, int item2) {
        if (arr == null || arr.length == 0) {
            throw new RuntimeException("Массив пустой");
        }
        Arrays.sort(arr);
        System.out.println(Arrays.toString(arr));
        int index = Arrays.binarySearch(arr, 1);
        int index2 = Arrays.binarySearch(arr, 4);
        if (index < 0 || index2 < 0){
            System.out.println("числа в массиве нет");
            return false;
        }
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] != 1 & arr[i] != 4) return false;
        }
        return true;
    }


    public static void main(String[] args) {

        //System.out.println(searchOneAndFour(arr6,1,4));
    }

}
