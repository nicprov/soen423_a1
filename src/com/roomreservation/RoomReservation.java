package com.roomreservation;

import com.roomreservation.collection.Entry;
import com.roomreservation.collection.LinkedPositionalList;
import com.roomreservation.collection.Node;
import com.roomreservation.collection.Position;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class RoomReservation extends UnicastRemoteObject implements RoomReservationInterface {

    private final LinkedPositionalList<Entry<Date, LinkedPositionalList<Entry<Integer, LinkedPositionalList<Entry<String, LinkedPositionalList<Entry<String, String>>>>>>>> database;
    private final String campus;

    protected RoomReservation(String campus) throws RemoteException {
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
