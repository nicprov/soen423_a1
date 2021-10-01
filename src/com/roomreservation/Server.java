package com.roomreservation;

import com.roomreservation.protobuf.protos.UdpRequest;
import com.roomreservation.protobuf.protos.UdpResponse;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.roomreservation.ConsoleColours.*;
import static com.roomreservation.CampusInformation.*;
import static com.roomreservation.protobuf.protos.UdpRequestActions.BookRoom;
import static com.roomreservation.protobuf.protos.UdpRequestActions.GetAvailableTimeslots;

public class Server {

    public static void main(String[] args) {
        try {
            if (args.length <= 1) {
                Campus campus = getCampus(args[0]);
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

    private static void startRMIServer(Campus campus) throws RemoteException, MalformedURLException {
        String registryURL;
        RoomReservation roomReservation;
        switch (campus){
            case DVL:
                roomReservation = new RoomReservation(campus);
                registryURL = "rmi://" + host + ":" + dvlRMIPort + "/server";
                LocateRegistry.createRegistry(dvlRMIPort);
                printWelcome(campus);
                break;
            case KKL:
                roomReservation = new RoomReservation(campus);
                registryURL = "rmi://" + host + ":" + kklRMIPort + "/server";
                LocateRegistry.createRegistry(kklRMIPort);
                printWelcome(campus);
                break;
            case WST:
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

    private static void startUDPServer(Campus campus){
        DatagramSocket datagramSocket = null;
        try {
            switch (campus){
                case DVL:
                    datagramSocket = new DatagramSocket(dvlUDPPort);
                    break;
                case KKL:
                    datagramSocket = new DatagramSocket(kklUDPPort);
                    break;
                case WST:
                default:
                    datagramSocket = new DatagramSocket(wstUDPPort);
                    break;
            }
            System.out.println("UDP Server ready");
            byte[] buffer = new byte[1000];

            while (true){
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(request);

                // Decode udpRequest object
                UdpRequest udpRequest = UdpRequest.parseFrom(trim(request.getData()));


                // Perform action
                UdpResponse.Builder udpResponse = UdpResponse.newBuilder();

                // Encode udpResponse object
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

    private static Campus getCampus(String campus) throws IOException {
        Pattern pattern = Pattern.compile("(dvl|kkl|wst)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(campus);
        if (!matcher.find()) {
            System.out.printf(ANSI_RED + "Invalid campus! Campus must be (DVL/KKL/WST)");
            System.exit(1);
        }
        switch (campus){
            case "dvl":
                return Campus.DVL;
            case "kkl":
                return Campus.KKL;
            case "wst":
            default:
                return Campus.WST;
        }
    }

    private static void printWelcome(Campus campus){
        System.out.println("==============================");
        System.out.println("Welcome to the " + campus.toString().toUpperCase() + " campus!");
        System.out.println("==============================");
    }

    private static byte[] trim(byte[] bytes) {
        int length = bytes.length - 1;
        while (length >= 0 && bytes[length] == 0)
            --length;
        return Arrays.copyOf(bytes, length + 1);
    }
}
