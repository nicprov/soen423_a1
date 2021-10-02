package com.roomreservation;

import com.roomreservation.common.Campus;
import com.roomreservation.common.RMIResponse;
import com.roomreservation.protobuf.protos.RequestObject;
import com.roomreservation.protobuf.protos.RequestObjectActions;
import com.roomreservation.protobuf.protos.ResponseObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.roomreservation.common.ConsoleColours.*;
import static com.roomreservation.common.CampusInformation.*;

public class Server {

    private static RoomReservation roomReservation;

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

                // Decode request object
                RequestObject requestObject = RequestObject.parseFrom(trim(request.getData()));

                // Build response object
                ResponseObject responseObject;
                ResponseObject.Builder tempObject;

                // Perform action
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);

                switch (RequestObjectActions.valueOf(requestObject.getAction())){
                    case GetAvailableTimeslots:
                        responseObject = new RMIResponse().toResponseObject(roomReservation.getAvailableTimeSlotOnCampus(dateFormat.parse(requestObject.getDate())));
                        break;
                    case BookRoom:
                        responseObject = new RMIResponse().toResponseObject(roomReservation.bookRoom(requestObject.getIdentifier(), Campus.valueOf(requestObject.getCampusName()), requestObject.getRoomNumber(), dateFormat.parse(requestObject.getDate()), requestObject.getTimeslot()));
                        break;
                    case CancelBooking:
                        responseObject = new RMIResponse().toResponseObject(roomReservation.cancelBooking(requestObject.getBookingId()));
                        break;
                    case IncreaseBookingCount:
                        tempObject = ResponseObject.newBuilder();
                        tempObject.setMessage("Increase Booking Count not supported through UDP");
                        tempObject.setDateTime(roomReservation.dateTimeFormat.format(new Date()));
                        tempObject.setRequestType(RequestObjectActions.IncreaseBookingCount.toString());
                        tempObject.setRequestParameters("None");
                        tempObject.setStatus(false);
                        responseObject = tempObject.build();
                        break;
                    case CreateRoom:
                        tempObject = ResponseObject.newBuilder();
                        tempObject.setMessage("Create Room not supported through UDP");
                        tempObject.setDateTime(roomReservation.dateTimeFormat.format(new Date()));
                        tempObject.setRequestType(RequestObjectActions.CreateRoom.toString());
                        tempObject.setRequestParameters("None");
                        tempObject.setStatus(false);
                        responseObject = tempObject.build();
                        break;
                    case DeleteRoom:
                    default:
                        tempObject = ResponseObject.newBuilder();
                        tempObject.setMessage("Delete Room not supported through UDP");
                        tempObject.setDateTime(roomReservation.dateTimeFormat.format(new Date()));
                        tempObject.setRequestType(RequestObjectActions.DeleteRoom.toString());
                        tempObject.setRequestParameters("None");
                        tempObject.setStatus(false);
                        responseObject = tempObject.build();
                        break;
                }
                // Encode response object
                byte[] response = responseObject.toByteArray();
                DatagramPacket reply = new DatagramPacket(response, response.length, request.getAddress(), request.getPort());
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
