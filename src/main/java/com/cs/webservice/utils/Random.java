package com.cs.webservice.utils;

public class Random {
    public static String generateAlphanumericString(int targetStringLength) {
        int leftLimit = 48;
        int rightLimit = 122;
        java.util.Random random = new java.util.Random();
        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    public static String generateNumberString(int targetStringLength) {
        int leftLimit = 48;
        int rightLimit = 57;
        java.util.Random random = new java.util.Random();
        return random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
