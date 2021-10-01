package com.roomreservation.common;

import com.roomreservation.Campus;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.roomreservation.ConsoleColours.*;

public class Parsing {
    public static int tryParseInt(String value){
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e){
            return -1;
        }
    }

    public static Date tryParseDate(String date){
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return new SimpleDateFormat("yyyy-MM-dd").parse(date);
        } catch (ParseException e){
            return null;
        }
    }

    public static ArrayList<String> tryParseTimeslotList(String list){
        //TODO validate that each entry is a valid timeslot
        return new ArrayList<String>(Arrays.asList(list.split("\\s*,\\s*")));
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
}
