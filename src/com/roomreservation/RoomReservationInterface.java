package com.roomreservation;

import com.roomreservation.protobuf.protos.ResponseObject;

import java.rmi.Remote;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

public interface RoomReservationInterface extends Remote {
    /* Admin role */
    RMIResponse createRoom(int roomNumber, Date date, ArrayList<String> listOfTimeSlots) throws java.rmi.RemoteException;
    RMIResponse deleteRoom(int roomNumber, Date date, ArrayList<String> listOfTimeSlots) throws java.rmi.RemoteException;

    /* Student role */
    RMIResponse bookRoom(String identifier, Campus campusName, int roomNumber, Date date, String timeslot) throws java.rmi.RemoteException;
    RMIResponse getAvailableTimeSlot(Date date) throws java.rmi.RemoteException;
    RMIResponse cancelBooking(String bookingId) throws java.rmi.RemoteException;
}