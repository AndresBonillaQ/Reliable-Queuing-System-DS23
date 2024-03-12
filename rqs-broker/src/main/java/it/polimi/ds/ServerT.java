package it.polimi.ds;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerT {
    public static void main(String[] args){

        ServerSocket serverSocket;
        Socket clientSocket;
        PrintWriter out;
        BufferedReader in;

        try{
            serverSocket = new ServerSocket(8081);
            System.out.println("SERVER STARTED");
        }catch (IOException e){
            System.out.println("ERROR during opening server");
            return;
        }

        try{
            clientSocket = serverSocket.accept();
            out = new PrintWriter(clientSocket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            System.out.println("CLIENT CONNECTED");
        }catch (IOException e){
            System.out.println("ERROR establishing connection");
            return;
        }

        while(true){
            try{
                String line = in.readLine();
                System.out.println("RECEIVED LINE: {" + line + "}");

                String response = "RESPONSE: " + line;
                System.out.println("SENDING: {" + response + "}");

                out.println("RESPONSE: " + line);
                out.flush();

            }catch (IOException e){
                System.out.println("ERROR during readLine");
                return;
            }
        }

    }
}