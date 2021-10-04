package com.concordia.common;

import com.roomreservation.common.Campus;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import static com.roomreservation.common.Parsing.*;
import static org.junit.jupiter.api.Assertions.*;

public class TestParsing {

    @Test
    public void testTryParseInt(){
        assertEquals(-1,tryParseInt("a"));
        assertEquals(13, tryParseInt("13"));
    }

    @Test
    public void testTryParseDate(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        assertNull(tryParseDate("2020/0as"));
        Date date = tryParseDate("2020-01-01");
        assertEquals(calendar.getTime().getYear(), date.getYear());
        assertEquals(calendar.getTime().getMonth(), date.getMonth());
        assertEquals(calendar.getTime().getDay(), date.getDay());
    }

    @Test
    public void testTryParseTimeslotList(){
        ArrayList<String> tempList = new ArrayList<>();
        tempList.add("9:30-10:00");
        assertEquals(null, tryParseTimeslotList("asd"));
        tempList.add("11:00-11:30");
        assertEquals(tempList, tryParseTimeslotList("9:30-10:00,11:00-11:30"));
    }

    @Test
    public void testTryParseTimeslot(){
        assertNull(tryParseTimeslot("9:00"));
        assertEquals("9:30-10:00", tryParseTimeslot("9:30-10:00"));
    }

    @Test
    public void testTryParseCampus(){
        assertNull(tryParseCampus("abs"));
        assertEquals(Campus.DVL, tryParseCampus("dvl"));
        assertEquals(Campus.WST, tryParseCampus("wst"));
        assertEquals(Campus.KKL, tryParseCampus("kkl"));
    }

    @Test
    public void testTryParseUUID(){
        String uuid = UUID.randomUUID().toString();
        assertNull(tryParseUUID("abc"));
        assertEquals(uuid, tryParseUUID(uuid));
    }
}
