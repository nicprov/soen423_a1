package com.roomreservation;

import com.roomreservation.collection.Entry;
import com.roomreservation.collection.LinkedPositionalList;
import com.roomreservation.collection.Node;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

public class RoomReservation extends UnicastRemoteObject implements RoomReservationInterface {

    private final LinkedPositionalList<Entry<Date, LinkedPositionalList<Entry<Integer, ArrayList<String>>>>> campus;

    protected RoomReservation() throws RemoteException {
        super();
        campus = new LinkedPositionalList<>();
    }


    @Override
    public String createRoom(int roomNumber, Date date, ArrayList<String> listOfTimeSlots) throws RemoteException {
        // Check whether an entry exist on date
        Iterator<Entry<Date, LinkedPositionalList<Entry<Integer, ArrayList<String>>>>> dateIterator = campus.iterator();
        while (dateIterator.hasNext()){
            Entry<Date, LinkedPositionalList<Entry<Integer, ArrayList<String>>>> dateNext = dateIterator.next();

            // Check if Date exist
            if (dateNext.getKey().equals(date)){
               Iterator<Entry<Integer, ArrayList<String>>> roomIterator = dateNext.getValue().iterator();

               boolean roomExist = false;
               Entry<Integer, ArrayList<String>> room = null;

               while (roomIterator.hasNext()){
                   room = roomIterator.next();

                   // Check if room exist
                   if (room.getKey().equals(roomNumber)){
                        roomExist = true;

                        ArrayList<String> timeslots = room.getValue();

                        // Check each timeslot and add missing ones
                        for (String timeslot: listOfTimeSlots){
                            if (!timeslots.contains(timeslot))
                                timeslots.add(timeslot);
                        }
                   }
               }

               if (!roomExist){
                   // Add room
                   LinkedPositionalList<Entry<Integer, ArrayList<String>>> rooms = dateNext.getValue();
                   rooms.addFirst(room);
               }

            }
        }
        LinkedPositionalList<Entry<Integer, ArrayList<String>>> room = new LinkedPositionalList<>();
        room.addFirst(new Node<>(roomNumber, listOfTimeSlots));
        campus.addFirst(new Node<>(date, room));
        return "Room (" + roomNumber + ") has been created.";
    }

    @Override
    public String deleteRoom(int roomNumber, Date date, ArrayList<String> listOfTimeSlots) throws RemoteException {
        // Check whether an entry exist on date
        Iterator<Entry<Date, LinkedPositionalList<Entry<Integer, ArrayList<String>>>>> dateIterator = campus.iterator();
        boolean found = false;
        while (dateIterator.hasNext()){
            Entry<Date, LinkedPositionalList<Entry<Integer, ArrayList<String>>>> dateNext = dateIterator.next();

            // Check if date exist
            if (dateNext.getKey().equals(date)) {
                // Found
                found = true;

                // Check if room exists
                Iterator<Entry<Integer, ArrayList<String>>> roomIterator = dateNext.getValue().iterator();

                boolean roomExist = false;
                Entry<Integer, ArrayList<String>> room = null;

                while (roomIterator.hasNext()) {
                    room = roomIterator.next();

                    // Check if room exist
                    if (room.getKey().equals(roomNumber)) {
                        roomExist = true;

                        ArrayList<String> timeslots = room.getValue();

                        // Check each timeslot and add missing ones
                        for (String timeslot : listOfTimeSlots) {
                            if (timeslots.contains(timeslot))
                                timeslots.remove(timeslot);
                        }
                    }
                }
                if (!roomExist)
                    return "Room (" + roomNumber + ") is not found in database";
            }
        }

        if (!found)
            return "Date (" + date.toString() + ") is not found in database";
        return "Successfully deleted timeslots associated with Room (" + roomNumber + ") on Date (" + date.toString() + ")";
    }

    @Override
    public String bookRoom(String campusName, int roomNumber, Date date, Timestamp timeslot) throws RemoteException {
        return "bookRoom";
    }

    @Override
    public String getAvailableTimeSlot(Date date) {
        return "getAvailableTimeSlot";
    }

    @Override
    public String cancelBooking(int bookingId) {
        return "cancelBooking";
    }
}
