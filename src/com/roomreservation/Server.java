package com.roomreservation;

import com.roomreservation.common.Campus;
import com.roomreservation.common.RMIResponse;
import com.roomreservation.protobuf.protos.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.roomreservation.common.ConsoleColours.*;

public class Server {

    private static final String HOST = "localhost";
    private static final String PATH = "server";

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

    private static void startRMIServer(Campus campus) throws IOException {
        String registryURL;
        roomReservation = new RoomReservation(campus);
        int remotePort = getServerPort();
        if (remotePort == -1){
            System.out.println(ANSI_RED + "Unable to get available port, central repository may be down" + RESET);
            System.exit(1);
        }
        if (!registerServer(campus.toString(), "rmi", remotePort)){
            System.out.println(ANSI_RED + "Unable to register server, central repository may be down" + RESET);
            System.exit(1);
        }
        registryURL = "rmi://" + HOST + ":" + remotePort + "/" + PATH;
        LocateRegistry.createRegistry(remotePort);
        printWelcome(campus);
        Naming.rebind(registryURL, roomReservation);
        System.out.println("RMI Server ready (port: " + remotePort + ")");
    }

    private static void startUDPServer(Campus campus){
        DatagramSocket datagramSocket = null;
        try {
            int remotePort = getServerPort();
            if (remotePort == -1){
                System.out.println(ANSI_RED + "Unable to get available port, central repository may be down" + RESET);
                System.exit(1);
            }
            if (!registerServer(campus.toString(), "udp", remotePort)){
                System.out.println(ANSI_RED + "Unable to register server, central repository may be down" + RESET);
                System.exit(1);
            }
            datagramSocket = new DatagramSocket(remotePort);
            System.out.println("UDP Server ready (port: " + remotePort + ")");
            byte[] buffer = new byte[1000];

            while (true){
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(datagramPacket);

                // Launch a new thread for each request
                DatagramSocket finalDatagramSocket = datagramSocket;
                Thread thread = new Thread(() -> {
                    try {
                        handleUDPRequest(finalDatagramSocket, datagramPacket);
                    } catch (IOException | ParseException e) {
                        System.out.println(ANSI_RED + "Exception: " + e.getMessage() + RESET);
                    }
                });
                thread.start();
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

    private static void handleUDPRequest(DatagramSocket datagramSocket, DatagramPacket datagramPacket) throws IOException, ParseException {
        // Decode request object
        RequestObject requestObject = RequestObject.parseFrom(trim(datagramPacket));

        // Build response object
        ResponseObject responseObject;
        ResponseObject.Builder tempObject;

        // Perform action
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

        switch (RequestObjectAction.valueOf(requestObject.getAction())){
            case GetAvailableTimeslots:
                responseObject = new RMIResponse().toResponseObject(roomReservation.getAvailableTimeSlotOnCampus(dateFormat.parse(requestObject.getDate())));
                break;
            case BookRoom:
                responseObject = new RMIResponse().toResponseObject(roomReservation.bookRoom(requestObject.getIdentifier(), Campus.valueOf(requestObject.getCampusName()), requestObject.getRoomNumber(), dateFormat.parse(requestObject.getDate()), requestObject.getTimeslot()));
                break;
            case CancelBooking:
                responseObject = new RMIResponse().toResponseObject(roomReservation.cancelBooking(requestObject.getIdentifier(), requestObject.getBookingId()));
                break;
            case GetBookingCount:
                responseObject = new RMIResponse().toResponseObject(roomReservation.getBookingCount(requestObject.getIdentifier(), dateFormat.parse(requestObject.getDate())));
                break;
            case CreateRoom:
                tempObject = ResponseObject.newBuilder();
                tempObject.setMessage("Create Room not supported through UDP");
                tempObject.setDateTime(roomReservation.dateFormat.format(new Date()));
                tempObject.setRequestType(RequestObjectAction.CreateRoom.toString());
                tempObject.setRequestParameters("None");
                tempObject.setStatus(false);
                responseObject = tempObject.build();
                break;
            case DeleteRoom:
            default:
                tempObject = ResponseObject.newBuilder();
                tempObject.setMessage("Delete Room not supported through UDP");
                tempObject.setDateTime(roomReservation.dateFormat.format(new Date()));
                tempObject.setRequestType(RequestObjectAction.DeleteRoom.toString());
                tempObject.setRequestParameters("None");
                tempObject.setStatus(false);
                responseObject = tempObject.build();
                break;
        }
        // Encode response object
        byte[] response = responseObject.toByteArray();
        DatagramPacket reply = new DatagramPacket(response, response.length, datagramPacket.getAddress(), datagramPacket.getPort());
        datagramSocket.send(reply);
    }

    private static Campus getCampus(String campus) {
        Pattern pattern = Pattern.compile("(dvl|kkl|wst)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(campus);
        if (!matcher.find()) {
            System.out.print(ANSI_RED + "Invalid campus! Campus must be (DVL/KKL/WST)");
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

    private static byte[] trim(DatagramPacket packet) {
        byte[] data = new byte[packet.getLength()];
        System.arraycopy(packet.getData(), packet.getOffset(), data, 0, packet.getLength());
        return data;
    }

    private static int getServerPort(){
        CentralRepository.Builder centralRepositoryRequest = CentralRepository.newBuilder();
        centralRepositoryRequest.setAction(CentralRepositoryAction.GetAvailablePort.toString());
        CentralRepository centralRepositoryResponse = udpTransfer(centralRepositoryRequest.build());
        if (centralRepositoryResponse == null)
            return -1;
        if (!centralRepositoryResponse.getStatus())
            return -1;
        return Integer.parseInt(centralRepositoryResponse.getPort());
    }

    private static boolean registerServer(String campus, String type, int port){
        CentralRepository.Builder centralRepositoryRequest = CentralRepository.newBuilder();
        centralRepositoryRequest.setAction(CentralRepositoryAction.Register.toString());
        centralRepositoryRequest.setPort(Integer.toString(port));
        centralRepositoryRequest.setPath(PATH);
        centralRepositoryRequest.setHost(HOST);
        centralRepositoryRequest.setType(type);
        centralRepositoryRequest.setCampus(campus);
        CentralRepository centralRepositoryResponse = udpTransfer(centralRepositoryRequest.build());
        if (centralRepositoryResponse != null)
            return centralRepositoryResponse.getStatus();
        return false;
    }

    private static CentralRepository udpTransfer(CentralRepository centralRepositoryRequest){
        DatagramSocket datagramSocket = null;
        try {
            int remotePort = 1024;
            datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(1000); // Set timeout
            InetAddress host = InetAddress.getLocalHost();
            DatagramPacket request = new DatagramPacket(centralRepositoryRequest.toByteArray(), centralRepositoryRequest.toByteArray().length, host, remotePort);
            datagramSocket.send(request);
            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            datagramSocket.receive(reply);
            return CentralRepository.parseFrom(trim(reply));
        }
        catch (SocketException e){
            System.out.println(ANSI_RED + "Socket: " + e.getMessage() + RESET);
        } catch (IOException e){
            System.out.println(ANSI_RED + "IO: " + e.getMessage() + RESET);
        } finally {
            if (datagramSocket != null)
                datagramSocket.close();
        }
        return null;
    }
}
