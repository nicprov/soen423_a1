package com.roomreservation;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class StudentServer {

    private static final String host = "localhost";
    private static final int portNumber = 6000;

    public static void main(String[] args) {
        InputStreamReader is = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(is);
        try {
            RoomReservationStudent roomReservationStudent = new RoomReservationStudent();
            String registryURL = "rmi://" + host + ":" + portNumber + "/server";
            LocateRegistry.createRegistry(portNumber);
            Naming.rebind(registryURL, roomReservationStudent);
            System.out.println("Student Server ready");
        } catch (Exception e) {
            System.out.println("Unable to start student server: " + e);
        }
    }
}
