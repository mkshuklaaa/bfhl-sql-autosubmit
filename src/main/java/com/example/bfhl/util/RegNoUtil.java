package com.example.bfhl.util;

public class RegNoUtil {
  public static int lastTwoDigits(String regNo) {
    if (regNo == null) return 0;
    String digits = regNo.replaceAll("\\D", "");
    if (digits.length() >= 2) {
      return Integer.parseInt(digits.substring(digits.length() - 2));
    } else if (digits.length() == 1) {
      return Integer.parseInt(digits);
    }
    return 0;
  }
}
