package com.roomreservation;

import java.util.Date;

public class RoomRecord {
    private Date date;
    private int roomNumber;
    private int[] availableTimes;
    private String bookedBy;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    public int[] getAvailableTimes() {
        return availableTimes;
    }

    public void setAvailableTimes(int[] availableTimes) {
        this.availableTimes = availableTimes;
    }

    public String getBookedBy() {
        return bookedBy;
    }

    public void setBookedBy(String bookedBy) {
        this.bookedBy = bookedBy;
    }
}
