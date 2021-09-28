package com.roomreservation;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class AdminServer {

    private static final String host = "localhost";
    private static final int portNumber = 5000;

    public static void main(String[] args) {
        InputStreamReader is = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(is);
        try {
            RoomReservationAdmin roomReservationAdmin = new RoomReservationAdmin();
            String registryURL = "rmi://" + host + ":" + portNumber + "/server";
            LocateRegistry.createRegistry(portNumber);
            Naming.rebind(registryURL, roomReservationAdmin);
            System.out.println("Admin Server ready");
        } catch (Exception e) {
            System.out.println("Unable to start admin server: " + e);
        }
    }
}
