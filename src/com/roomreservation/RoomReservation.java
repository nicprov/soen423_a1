package com.roomreservation;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Timestamp;
import java.util.Date;

public class RoomReservation extends UnicastRemoteObject implements RoomReservationInterface {

    protected RoomReservation() throws RemoteException {
        super();
    }

    @Override
    public String createRoom(int roomNumber, Date date, Timestamp[] listOfTimeSlots) throws RemoteException {
        return "createRoom";
    }

    @Override
    public String deleteRoom(int roomNumber, Date date, Timestamp[] listOfTimeSlots) throws RemoteException {
        return "deleteRoom";
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
