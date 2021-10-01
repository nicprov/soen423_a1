package com.roomreservation;

import com.roomreservation.collection.Entry;
import com.roomreservation.collection.LinkedPositionalList;
import com.roomreservation.collection.Node;
import com.roomreservation.collection.Position;
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

import static com.roomreservation.CampusInformation.*;

public class RoomReservation extends UnicastRemoteObject implements RoomReservationInterface {

    private final LinkedPositionalList<Entry<Date, LinkedPositionalList<Entry<Integer, LinkedPositionalList<Entry<String, LinkedPositionalList<Entry<String, String>>>>>>>> database;
    private final Campus campus;
    public final DateFormat dateTimeFormat;

    protected RoomReservation(Campus campus) throws RemoteException {
        super();
        this.database = new LinkedPositionalList<>();
        this.campus = campus;
        this.dateTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    }

    @Override
    public RMIResponse createRoom(int roomNumber, Date date, ArrayList<String> listOfTimeSlots) throws RemoteException {
        // Check whether an entry exist on date
        RMIResponse rmiResponse = new RMIResponse();
        String message = "Date (" + date.toString() + ") entry has been created";
        boolean status = true;

        boolean dateExist = false;
        Iterator<Entry<Date, LinkedPositionalList<Entry<Integer, LinkedPositionalList<Entry<String, LinkedPositionalList<Entry<String, String>>>>>>>> dateIterator = database.iterator();
        while (dateIterator.hasNext()){
            Entry<Date, LinkedPositionalList<Entry<Integer, LinkedPositionalList<Entry<String, LinkedPositionalList<Entry<String, String>>>>>>> dateNext = dateIterator.next();

            // Check if Date exist
            if (dateNext.getKey().equals(date)){
                dateExist = true;
                Iterator<Entry<Integer, LinkedPositionalList<Entry<String, LinkedPositionalList<Entry<String, String>>>>>> roomIterator = dateNext.getValue().iterator();

                boolean roomExist = false;
                Entry<Integer, LinkedPositionalList<Entry<String, LinkedPositionalList<Entry<String, String>>>>> roomNext = null;

                while (roomIterator.hasNext()){
                    roomNext = roomIterator.next();

                    // Check if room exist
                    if (roomNext.getKey().equals(roomNumber)){
                        roomExist = true;

                        // Check if timeslot exist
                        for (String timeslot: listOfTimeSlots){

                            boolean timeslotExist = false;

                            Iterator<Entry<String, LinkedPositionalList<Entry<String, String>>>> timeslotIterator = roomNext.getValue().iterator();
                            while (timeslotIterator.hasNext()) {
                                Entry<String, LinkedPositionalList<Entry<String, String>>> timeslotNext = timeslotIterator.next();

                                if (timeslotNext.getKey().equals(timeslot)){
                                    // Timeslot already exist
                                    timeslotExist = true;
                                    status = false;
                                }
                            }
                            // Add timeslot
                            if (!timeslotExist){
                                roomNext.getValue().addFirst(new Node<>(timeslot, null));
                                message = "Timeslot entries have been added to Room(" + roomNumber + ")";
                            }
                       }
                   }
               }
                // Add room
                if (!roomExist){
                   // Add timeslots
                   LinkedPositionalList<Entry<String, LinkedPositionalList<Entry<String, String>>>> timeslots = new LinkedPositionalList<>();
                   for (String timeslot: listOfTimeSlots){
                       timeslots.addFirst(new Node<>(timeslot, null));
                   }
                   dateNext.getValue().addFirst(new Node<>(roomNumber, timeslots));
                   message = "Room (" + roomNumber + ") entry has been created";
                }

            }
        }
        if (!dateExist) {
            // Add timeslots
            LinkedPositionalList<Entry<String, LinkedPositionalList<Entry<String, String>>>> timeslots = new LinkedPositionalList<>();
            for (String timeslot: listOfTimeSlots){
                timeslots.addFirst(new Node<>(timeslot, null));
            }
            database.addFirst(new Node<>(date, new LinkedPositionalList<>(new Node<>(roomNumber, timeslots))));
        }
        rmiResponse.setMessage(message);
        rmiResponse.setDatetime(new Date());
        rmiResponse.setRequestType(RequestObjectActions.CreateRoom);
        rmiResponse.setRequestParameters("Room number: " + roomNumber + ", Date: " + date.toString() + ", List of Timeslots: " + listOfTimeSlots.toString());
        rmiResponse.setStatus(status);
        return rmiResponse;
    }

    @Override
    public RMIResponse deleteRoom(int roomNumber, Date date, ArrayList<String> listOfTimeSlots) throws RemoteException {
        // Check whether an entry exist on date
        RMIResponse rmiResponse = new RMIResponse();
        boolean status = true;
        String message = "Successfully deleted timeslots associated with Room (\" + roomNumber + \") on Date (\" + date.toString() + \")";
        boolean dateExist = false;
        Iterator<Position<Entry<Date, LinkedPositionalList<Entry<Integer, LinkedPositionalList<Entry<String, LinkedPositionalList<Entry<String, String>>>>>>>>> dateIterator = database.positions().iterator();
        while (dateIterator.hasNext()){
            Position<Entry<Date, LinkedPositionalList<Entry<Integer, LinkedPositionalList<Entry<String, LinkedPositionalList<Entry<String, String>>>>>>>> dateNext = dateIterator.next();

            // Check if Date exist
            if (dateNext.getElement().getKey().equals(date)){
                dateExist = true;
                Iterator<Position<Entry<Integer, LinkedPositionalList<Entry<String, LinkedPositionalList<Entry<String, String>>>>>>> roomIterator = dateNext.getElement().getValue().positions().iterator();

                boolean roomExist = false;
                Position<Entry<Integer, LinkedPositionalList<Entry<String, LinkedPositionalList<Entry<String, String>>>>>> roomNext = null;

                while (roomIterator.hasNext()){
                    roomNext = roomIterator.next();

                    // Check if room exist
                    if (roomNext.getElement().getKey().equals(roomNumber)){
                        roomExist = true;

                        // Check if timeslot exist
                        for (String timeslot: listOfTimeSlots){

                            boolean timeslotExist = false;

                            Iterator<Position<Entry<String, LinkedPositionalList<Entry<String, String>>>>> timeslotIterator = roomNext.getElement().getValue().positions().iterator();
                            while (timeslotIterator.hasNext()) {
                                Position<Entry<String, LinkedPositionalList<Entry<String, String>>>> timeslotNext = timeslotIterator.next();

                                if (timeslotNext.getElement().getKey().equals(timeslot)){
                                    // Timeslot exist
                                    timeslotExist = true;
                                    roomNext.getElement().getValue().remove(timeslotNext);
                                    message = "Timeslot entries have been removed from Room(" + roomNumber + ")";
                                }
                            }
                            // Remove timeslot
                            if (!timeslotExist){
                                message = "No timeslots to erase";
                                status = false;
                            }
                        }

                        // Delete room
                        dateNext.getElement().getValue().remove(roomNext);
                        message = "Room (" + roomNumber + ") has been removed";
                    }
                }
                // Add room
                if (!roomExist){
                    message = "Room (" + roomNumber + ") is not found in database";
                    status = false;
                }

                // Delete date
                database.remove(dateNext);
                message = "Date (" + date.toString() + ") has been removed";
            }
        }
        if (!dateExist) {
            message = "Date (" + date.toString() + ") is not found in database";
            status = false;
        }
        rmiResponse.setMessage(message);
        rmiResponse.setDatetime(new Date());
        rmiResponse.setRequestType(RequestObjectActions.DeleteRoom);
        rmiResponse.setRequestParameters("Room number: " + roomNumber + ", Date: " + date.toString() + ", List of Timeslots: " + listOfTimeSlots.toString());
        rmiResponse.setStatus(status);
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
            requestObject.setDate(date.toString());
            requestObject.setTimeslot(timeslot);
            return udpTransfer(campus, requestObject.build());
        }
    }

    @Override
    public RMIResponse getAvailableTimeSlot(Date date) {
        // Build new proto request object
        RequestObject.Builder requestObject = RequestObject.newBuilder();
        requestObject.setAction(RequestObjectActions.GetAvailableTimeslots.toString());
        requestObject.setDate(date.toString());

        // Get response object
        RMIResponse dvlTimeslots = udpTransfer(Campus.DVL, requestObject.build());
        RMIResponse kklTimeslots = udpTransfer(Campus.KKL, requestObject.build());
        RMIResponse wstTimeslots = udpTransfer(Campus.WST, requestObject.build());
        RMIResponse rmiResponse = new RMIResponse();
        rmiResponse.setMessage("DVL " + dvlTimeslots.getMessage() + ", KKL " + kklTimeslots.getMessage() + ", WST " + wstTimeslots.getMessage());
        rmiResponse.setDatetime(new Date());
        rmiResponse.setRequestType(RequestObjectActions.GetAvailableTimeslots);
        rmiResponse.setRequestParameters("Date: " + date.toString());
        rmiResponse.setStatus(true);
        return rmiResponse;
    }

    @Override
    public RMIResponse cancelBooking(String bookingId) {
        // Check whether an entry exist on date
        RMIResponse rmiResponse = new RMIResponse();
        boolean status = false;
        String message = "Booking (" + bookingId + ") has been cancelled";

        boolean bookingExist = false;
        Iterator<Position<Entry<Date, LinkedPositionalList<Entry<Integer, LinkedPositionalList<Entry<String, LinkedPositionalList<Entry<String, String>>>>>>>>> dateIterator = database.positions().iterator();
        while (dateIterator.hasNext()){
            Position<Entry<Date, LinkedPositionalList<Entry<Integer, LinkedPositionalList<Entry<String, LinkedPositionalList<Entry<String, String>>>>>>>> dateNext = dateIterator.next();

            Iterator<Position<Entry<Integer, LinkedPositionalList<Entry<String, LinkedPositionalList<Entry<String, String>>>>>>> roomIterator = dateNext.getElement().getValue().positions().iterator();

            boolean roomExist = false;
            Position<Entry<Integer, LinkedPositionalList<Entry<String, LinkedPositionalList<Entry<String, String>>>>>> roomNext = null;

            while (roomIterator.hasNext()){
                roomNext = roomIterator.next();

                Iterator<Position<Entry<String, LinkedPositionalList<Entry<String, String>>>>> timeslotIterator = roomNext.getElement().getValue().positions().iterator();
                while (timeslotIterator.hasNext()) {
                    Position<Entry<String, LinkedPositionalList<Entry<String, String>>>> timeslotNext = timeslotIterator.next();

                    // Check if timeslot already has a booking
                    if (timeslotNext.getElement().getValue() != null){
                        Iterator<Position<Entry<String, String>>> timeslotPropertiesIterator = timeslotNext.getElement().getValue().positions().iterator();

                        while (timeslotPropertiesIterator.hasNext()){
                            Position<Entry<String, String>> timeslotPropertiesNext = timeslotPropertiesIterator.next();

                            if (timeslotPropertiesNext.getElement().getKey().equals("bookingId") && timeslotPropertiesNext.getElement().getValue().equals(bookingId)){
                                // Cancel booking
                                timeslotNext.getElement().getValue().set(timeslotPropertiesNext, null);
                                bookingExist = true;
                                status = true;
                            }
                        }
                    }
                }
            }
        }
        if (!bookingExist)
            message = "Booking (" + bookingId + ") does not exist";
        rmiResponse.setMessage(message);
        rmiResponse.setDatetime(new Date());
        rmiResponse.setRequestType(RequestObjectActions.CancelBooking);
        rmiResponse.setRequestParameters("Booking Id: " + bookingId);
        rmiResponse.setStatus(status);
        return rmiResponse;
    }

    private RMIResponse bookRoomOnCampus(String identifier, int roomNumber, Date date, String timeslot) {
        // Check whether an entry exist on date
        RMIResponse rmiResponse = new RMIResponse();
        boolean status = false;
        String message = "Timeslot (" + timeslot + ") has been booked";

        boolean dateExist = false;
        Iterator<Position<Entry<Date, LinkedPositionalList<Entry<Integer, LinkedPositionalList<Entry<String, LinkedPositionalList<Entry<String, String>>>>>>>>> dateIterator = database.positions().iterator();
        while (dateIterator.hasNext()){
            Position<Entry<Date, LinkedPositionalList<Entry<Integer, LinkedPositionalList<Entry<String, LinkedPositionalList<Entry<String, String>>>>>>>> dateNext = dateIterator.next();

            // Check if Date exist
            if (dateNext.getElement().getKey().equals(date)){
                dateExist = true;
                Iterator<Position<Entry<Integer, LinkedPositionalList<Entry<String, LinkedPositionalList<Entry<String, String>>>>>>> roomIterator = dateNext.getElement().getValue().positions().iterator();

                boolean roomExist = false;
                Position<Entry<Integer, LinkedPositionalList<Entry<String, LinkedPositionalList<Entry<String, String>>>>>> roomNext = null;

                while (roomIterator.hasNext()){
                    roomNext = roomIterator.next();

                    // Check if room exist
                    if (roomNext.getElement().getKey().equals(roomNumber)){
                        roomExist = true;

                        boolean timeslotExist = false;

                        Iterator<Position<Entry<String, LinkedPositionalList<Entry<String, String>>>>> timeslotIterator = roomNext.getElement().getValue().positions().iterator();
                        while (timeslotIterator.hasNext()) {
                            Position<Entry<String, LinkedPositionalList<Entry<String, String>>>> timeslotNext = timeslotIterator.next();

                            // Check if timestamp already exist
                            if (timeslotNext.getElement().getKey().equals(timeslot)){
                                timeslotExist = true;

                                // Check if timeslot already has a booking
                                if (timeslotNext.getElement().getValue() == null){
                                    // Create timeslot and add attributes
                                    roomNext.getElement().getValue().set(timeslotNext, new Node<>(timeslot, new LinkedPositionalList<>()));
                                    timeslotNext.getElement().getValue().addFirst(new Node<>("bookingId", UUID.randomUUID().toString()));
                                    timeslotNext.getElement().getValue().addFirst(new Node<>("studentId", identifier));
                                    status = true;
                                } else {
                                    // Already booked
                                    message = "Timeslot (" + timeslot + ") is already booked";
                                }
                            }
                        }

                        if (!timeslotExist)
                            message = "Timeslot (" + timeslot + ") does not exist";
                    }
                }
                // Add room
                if (!roomExist){
                    message = "The room (" + roomNumber + ") does not exist";
                }

            }
        }
        if (!dateExist) {
            // Add timeslots
            message = "There are not rooms available on that date (" + date.toString() + ")";
        }
        rmiResponse.setMessage(message);
        rmiResponse.setDatetime(new Date());
        rmiResponse.setRequestType(RequestObjectActions.BookRoom);
        rmiResponse.setRequestParameters("Identifier: " + identifier + ", Room Number: " + roomNumber + ", Date: " + date.toString() + ", Timeslot: " + timeslot);
        rmiResponse.setStatus(status);
        return rmiResponse;
    }

    private RMIResponse udpTransfer(Campus campus, RequestObject requestObject){
        DatagramSocket datagramSocket = null;
        try {
            int remotePort;
            datagramSocket = new DatagramSocket();
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
            System.out.println("Socket: " + e.getMessage());
        }
        catch (IOException e){
            System.out.println("IO: " + e.getMessage());
        } catch (ParseException e) {
            e.printStackTrace();
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
}
