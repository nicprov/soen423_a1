package com.roomreservation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client {

    public static final int adminServerPort = 5000;
    public static final int studentServerPort = 6000;
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String DEFAULT = "\033[0m";

    public static void main(String[] args) {
        InputStreamReader is = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(is);
        try {
            getUser(br);
            //String registryURL = "rmi://localhost:5000/test";
            //RoomReservationStudentInterface roomReservation = (RoomReservationStudentInterface) Naming.lookup(registryURL);
            System.out.println("Lookup completed");
            //System.out.println(roomReservation.cancelBooking(1));
        } catch (Exception e){
            System.out.println("Unable to start client: " + e);
        }
    }

    public static String getUser(BufferedReader br) throws IOException {
        System.out.printf("Enter unique identifier: ");
        String identifier = br.readLine().trim();
        Pattern pattern = Pattern.compile("(dvla|dvls)[0-9]{4}$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(identifier);
        while (!matcher.find()){
            System.out.printf(ANSI_RED + "Invalid identifier! Please enter your unique identifier: ");
            identifier = br.readLine().trim();
            matcher = pattern.matcher(identifier);
        }
        System.out.println(ANSI_GREEN + "Valid identifier" + DEFAULT);
        return identifier;
    }
}
