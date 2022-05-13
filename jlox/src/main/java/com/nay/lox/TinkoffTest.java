package com.nay.lox;

//Даны 2 отсортированных массива с уникальными элементами, найти и вывести их упорядоченное объединение без дубликатов, используя константу доп. памяти.
//
//Input : arr1[] = {1, 3, 4, 5, 7}
//        arr2[] = {2, 3, 5, 6}
//Output : Union = {1, 2, 3, 4, 5, 6, 7}

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TinkoffTest {
  public static void main(String[] args) {
    mergeArrays(List.of(), List.of());
  }
  public static void mergeArrays(List<Integer> arr1, List<Integer> arr2) {
    int ind1 = 0;
    int ind2 = 0;

    while (ind1 < arr1.size() && ind2 < arr2.size()) {
      // make a choice
      if (arr1.get(ind1) < arr2.get(ind2)) {
        System.out.println(arr1.get(ind1));
        ind1++;
        continue;
      }
      if (arr2.get(ind2) < arr1.get(ind1)) {
        System.out.println(arr2.get(ind2));
        ind2++;
        continue;
      }
      if (Objects.equals(arr1.get(ind1), arr2.get(ind2))) {
        System.out.println(arr1.get(ind1));
        ind1++;
        ind2++;
      }
    }
    printFromInd(ind1, arr1);
    printFromInd(ind2, arr2);
  }

  static void printFromInd(int ind, List<Integer> array) {
    for (int i=ind; i<array.size(); i++) {
      System.out.println(array.get(i));
    }
  }

  //На улице стоят n непокрашенных домов в ряд.
  //
  //Известна стоимость покраски каждого дома в каждый цвет (красный, зеленый, синий).
  //
  //Жильцы этих домов большие завистники, поэтому никакие два дома, стоящие рядом, не могут иметь одинакового покраса.
  //
  //Найти минимальную стоимость покраски всех домов при указанных условиях.
  //
  //Входные данные: houses[][3] – массив стоимости покраски в каждый цвет для каждого дома
  //findMinPrice([[1, 2, 3], [3, 2, 1]]) => 2

  //findMinPrice([[1, 2, 3], [3, 2, 1], [3, 2, 1]]) => 2

  //findMinPrice([[1, 2, 3], [2, 3, 1], [6, 5, 1]]) => 2

  // r(2, 1, 1), (4, 4, 2), (8, 7, 5) => 5
  // min(

  static int paint(int[][] houses) {
    if (houses.length == 0) {
      return 0;
    }

    int red = houses[0][0];
    int green = houses[0][1];
    int blue = houses[0][2];

    for (int i = 1; i<houses.length; i++) {
      int new_red = Math.min(green, blue) + houses[i][0];
      int new_green = Math.min(red, blue) + houses[i][1];
      int new_blue = Math.min(red, green) + houses[i][2];

      // update
      red = new_red;
      green = new_green;
      blue = new_blue;
    }

    return Math.min(red, Math.min(blue, green));
  }

}
