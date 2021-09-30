package com.concordia;

import org.junit.jupiter.api.Test;

import static com.roomreservation.ConsoleColours.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConcoleColours {

    @Test
    public void testAnsiRed(){
        assertEquals("\u001B[31m", ANSI_RED);
    }

    @Test
    public void testAnsiGreen(){
        assertEquals("\u001B[32m", ANSI_GREEN);
    }

    @Test
    public void testReset(){
        assertEquals("\u001B[0m", RESET);
    }
}
