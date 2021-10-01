package com.roomreservation;

import com.roomreservation.collection.Entry;
import com.roomreservation.collection.LinkedPositionalList;
import com.roomreservation.collection.Node;
import com.roomreservation.collection.Position;
import com.roomreservation.protobuf.protos.UdpRequest;
import com.roomreservation.protobuf.protos.UdpRequestActions;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import static com.roomreservation.CampusInformation.*;

public class RoomReservation extends UnicastRemoteObject implements RoomReservationInterface {

    private final LinkedPositionalList<Entry<Date, LinkedPositionalList<Entry<Integer, LinkedPositionalList<Entry<String, LinkedPositionalList<Entry<String, String>>>>>>>> database;
    private final Campus campus;

    protected RoomReservation(Campus campus) throws RemoteException {
        super();
        this.database = new LinkedPositionalList<>();
        this.campus = campus;
    }

    @Override
    public String createRoom(int roomNumber, Date date, ArrayList<String> listOfTimeSlots) throws RemoteException {
        // Check whether an entry exist on date
        var message = "Date (" + date.toString() + ") entry has been created";

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
        return message;
    }

    @Override
    public String deleteRoom(int roomNumber, Date date, ArrayList<String> listOfTimeSlots) throws RemoteException {
        // Check whether an entry exist on date
        var message = "Successfully deleted timeslots associated with Room (\" + roomNumber + \") on Date (\" + date.toString() + \")";
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
                }

                // Delete date
                database.remove(dateNext);
                message = "Date (" + date.toString() + ") has been removed";
            }
        }
        if (!dateExist) {
            message = "Date (" + date.toString() + ") is not found in database";
        }
        return message;
    }

    @Override
    public String bookRoom(String identifier, Campus campus, int roomNumber, Date date, String timeslot) throws RemoteException {
        if (campus.equals(this.campus))
            return bookRoomOnCampus(identifier, roomNumber, date, timeslot);
        else {
            // Perform action on remote server
            UdpRequest.Builder udpRequest = UdpRequest.newBuilder();
            udpRequest.setAction(UdpRequestActions.BookRoom.toString());
            udpRequest.setRoomNumber(roomNumber);
            udpRequest.setDate(date.toString());
            udpRequest.setTimeslot(timeslot);
            return udpTransfer(campus, udpRequest.build());
        }
    }

    @Override
    public String getAvailableTimeSlot(Date date) {
        // Build new proto request object
        UdpRequest.Builder udpRequest = UdpRequest.newBuilder();
        udpRequest.setAction(UdpRequestActions.GetAvailableTimeslots.toString());
        udpRequest.setDate(date.toString());
        String dvlTimeslots = udpTransfer(Campus.DVL, udpRequest.build());
        String kklTimeslots = udpTransfer(Campus.KKL, udpRequest.build());
        String wstTimeslots = udpTransfer(Campus.WST, udpRequest.build());
        return "DVL " + dvlTimeslots + ", KKL " + kklTimeslots + ", WST " + wstTimeslots;
    }

    @Override
    public String cancelBooking(String bookingId) {
        // Check whether an entry exist on date
        var message = "Booking (" + bookingId + ") has been cancelled";

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
                            }
                        }
                    }
                }
            }
        }
        if (!bookingExist)
            message = "Booking (" + bookingId + ") does not exist";
        return message;
    }

    private String bookRoomOnCampus(String identifier, int roomNumber, Date date, String timeslot) {
        // Check whether an entry exist on date
        var message = "Timeslot (" + timeslot + ") has been booked";

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
        return message;
    }

    private String udpTransfer(Campus campus, UdpRequest udpRequest){
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
            DatagramPacket request = new DatagramPacket(udpRequest.toByteArray(), udpRequest.toByteArray().length, host, remotePort);
            datagramSocket.send(request);

            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            datagramSocket.receive(reply);

            // TODO: Create UdpReponse object
            return UdpRequest.parseFrom(trim(reply.getData())).getAction();
        }
        catch (SocketException e){
            System.out.println("Socket: " + e.getMessage());
        }
        catch (IOException e){
            System.out.println("IO: " + e.getMessage());
        }
        finally {
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
