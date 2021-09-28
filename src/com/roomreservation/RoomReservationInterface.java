package com.roomreservation;

import java.rmi.Remote;
import java.sql.Timestamp;
import java.util.Date;

public interface RoomReservationInterface extends Remote {
    /* Admin roles */
    String createRoom(int roomNumber, Date date, Timestamp[] listOfTimeSlots) throws java.rmi.RemoteException;
    String deleteRoom(int roomNumber, Date date, Timestamp[] listOfTimeSlots) throws java.rmi.RemoteException;

    /* Student roles */
    String bookRoom(String campusName, int roomNumber, Date date, Timestamp timeslot) throws java.rmi.RemoteException;
    String getAvailableTimeSlot(Date date) throws java.rmi.RemoteException;
    String cancelBooking(int bookingId) throws java.rmi.RemoteException;
}
