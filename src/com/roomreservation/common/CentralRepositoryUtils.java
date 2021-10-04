package com.roomreservation.common;

import com.roomreservation.protobuf.protos.CentralRepository;
import com.roomreservation.protobuf.protos.CentralRepositoryAction;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import static com.roomreservation.common.ConsoleColours.ANSI_RED;
import static com.roomreservation.common.ConsoleColours.RESET;

public class CentralRepositoryUtils {
    public static byte[] trim(DatagramPacket packet) {
        byte[] data = new byte[packet.getLength()];
        System.arraycopy(packet.getData(), packet.getOffset(), data, 0, packet.getLength());
        return data;
    }

    public static CentralRepository lookupServer(String campus, String type){
        CentralRepository.Builder centralRepositoryRequest = CentralRepository.newBuilder();
        centralRepositoryRequest.setAction(CentralRepositoryAction.Lookup.toString());
        centralRepositoryRequest.setType(type);
        centralRepositoryRequest.setCampus(campus);
        return udpTransfer(centralRepositoryRequest.build());
    }

    public static CentralRepository udpTransfer(CentralRepository centralRepositoryRequest){
        DatagramSocket datagramSocket = null;
        try {
            int remotePort = 1024;
            datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(1000); // Set timeout
            InetAddress host = InetAddress.getLocalHost();
            DatagramPacket request = new DatagramPacket(centralRepositoryRequest.toByteArray(), centralRepositoryRequest.toByteArray().length, host, remotePort);
            datagramSocket.send(request);
            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            datagramSocket.receive(reply);
            return CentralRepository.parseFrom(trim(reply));
        }
        catch (SocketException e){
            System.out.println(ANSI_RED + "Socket: " + e.getMessage() + RESET);
        } catch (IOException e){
            System.out.println(ANSI_RED + "IO: " + e.getMessage() + RESET);
        } finally {
            if (datagramSocket != null)
                datagramSocket.close();
        }
        return null;
    }
}
