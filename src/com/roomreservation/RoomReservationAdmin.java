package com.roomreservation;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Timestamp;
import java.util.Date;

public class RoomReservationAdmin extends UnicastRemoteObject implements RoomReservationAdminInterface {

    protected RoomReservationAdmin() throws RemoteException {
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
}
