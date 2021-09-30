package com.roomreservation;

import com.roomreservation.collection.Entry;
import com.roomreservation.collection.LinkedPositionalList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.sql.Timestamp;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.roomreservation.ConsoleColours.*;
import static com.roomreservation.CampusInformation.*;

public class Server {

    public static void main(String[] args) {
        InputStreamReader is = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(is);
        try {
            String registryURL;
            String campus = getCampus(br);
            RoomReservation roomReservation = new RoomReservation();
            switch (campus.toLowerCase()){
                case "dvl":
                    registryURL = "rmi://" + host + ":" + dvlPort + "/server";
                    LocateRegistry.createRegistry(dvlPort);
                    printWelcome(campus);
                    break;
                case "kkl":
                    registryURL = "rmi://" + host + ":" + kklPort + "/server";
                    LocateRegistry.createRegistry(kklPort);
                    printWelcome(campus);
                    break;
                default:
                    registryURL = "rmi://" + host + ":" + wstPort + "/server";
                    LocateRegistry.createRegistry(wstPort);
                    printWelcome(campus);
                    break;
            }
            Naming.rebind(registryURL, roomReservation);
            System.out.println("Server ready");
        } catch (Exception e) {
            System.out.println("Unable to start admin server: " + e);
        }
    }

    private static String getCampus(BufferedReader br) throws IOException {
        System.out.printf("Enter campus ID (DVL/KKL/WST): ");
        String campus = br.readLine().trim();
        Pattern pattern = Pattern.compile("(dvl|kkl|wst)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(campus);
        while (!matcher.find()){
            System.out.printf(ANSI_RED + "Invalid campus! Please enter a valid campus ID (DVL/KKL/WST): ");
            campus = br.readLine().trim();
            matcher = pattern.matcher(campus);
        }
        System.out.println(ANSI_GREEN + "Valid campus" + RESET);
        return campus;
    }

    private static void printWelcome(String campus){
        System.out.println("\n==============================");
        System.out.println("Welcome to the " + campus.toUpperCase() + " campus!");
        System.out.println("==============================");
    }
}
