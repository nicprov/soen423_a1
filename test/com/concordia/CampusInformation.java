package com.concordia;

import org.junit.jupiter.api.Test;

import static com.roomreservation.CampusInformation.*;
import static org.junit.jupiter.api.Assertions.*;


public class CampusInformation {

    @Test
    public void testDvlPort(){
        assertEquals(5000, dvlPort);
    }

    @Test
    public void testKklPort(){
        assertEquals(5001, kklPort);
    }

    @Test
    public void testWstPort(){
        assertEquals(5002, wstPort);
    }

    @Test
    public void testHost(){
        assertEquals("localhost", host);
    }
}
