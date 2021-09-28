package com.roomreservation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.roomreservation.ConsoleColours.*;
import static com.roomreservation.CampusInformation.*;

public class Client {

    public static final int adminServerPort = 5000;
    public static final int studentServerPort = 6000;

    public static void main(String[] args) {
        InputStreamReader is = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(is);
        try {
            String registryURL;
            String identifier = getIdentifier(bufferedReader);
            String identifierType = getIdentifierType(identifier);
            if (identifier.toLowerCase().startsWith("dvl")){
                // Connect to Dorval-Campus (DVL)
                registryURL = "rmi://" + host + ":" + dvlPort + "/server";
            } else if (identifier.toLowerCase().startsWith("kkl")) {
                // Connect to Kirkland-Campus (KKL)
                registryURL = "rmi://" + host + ":" + kklPort + "/server";
            } else {
                // Connect to Westmount-Campus (WST)
                registryURL = "rmi://" + host + ":" + wstPort + "/server";
            }
            System.out.println("Lookup completed");
            RoomReservationInterface roomReservation = (RoomReservationInterface) Naming.lookup(registryURL);
            if (identifierType.equals("admin"))
                startAdmin(roomReservation, bufferedReader);
            else
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
        Pattern pattern = Pattern.compile("(dvl|kkl|wst)(a|s)[0-9]{4}$", Pattern.CASE_INSENSITIVE);
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
     * Gets identifier type (either student or administrator) based
     * on 4th char in identifier.
     * @param identifier Validated identifier
     * @return 'admin' or 'student'
     */
    private static String getIdentifierType(String identifier) {
        if (identifier.charAt(3) == 'a')
            return "admin";
        return "student";
    }

    /**
     * List possible actions based on identifierType (either student or admin) and prompts
     * user to select an action from his specific user role
     * @param identifierType either "student" or "admin"
     */
    private static String listAndGetActions(String identifierType, BufferedReader bufferedReader) throws IOException {
        String action = "";
        if (identifierType.equals("admin")){
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
        } else {
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
            String action = listAndGetActions("admin", bufferedReader);
            switch (action){
                case "1":
                    System.out.println("Create room");
                    break;
                case "2":
                    System.out.println("Delete room");
                    break;
                default:
                    System.out.println("Goodbye!");
                    System.exit(0);
                    break;
            }
        }
    }

    /**
     * Start student processing
     * @param roomReservation
     * @param bufferedReader
     * @throws IOException
     */
    private static void startStudent(RoomReservationInterface roomReservation, BufferedReader bufferedReader) throws IOException {
        while (true) {
            String action = listAndGetActions("student", bufferedReader);
            switch (action){
                case "1":
                    System.out.println("Book room");
                    break;
                case "2":
                    System.out.println("Get available time slots");
                    break;
                case "3":
                    System.out.println("Cancel booking");
                    break;
                default:
                    System.out.println("Goodbye!");
                    System.exit(0);
                    break;
            }
        }

    }
}
