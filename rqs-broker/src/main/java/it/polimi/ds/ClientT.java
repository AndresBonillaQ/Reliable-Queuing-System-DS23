package it.polimi.ds;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientT {
    public static void main(String[] args){

        Socket clientSocket;
        PrintWriter out;
        BufferedReader in;
        BufferedReader inConsole;

        try{
            clientSocket = new Socket("127.0.0.1", 8081);
            out = new PrintWriter(clientSocket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            inConsole = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("CONNECTED TO THE SERVER");
        }catch (IOException e){
            System.out.println("ERROR establishing connection");
            return;
        }

        while(true){
            try{

                String lineConsole = inConsole.readLine();
                System.out.println("SENDING STRING: {" + lineConsole + "}");

                out.println(lineConsole);
                out.flush();

                String line = in.readLine();
                System.out.println("RECEIVED LINE: {" + line + "}");

            }catch (IOException e){
                System.out.println("ERROR during readLine");
                return;
            }
        }

    }
}