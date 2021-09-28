package com.roomreservation;

import java.rmi.Remote;
import java.sql.Timestamp;
import java.util.Date;

public interface RoomReservationStudentInterface extends Remote {
    String bookRoom(String campusName, int roomNumber, Date date, Timestamp timeslot) throws java.rmi.RemoteException;
    String getAvailableTimeSlot(Date date) throws java.rmi.RemoteException;
    String cancelBooking(int bookingId) throws java.rmi.RemoteException;
}
