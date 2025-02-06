package com.biteme.app.cli;

import java.util.Scanner;

public class CLIUtils {

    private CLIUtils() {
        //costruttore privato
    }

    private static final Scanner scanner = new Scanner(System.in);

    public static Scanner getScanner() {
        return scanner;
    }
}
