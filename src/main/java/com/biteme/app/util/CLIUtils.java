package com.biteme.app.util;

import java.util.Scanner;

public class CLIUtils {

    private CLIUtils() {
    }

    private static final Scanner scanner = new Scanner(System.in);

    public static Scanner getScanner() {
        return scanner;
    }
}