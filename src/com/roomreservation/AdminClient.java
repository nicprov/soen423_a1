package com.roomreservation;

import com.roomreservation.common.Parsing;

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

public class AdminClient {
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
            startAdmin(roomReservation, bufferedReader);
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
        Pattern pattern = Pattern.compile("(dvl|kkl|wst)(a)[0-9]{4}$", Pattern.CASE_INSENSITIVE);
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
        System.out.println("Administration section");
        System.out.println("==============================");
        System.out.println("Select an action from the list below:");
        System.out.println("1. Create room");
        System.out.println("2. Delete room");
        System.out.println("3. Quit");
        System.out.printf("Selection: ");
        action = bufferedReader.readLine().trim();
        while (!action.equals("1") && !action.equals("2") && !action.equals("3")){
            System.out.println(ANSI_RED + "Invalid selection! Must select a valid action (1, 2, 3): " + RESET);
            action = bufferedReader.readLine().trim();
        }
        return action;
    }

    /**
     * Start admin action processing
     * @param roomReservation
     * @param bufferedReader
     * @throws IOException
     */
    private static void startAdmin(RoomReservationInterface roomReservation, BufferedReader bufferedReader) throws IOException {
        while (true) {
            String action = listAndGetActions(bufferedReader);
            switch (action){
                case "1":
                    createRoom(roomReservation, bufferedReader);
                    break;
                case "2":
                    deleteRoom(roomReservation, bufferedReader);
                    break;
                case "3":
                default:
                    System.out.println("Goodbye!");
                    System.exit(0);
                    break;
            }
        }
    }

    private static void createRoom(RoomReservationInterface roomReservation, BufferedReader bufferedReader) throws RemoteException {

        System.out.println("\nCREATE ROOM");
        System.out.println("-----------");
        System.out.print("Enter room number  (ie. 201): ");
        try {
            int roomNumber = Parsing.tryParseInt(bufferedReader.readLine());
            while (roomNumber == -1){
                System.out.print(ANSI_RED + "Invalid room number provided, must be an integer (ie. 201): " + RESET);
                roomNumber = Parsing.tryParseInt(bufferedReader.readLine());
            }

            System.out.print("Enter date (ie. 2021-01-01): ");
            Date date = Parsing.tryParseDate(bufferedReader.readLine());
            while (date == null){
                System.out.print(ANSI_RED + "Invalid date provided, must be in the following format (ie. 2021-01-01): " + RESET);
                date = Parsing.tryParseDate(bufferedReader.readLine());
            }

            System.out.print("Enter a list of timeslots (ie. 9:30-10:00, 11:15-11:30): ");
            ArrayList<String> timeslots = Parsing.tryParseTimeslotList(bufferedReader.readLine());
            while (timeslots == null){
                System.out.print(ANSI_RED + "Invalid timeslots provided, must be in the following format (ie. 9:30-10:00, 11:15-11:30): " + RESET);
                timeslots = Parsing.tryParseTimeslotList(bufferedReader.readLine());
            }
            RMIResponse response = roomReservation.createRoom(roomNumber, date, timeslots);
            System.out.println(response.getMessage());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void deleteRoom(RoomReservationInterface roomReservation, BufferedReader bufferedReader) throws RemoteException {
        int roomNumber = 0;
        Date date = new Date();
        ArrayList<String> timeslots = new ArrayList<>();

        System.out.println("\nDELETE ROOM");
        System.out.println("-----------");
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
        System.out.printf("Enter a list of timeslots (ie. 9:30-10:00, 11:15-11:30): ");
        try {
            timeslots = new ArrayList<String>(Arrays.asList(bufferedReader.readLine().split("\\s*,\\s*")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        RMIResponse response = roomReservation.deleteRoom(roomNumber, date, timeslots);
        System.out.println(response.getMessage());
    }
}
