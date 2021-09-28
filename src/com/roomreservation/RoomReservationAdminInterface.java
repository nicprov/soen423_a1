package com.roomreservation;

import java.rmi.Remote;
import java.sql.Timestamp;
import java.util.Date;

public interface RoomReservationAdminInterface extends Remote {
    String createRoom(int roomNumber, Date date, Timestamp[] listOfTimeSlots) throws java.rmi.RemoteException;
    String deleteRoom(int roomNumber, Date date, Timestamp[] listOfTimeSlots) throws java.rmi.RemoteException;
}
