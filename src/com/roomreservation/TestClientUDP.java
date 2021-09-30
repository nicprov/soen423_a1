package com.roomreservation;

import com.google.protobuf.ByteString;
import com.roomreservation.protobuf.protos.UdpRequest;
import com.roomreservation.protobuf.protos.UdpRequestActions;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

public class TestClientUDP {
    public static void main(String[] args){
        DatagramSocket datagramSocket = null;
        try {
            datagramSocket = new DatagramSocket();

            UdpRequest.Builder udpRequest = UdpRequest.newBuilder();
            udpRequest.setAction(UdpRequestActions.GetAvailableTimeslots.toString());
            udpRequest.setBookingId(123);
            udpRequest.setRoomNumber(1231412341);
            byte[] m = udpRequest.build().toByteArray();

            InetAddress host = InetAddress.getLocalHost();

            DatagramPacket request = new DatagramPacket(m, m.length, host, 5001);
            datagramSocket.send(request);

            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            System.out.println("test");
            datagramSocket.receive(reply);
            
            System.out.println("Action: " + UdpRequest.parseFrom(trim(reply.getData())).getAction());
            System.out.println("Booking ID:" + UdpRequest.parseFrom(trim(reply.getData())).getBookingId());
            System.out.println("Room number: " + UdpRequest.parseFrom(trim(reply.getData())).getRoomNumber());
        }
        catch (SocketException e){
            System.out.println("Socket: " + e.getMessage());
        }
        catch (IOException e){
            System.out.println("IO: " + e.getMessage());
        }
        finally {
            if (datagramSocket != null)
                datagramSocket.close();
        }
    }

    public static byte[] trim(byte[] bytes)
    {
        int length = bytes.length - 1;
        while (length >= 0 && bytes[length] == 0)
            --length;
        return Arrays.copyOf(bytes, length + 1);
    }
}
