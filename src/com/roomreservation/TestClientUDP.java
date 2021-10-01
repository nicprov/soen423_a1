package com.roomreservation;

import com.roomreservation.protobuf.protos.RequestObject;
import com.roomreservation.protobuf.protos.RequestObjectActions;
import com.roomreservation.protobuf.protos.ResponseObject;

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

            RequestObject.Builder requestObject = RequestObject.newBuilder();
            requestObject.setAction(RequestObjectActions.GetAvailableTimeslots.toString());

            byte[] m = requestObject.build().toByteArray();

            InetAddress host = InetAddress.getLocalHost();

            DatagramPacket request = new DatagramPacket(m, m.length, host, 5001);
            datagramSocket.send(request);

            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            datagramSocket.receive(reply);

            System.out.println("Message: " + ResponseObject.parseFrom(trim(reply.getData())));
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
