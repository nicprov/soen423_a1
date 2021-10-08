package com.roomreservation.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.roomreservation.common.ConsoleColours.ANSI_RED;
import static com.roomreservation.common.ConsoleColours.RESET;

public class Parsing {

    public static ArrayList<String> getTimeslots(BufferedReader bufferedReader) throws IOException {
        System.out.print("Enter a list of timeslots (ie. 9:30-10:00, 11:15-11:30): ");
        ArrayList<String> timeslots = Parsing.tryParseTimeslotList(bufferedReader.readLine());
        while (timeslots == null){
            System.out.print(ANSI_RED + "Invalid timeslots provided, must be in the following format (ie. 9:30-10:00, 11:15-11:30): " + RESET);
            timeslots = Parsing.tryParseTimeslotList(bufferedReader.readLine());
        }
        return timeslots;
    }

    public static String getTimeslot(BufferedReader bufferedReader) throws IOException {
        System.out.print("Enter a timeslots (ie. 9:30-10:00): ");
        String timeslot = Parsing.tryParseTimeslot(bufferedReader.readLine());
        while (timeslot == null){
            System.out.print(ANSI_RED + "Invalid timeslot provided, must be in the following format (ie. 9:30-10:00): " + RESET);
            timeslot = Parsing.tryParseTimeslot(bufferedReader.readLine());
        }
        return timeslot;
    }

    public static Date getDate(BufferedReader bufferedReader) throws IOException {
        System.out.print("Enter date (ie. 2021-01-01): ");
        Date date = Parsing.tryParseDate(bufferedReader.readLine());
        while (date == null){
            System.out.print(ANSI_RED + "Invalid date format (ie. 2021-01-01): " + RESET);
            date = Parsing.tryParseDate(bufferedReader.readLine());
        }
        return date;
    }

    public static int getRoomNumber(BufferedReader bufferedReader) throws IOException {
        System.out.print("Enter room number  (ie. 201): ");
        int roomNumber = Parsing.tryParseInt(bufferedReader.readLine());
        while (roomNumber == -1){
            System.out.print(ANSI_RED + "Invalid room number, must be an integer (ie. 201): " + RESET);
            roomNumber = Parsing.tryParseInt(bufferedReader.readLine());
        }
        return roomNumber;
    }

    public static Campus getCampus(BufferedReader bufferedReader) throws IOException {
        System.out.print("Enter campus name (dvl, kkl, wst): ");
        Campus campus = Parsing.tryParseCampus(bufferedReader.readLine());
        while (campus == null){
            System.out.print(ANSI_RED + "Invalid campus, must be one of the following (dvl, kkl, wst): " + RESET);
            campus = Parsing.tryParseCampus(bufferedReader.readLine());
        }
        return campus;
    }

    public static String getBookingId(BufferedReader bufferedReader) throws IOException {
        System.out.print("Enter booking ID: ");
        String bookingID = Parsing.tryParseBookingId(bufferedReader.readLine());
        while (bookingID == null){
            System.out.print(ANSI_RED + "Invalid booking ID, must be a valid UUID (ie. KKL:ce612356-db1f-4523-8c8b-c35bff35ebd0): " + RESET);
            bookingID = Parsing.tryParseUUID(bufferedReader.readLine());
        }
        return bookingID;
    }

    public static int tryParseInt(String value){
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e){
            return -1;
        }
    }

    public static Date tryParseDate(String date){
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(date);
        } catch (ParseException e){
            return null;
        }
    }

    public static ArrayList<String> tryParseTimeslotList(String list){
        List<String> tempList = Arrays.asList(list.split("\\s*,\\s*"));
        if (tempList.size() == 0)
            return null;
        for (String timeslot: tempList){
            if (Parsing.tryParseTimeslot(timeslot) == null)
                return null;
        }
        return new ArrayList<>(tempList);
    }

    public static String tryParseTimeslot(String timeslot){
        Pattern pattern = Pattern.compile("[0-9]{1,2}:[0-9]{2}-[0-9]{1,2}:[0-9]{2}$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(timeslot);
        if (matcher.find())
            return timeslot;
        return null;
    }

    public static Campus tryParseCampus(String campus){
        Pattern pattern = Pattern.compile("(dvl|kkl|wst)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(campus);
        if (matcher.find()) {
            switch (campus.toLowerCase()){
                case "dvl":
                    return Campus.DVL;
                case "kkl":
                    return Campus.KKL;
                case "wst":
                default:
                    return Campus.WST;
            }
        }
        return null;
    }

    public static String tryParseBookingId(String bookingId){
        try{
            String campus = bookingId.split(":")[0];
            String uuid = bookingId.split(":")[1];
            if (tryParseCampus(campus) != null && tryParseUUID(uuid) != null)
                return bookingId;
            return null;
        } catch (Exception e){
            return null;
        }
    }
    public static String tryParseUUID(String uuid){
        try {
            return UUID.fromString(uuid).toString();
        } catch (IllegalArgumentException e){
            return null;
        }
    }
}
