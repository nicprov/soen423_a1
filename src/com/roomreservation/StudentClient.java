package com.roomreservation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.roomreservation.CampusInformation.*;
import static com.roomreservation.ConsoleColours.*;

public class StudentClient {
    private static String identifier;

    public static void main(String[] args) {
        InputStreamReader is = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(is);
        try {
            String registryURL;
            identifier = getIdentifier(bufferedReader);
            if (identifier.toLowerCase().startsWith("dvl")){
                // Connect to Dorval-Campus (DVL)
                registryURL = "rmi://" + host + ":" + dvlRMIPort + "/server";
            } else if (identifier.toLowerCase().startsWith("kkl")) {
                // Connect to Kirkland-Campus (KKL)
                registryURL = "rmi://" + host + ":" + kklRMIPort + "/server";
            } else {
                // Connect to Westmount-Campus (WST)
                registryURL = "rmi://" + host + ":" + wstRMIPort + "/server";
            }
            System.out.println("Lookup completed");
            RoomReservationInterface roomReservation = (RoomReservationInterface) Naming.lookup(registryURL);
            startStudent(roomReservation, bufferedReader);
        } catch (ConnectException e) {
            System.out.println("Unable to connect to remote server, host may be down. Please try again later!");
        } catch (Exception e){
            System.out.println("Unable to start client: " + e);
        }
    }

    /**
     * Gets and validates unique identifier using regex. Identifier must contain the campus (dvl, kkl, wst)
     * followed by the user type (a for admin or s for student) followed by exactly four digits.
     * @param br BufferedReader for console output
     * @return Validated unique identifier
     * @throws IOException
     */
    private static String getIdentifier(BufferedReader br) throws IOException {
        System.out.printf("Enter unique identifier: ");
        String identifier = br.readLine().trim();
        Pattern pattern = Pattern.compile("(dvl|kkl|wst)(s)[0-9]{4}$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(identifier);
        while (!matcher.find()){
            System.out.printf(ANSI_RED + "Invalid identifier! Please enter your unique identifier: ");
            identifier = br.readLine().trim();
            matcher = pattern.matcher(identifier);
        }
        System.out.println(ANSI_GREEN + "Valid identifier" + RESET);
        return identifier;
    }

    /**
     * List possible actions based on identifierType (either student or admin) and prompts
     * user to select an action from his specific user role
     */
    private static String listAndGetActions(BufferedReader bufferedReader) throws IOException {
        String action = "";
        System.out.println("\n==============================");
        System.out.println("Student section");
        System.out.println("==============================");
        System.out.println("Select an action from the list below:");
        System.out.println("1. Book room");
        System.out.println("2. Get available time slots");
        System.out.println("3. Cancel booking");
        System.out.println("4. Quit");
        System.out.printf("Selection: ");
        action = bufferedReader.readLine().trim();
        while (!action.equals("1") && !action.equals("2") && !action.equals("3") && !action.equals("4")){
            System.out.println(ANSI_RED + "Invalid selection! Must select a valid action (1, 2, 3, 4): " + RESET);
            action = bufferedReader.readLine().trim();
        }
        return action;
    }

    /**
     * Start student processing
     * @param roomReservation
     * @param bufferedReader
     * @throws IOException
     */
    private static void startStudent(RoomReservationInterface roomReservation, BufferedReader bufferedReader) throws IOException {
        while (true) {
            String action = listAndGetActions(bufferedReader);
            switch (action){
                case "1":
                    bookRoom(roomReservation, bufferedReader);
                    break;
                case "2":
                    getAvailableTimeSlots(roomReservation, bufferedReader);
                    break;
                case "3":
                    cancelBooking(roomReservation, bufferedReader);
                    break;
                case "4":
                default:
                    System.out.println("Goodbye!");
                    System.exit(0);
                    break;
            }
        }
    }

    private static void bookRoom(RoomReservationInterface roomReservation, BufferedReader bufferedReader){
        Campus campusName = Campus.DVL;
        int roomNumber = 0;
        Date date = new Date();
        String timeslot = "";

        System.out.println("\nBOOK ROOM");
        System.out.println("-----------");
        System.out.printf("Enter campus name (dvl, kkl, wst): ");
        try {
            switch (bufferedReader.readLine()){
                case "dvl":
                    campusName = Campus.DVL;
                    break;
                case "kkl":
                    campusName = Campus.KKL;
                    break;
                case "wst":
                default:
                    campusName = Campus.WST;
                    break;

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.printf("Enter room number  (ie. 201): ");
        try {
            roomNumber = Integer.parseInt(bufferedReader.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.printf("Enter date (ie. 2021-01-01): ");
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            date = dateFormat.parse(bufferedReader.readLine());
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.printf("Enter a timeslot (ie. 9:30-10:00): ");
        try {
            timeslot = bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            RMIResponse response = roomReservation.bookRoom(identifier, campusName, roomNumber, date, timeslot);
            System.out.println(response.getMessage());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private static void getAvailableTimeSlots(RoomReservationInterface roomReservation, BufferedReader bufferedReader){
        System.out.println("\nGET AVAILABLE TIME SLOTS");
        System.out.println("-----------");
        try {
            Date date = new Date();
            System.out.print("Enter date (ie. 2021-01-01): ");
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            date = dateFormat.parse(bufferedReader.readLine());
            RMIResponse response = roomReservation.getAvailableTimeSlot(date);
            System.out.println(response.getMessage());
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
    }

    private static void cancelBooking(RoomReservationInterface roomReservation, BufferedReader bufferedReader){
        System.out.println("\nCANCEL BOOKING");
        System.out.println("-----------");
        try {
            System.out.print("Enter booking ID: ");
            String bookingId = bufferedReader.readLine();
            RMIResponse response = roomReservation.cancelBooking(bookingId);
            System.out.println(response.getMessage());
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
