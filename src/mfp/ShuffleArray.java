package mfp;

import java.util.Random;

/**
 * A utility class for shuffling the order of recommendations
 */
public class ShuffleArray {
  public static void shuffleArray(String[] a) {
    int n = a.length;
    Random random = new Random();
    random.nextInt();
    for (int i = 0; i < n; i++) {
      int change = i + random.nextInt(n - i);
      swap(a, i, change);
    }
  }

  private static void swap(String[] a, int i, int change) {
	String helper = a[i];
    a[i] = a[change];
    a[change] = helper;
  }

  public static void main(String[] args) {
	  String[] a = new String[] { "1", "2", "3", "4", "5", "6", "7" };
    shuffleArray(a);
    for (String i : a) {
      System.out.println(i);
    }
  }
} 