package com.roomreservation;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class Server {

    public static void main(String[] args) {
        InputStreamReader is = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(is);
        String portNum, registryURL;
        try {
            RoomReservation roomReservation = new RoomReservation();
            registryURL = "rmi://localhost:5000/test";
            LocateRegistry.createRegistry(5000);
            Naming.rebind(registryURL, roomReservation);
            System.out.println("Server ready");
        } catch (Exception e) {
            System.out.println("Unable to start server: " + e);
        }
    }
}
