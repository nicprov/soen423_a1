package com.roomreservation;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.Naming;

public class Client {

    public static void main(String[] args) {
        try {
            InputStreamReader is = new InputStreamReader(System.in);
            BufferedReader br = new BufferedReader(is);
            String registryURL = "rmi://localhost:5000/test";
            RoomReservationInterface roomReservation = (RoomReservationInterface) Naming.lookup(registryURL);
            System.out.println("Lookup completed");
            System.out.println(roomReservation.cancelBooking(1));
        } catch (Exception e){
            System.out.println("Unable to start client: " + e);
        }
    }
}
