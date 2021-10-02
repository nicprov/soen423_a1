package com.roomreservation;

import com.roomreservation.collection.Entry;
import com.roomreservation.collection.LinkedPositionalList;
import com.roomreservation.collection.Node;
import com.roomreservation.collection.Position;
import com.roomreservation.common.Campus;
import com.roomreservation.common.RMIResponse;
import com.roomreservation.protobuf.protos.RequestObject;
import com.roomreservation.protobuf.protos.ResponseObject;
import com.roomreservation.protobuf.protos.RequestObjectActions;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.roomreservation.common.CampusInformation.*;
import static com.roomreservation.common.ConsoleColours.ANSI_RED;
import static com.roomreservation.common.ConsoleColours.RESET;

public class RoomReservation extends UnicastRemoteObject implements RoomReservationInterface {

    private final LinkedPositionalList<Entry<Date, LinkedPositionalList<Entry<Integer, LinkedPositionalList<Entry<String, LinkedPositionalList<Entry<String, String>>>>>>>> database;
    private final Campus campus;
    public final DateFormat dateTimeFormat;
    public final DateFormat dateFormat;

    protected RoomReservation(Campus campus) throws RemoteException {
        super();
        this.database = new LinkedPositionalList<>();
        this.campus = campus;
        this.dateTimeFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        this.dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    }

    @Override
    public RMIResponse createRoom(int roomNumber, Date date, ArrayList<String> listOfTimeSlots) throws RemoteException {
        Position<Entry<Date, LinkedPositionalList<Entry<Integer, LinkedPositionalList<Entry<String, LinkedPositionalList<Entry<String, String>>>>>>>> datePosition = findDate(date);
        boolean timeSlotCreated = false;
        boolean roomExist = false;
        if (datePosition == null){
            // Date not found so create date entry
            LinkedPositionalList<Entry<String, LinkedPositionalList<Entry<String, String>>>> timeslots = new LinkedPositionalList<>();
            for (String timeslot: listOfTimeSlots){
                timeslots.addFirst(new Node<>(timeslot, null));
            }
            database.addFirst(new Node<>(date, new LinkedPositionalList<>(new Node<>(roomNumber, timeslots))));
        } else {
            // Date exist, check if room exist
            Position<Entry<Integer, LinkedPositionalList<Entry<String, LinkedPositionalList<Entry<String, String>>>>>> roomPosition = findRoom(roomNumber, datePosition);
            if (roomPosition == null) {
                // Room not found so create room
                LinkedPositionalList<Entry<String, LinkedPositionalList<Entry<String, String>>>> timeslots = new LinkedPositionalList<>();
                for (String timeslot: listOfTimeSlots){
                    timeslots.addFirst(new Node<>(timeslot, null));
                }
                datePosition.getElement().getValue().addFirst(new Node<>(roomNumber, timeslots));
            } else {
                // Room exist, so check if timeslot exist
                roomExist = true;
                for (String timeslot: listOfTimeSlots){
                    if (findTimeslot(timeslot, roomPosition) == null) {
                        // Timeslot does not exist, so create it, skip otherwise
                        roomPosition.getElement().getValue().addFirst(new Node<>(timeslot, null));
                        timeSlotCreated = true;
                    }
                }
            }
        }
        RMIResponse rmiResponse = new RMIResponse();
        if (!roomExist) {
            rmiResponse.setMessage("Created room (" + roomNumber + ")");
            rmiResponse.setStatus(true);
        } else if (!timeSlotCreated){
            rmiResponse.setMessage("Room already exist with specified timeslots");
            rmiResponse.setStatus(false);
        } else {
            rmiResponse.setMessage("Added timeslots to room (" + roomNumber + ")");
            rmiResponse.setStatus(true);
        }
        rmiResponse.setDatetime(new Date());
        rmiResponse.setRequestType(RequestObjectActions.CreateRoom.toString());
        rmiResponse.setRequestParameters("Room number: " + roomNumber + ", Date: " + dateFormat.format(date) + ", List of Timeslots: " + listOfTimeSlots);
        return rmiResponse;
    }

    @Override
    public RMIResponse deleteRoom(int roomNumber, Date date, ArrayList<String> listOfTimeSlots) throws RemoteException {
        Position<Entry<Date, LinkedPositionalList<Entry<Integer, LinkedPositionalList<Entry<String, LinkedPositionalList<Entry<String, String>>>>>>>> datePosition = findDate(date);
        boolean timeslotExist = false;
        if (datePosition != null){
            // Date exist, check if room exist
            Position<Entry<Integer, LinkedPositionalList<Entry<String, LinkedPositionalList<Entry<String, String>>>>>> roomPosition = findRoom(roomNumber, datePosition);
            if (roomPosition != null) {
                // Room found, search for timeslots
                for (String timeslot: listOfTimeSlots){
                    Position<Entry<String, LinkedPositionalList<Entry<String, String>>>> timeslotPosition = findTimeslot(timeslot, roomPosition);
                    if (timeslotPosition != null) {
                        //TODO Reduce booking count

                        // Timeslot exists, so delete it
                        roomPosition.getElement().getValue().remove(timeslotPosition);
                        timeslotExist = true;
                    }
                }
            }
        }
        RMIResponse rmiResponse = new RMIResponse();
        if (!timeslotExist){
            rmiResponse.setMessage("No timeslots to delete on (" + dateFormat.format(date) + ")");
            rmiResponse.setStatus(false);
        } else {
            rmiResponse.setMessage("Removed timeslots from room (" + roomNumber + ")");
            rmiResponse.setStatus(true);
        }
        rmiResponse.setDatetime(new Date());
        rmiResponse.setRequestType(RequestObjectActions.CreateRoom.toString());
        rmiResponse.setRequestParameters("Room number: " + roomNumber + ", Date: " + dateFormat.format(date) + ", List of Timeslots: " + listOfTimeSlots);
        return rmiResponse;
    }

    @Override
    public RMIResponse bookRoom(String identifier, Campus campus, int roomNumber, Date date, String timeslot) throws RemoteException {
        if (campus.equals(this.campus))
            return bookRoomOnCampus(identifier, roomNumber, date, timeslot);
        else {
            // Perform action on remote server
            RequestObject.Builder requestObject = RequestObject.newBuilder();
            requestObject.setAction(RequestObjectActions.BookRoom.toString());
            requestObject.setIdentifier(identifier);
            requestObject.setRoomNumber(roomNumber);
            requestObject.setDate(dateFormat.format(date));
            requestObject.setTimeslot(timeslot);
            return udpTransfer(campus, requestObject.build());
        }
    }

    @Override
    public RMIResponse getAvailableTimeSlot(Date date) {
        // Build new proto request object
        RequestObject.Builder requestObject = RequestObject.newBuilder();
        requestObject.setAction(RequestObjectActions.GetAvailableTimeslots.toString());
        requestObject.setDate(dateFormat.format(date));

        // Get response object from each campus
        RMIResponse dvlTimeslots = udpTransfer(Campus.DVL, requestObject.build());
        RMIResponse kklTimeslots = udpTransfer(Campus.KKL, requestObject.build());
        RMIResponse wstTimeslots = udpTransfer(Campus.WST, requestObject.build());
        System.out.println("test");
        String message = "";
        if (dvlTimeslots != null)
            message += "DVL " + dvlTimeslots.getMessage() + "\n";
        else
            message += "DVL (no response from server)\n";
        if (kklTimeslots != null)
            message += "KKL " + kklTimeslots.getMessage() + "\n";
        else
            message += "KKL (no response from server)\n";
        if (wstTimeslots != null)
            message += "WST " + wstTimeslots.getMessage();
        else
            message += "WST (no response from server)";

        //  Create response object for rmi
        RMIResponse rmiResponse = new RMIResponse();
        rmiResponse.setMessage(message);
        rmiResponse.setDatetime(new Date());
        rmiResponse.setRequestType(RequestObjectActions.GetAvailableTimeslots.toString());
        rmiResponse.setRequestParameters("Date: " + dateFormat.format(date));
        rmiResponse.setStatus(true);
        return rmiResponse;
    }

    @Override
    public RMIResponse cancelBooking(String bookingId) {
        boolean bookingExist = false;
        for (Position<Entry<Date, LinkedPositionalList<Entry<Integer, LinkedPositionalList<Entry<String, LinkedPositionalList<Entry<String, String>>>>>>>> dateNext : database.positions()) {
            for (Position<Entry<Integer, LinkedPositionalList<Entry<String, LinkedPositionalList<Entry<String, String>>>>>> roomNext : dateNext.getElement().getValue().positions()) {
                for (Position<Entry<String, LinkedPositionalList<Entry<String, String>>>> timeslotNext : roomNext.getElement().getValue().positions()) {
                    if (timeslotNext.getElement().getValue() != null){
                        for (Position<Entry<String, String>> timeslotPropertiesNext : timeslotNext.getElement().getValue().positions()) {
                            if (timeslotPropertiesNext.getElement().getKey().equals("bookingId") && timeslotPropertiesNext.getElement().getValue().equals(bookingId)) {
                                // TODO Reduce count for student

                                // Cancel booking
                                timeslotNext.getElement().getValue().set(timeslotPropertiesNext, null);
                                bookingExist = true;
                            }
                        }
                    }
                }
            }
        }
        RMIResponse rmiResponse = new RMIResponse();
        if (!bookingExist){
            rmiResponse.setMessage("Booking (" + bookingId + ") does not exist");
            rmiResponse.setStatus(false);
        } else {
            rmiResponse.setMessage("Cancelled booking (" + bookingId + ")");
            rmiResponse.setStatus(true);
        }
        rmiResponse.setDatetime(new Date());
        rmiResponse.setRequestType(RequestObjectActions.CreateRoom.toString());
        rmiResponse.setRequestParameters("Booking Id: " + bookingId);
        return rmiResponse;
    }

    public RMIResponse getAvailableTimeSlotOnCampus(Date date){
        int counter = 0;
        for (Position<Entry<Date, LinkedPositionalList<Entry<Integer, LinkedPositionalList<Entry<String, LinkedPositionalList<Entry<String, String>>>>>>>> dateNext : database.positions()) {
            for (Position<Entry<Integer, LinkedPositionalList<Entry<String, LinkedPositionalList<Entry<String, String>>>>>> roomNext : dateNext.getElement().getValue().positions()) {
                for (Position<Entry<String, LinkedPositionalList<Entry<String, String>>>> timeslotNext : roomNext.getElement().getValue().positions()) {
                    if (timeslotNext.getElement().getValue() == null)
                        counter++;
                }
            }
        }
        RMIResponse rmiResponse = new RMIResponse();
        rmiResponse.setMessage(Integer.toString(counter));
        rmiResponse.setDatetime(new Date());
        rmiResponse.setRequestType(RequestObjectActions.GetAvailableTimeslots.toString());
        rmiResponse.setRequestParameters("Date: " + dateFormat.format(date));
        rmiResponse.setStatus(true);
        return rmiResponse;
    }

    private RMIResponse bookRoomOnCampus(String identifier, int roomNumber, Date date, String timeslot) {
        boolean timeslotExist = false;
        String bookingId = "";
        Position<Entry<Date, LinkedPositionalList<Entry<Integer, LinkedPositionalList<Entry<String, LinkedPositionalList<Entry<String, String>>>>>>>> datePosition = findDate(date);
        if (datePosition != null) {
            // Date exist, check if room exist
            Position<Entry<Integer, LinkedPositionalList<Entry<String, LinkedPositionalList<Entry<String, String>>>>>> roomPosition = findRoom(roomNumber, datePosition);
            if (roomPosition != null) {
                // Room found, search for timeslots
                Position<Entry<String, LinkedPositionalList<Entry<String, String>>>> timeslotPosition = findTimeslot(timeslot, roomPosition);

                // Check if timeslot exist
                if (timeslotPosition != null) {
                    timeslotExist = true;
                    //TODO Reduce booking count
                    if (timeslotPosition.getElement().getValue() == null){
                        // Create timeslot and add attributes
                        bookingId = UUID.randomUUID().toString();
                        roomPosition.getElement().getValue().set(timeslotPosition, new Node<>(timeslot, new LinkedPositionalList<>()));
                        timeslotPosition.getElement().getValue().addFirst(new Node<>("bookingId", bookingId));
                        timeslotPosition.getElement().getValue().addFirst(new Node<>("studentId", identifier));
                    }
                }
            }
        }
        RMIResponse rmiResponse = new RMIResponse();
        if (!timeslotExist){
            rmiResponse.setMessage("Timeslot (" + timeslot + ") does not exist on (" + dateFormat.format(date) + ")");
            rmiResponse.setStatus(false);
        } else {
            rmiResponse.setMessage("Timeslot (" + timeslot + ") has been booked, booking ID: " + bookingId);
            rmiResponse.setStatus(true);
        }
        rmiResponse.setDatetime(new Date());
        rmiResponse.setRequestType(RequestObjectActions.CreateRoom.toString());
        rmiResponse.setRequestParameters("Identifier: " + identifier + ", Room Number: " + roomNumber + ", Date: " + dateFormat.format(date) + ", Timeslot: " + timeslot);
        return rmiResponse;
    }

    private RMIResponse udpTransfer(Campus campus, RequestObject requestObject){
        //TODO add concurrency
        DatagramSocket datagramSocket = null;
        try {
            int remotePort;
            datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(1000); // Set timeout
            InetAddress host = InetAddress.getLocalHost();
            switch (campus){
                case DVL:
                    remotePort = dvlUDPPort;
                    break;
                case KKL:
                    remotePort = kklUDPPort;
                    break;
                case WST:
                default:
                    remotePort = wstUDPPort;
                    break;
            }
            DatagramPacket request = new DatagramPacket(requestObject.toByteArray(), requestObject.toByteArray().length, host, remotePort);
            datagramSocket.send(request);
            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            datagramSocket.receive(reply);
            return new RMIResponse().fromResponseObject(ResponseObject.parseFrom(trim(reply.getData())));
        }
        catch (SocketException e){
            System.out.println(ANSI_RED + "Socket: " + e.getMessage() + RESET);
        } catch (IOException e){
            System.out.println(ANSI_RED + "IO: " + e.getMessage() + RESET);
        } catch (ParseException e) {
            System.out.println(ANSI_RED + "Exception: " + e.getMessage() + RESET);
        } finally {
            if (datagramSocket != null)
                datagramSocket.close();
        }
        return null;
    }

    private static byte[] trim(byte[] bytes) {
        int length = bytes.length - 1;
        while (length >= 0 && bytes[length] == 0)
            --length;
        return Arrays.copyOf(bytes, length + 1);
    }

    private Position<Entry<Date, LinkedPositionalList<Entry<Integer, LinkedPositionalList<Entry<String, LinkedPositionalList<Entry<String, String>>>>>>>> findDate(Date date){
        for (Position<Entry<Date, LinkedPositionalList<Entry<Integer, LinkedPositionalList<Entry<String, LinkedPositionalList<Entry<String, String>>>>>>>> dateNext : database.positions()) {
            if (dateNext.getElement().getKey().equals(date))
                return dateNext;
        }
        return null;
    }

    private Position<Entry<Integer, LinkedPositionalList<Entry<String, LinkedPositionalList<Entry<String, String>>>>>> findRoom(int roomNumber, Position<Entry<Date, LinkedPositionalList<Entry<Integer, LinkedPositionalList<Entry<String, LinkedPositionalList<Entry<String, String>>>>>>>> datePosition){
        for (Position<Entry<Integer, LinkedPositionalList<Entry<String, LinkedPositionalList<Entry<String, String>>>>>> roomNext : datePosition.getElement().getValue().positions()) {
            if (roomNext.getElement().getKey().equals(roomNumber))
                return roomNext;
        }
        return null;
    }

    private Position<Entry<String, LinkedPositionalList<Entry<String, String>>>> findTimeslot(String timeslot, Position<Entry<Integer, LinkedPositionalList<Entry<String, LinkedPositionalList<Entry<String, String>>>>>> room){
        for (Position<Entry<String, LinkedPositionalList<Entry<String, String>>>> timeslotNext : room.getElement().getValue().positions()) {
            if (timeslotNext.getElement().getKey().equals(timeslot))
                return timeslotNext;
        }
        return null;
    }
}
