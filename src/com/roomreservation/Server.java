package com.roomreservation;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.roomreservation.ConsoleColours.*;
import static com.roomreservation.CampusInformation.*;

public class Server {

    public static void main(String[] args) {
        try {
            if (args.length <= 1) {
                String campus = getCampus(args[0]).toLowerCase();
                startRMIServer(campus);
                startUDPServer(campus); // For internal communication between servers
            } else {
                System.err.println("Please only specify one parameter");
                System.exit(1);
            }
        }
        catch (Exception e){
            System.err.println("Usage: java Server [CAMPUS]");
            System.exit(1);
        }
    }

    private static void startRMIServer(String campus) throws RemoteException, MalformedURLException {
        String registryURL;
        RoomReservation roomReservation;
        switch (campus){
            case "dvl":
                roomReservation = new RoomReservation(campus);
                registryURL = "rmi://" + host + ":" + dvlRMIPort + "/server";
                LocateRegistry.createRegistry(dvlRMIPort);
                printWelcome(campus);
                break;
            case "kkl":
                roomReservation = new RoomReservation(campus);
                registryURL = "rmi://" + host + ":" + kklRMIPort + "/server";
                LocateRegistry.createRegistry(kklRMIPort);
                printWelcome(campus);
                break;
            default:
                roomReservation = new RoomReservation(campus);
                registryURL = "rmi://" + host + ":" + wstRMIPort + "/server";
                LocateRegistry.createRegistry(wstRMIPort);
                printWelcome(campus);
                break;
        }
        Naming.rebind(registryURL, roomReservation);
        System.out.println("RMI Server ready");
    }

    private static void startUDPServer(String campus){
        DatagramSocket datagramSocket = null;
        try {
            switch (campus){
                case "dvl":
                    datagramSocket = new DatagramSocket(dvlUDPPort);
                    break;
                case "kkl":
                    datagramSocket = new DatagramSocket(kklUDPPort);
                    break;
                default:
                    datagramSocket = new DatagramSocket(wstUDPPort);
                    break;
            }
            System.out.println("UDP Server ready");
            byte[] buffer = new byte[1000];

            while (true){
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(request);
                DatagramPacket reply = new DatagramPacket(request.getData(), request.getLength(), request.getAddress(), request.getPort());
                datagramSocket.send(reply);
            }
        }
        catch (SocketException e){
            System.out.println("Socket: " + e.getMessage());
            System.exit(1);
        }
        catch (IOException e){
            System.out.println("IO Exception: " + e.getMessage());
            System.exit(1);
        }
        catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            System.exit(1);
        }
        finally {
            if (datagramSocket != null)
                datagramSocket.close();
        }
    }

    private static String getCampus(String campus) throws IOException {
        Pattern pattern = Pattern.compile("(dvl|kkl|wst)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(campus);
        if (!matcher.find()) {
            System.out.printf(ANSI_RED + "Invalid campus! Campus must be (DVL/KKL/WST)");
            System.exit(1);
        }
        return campus.toLowerCase();
    }

    private static void printWelcome(String campus){
        System.out.println("==============================");
        System.out.println("Welcome to the " + campus.toUpperCase() + " campus!");
        System.out.println("==============================");
    }
}
