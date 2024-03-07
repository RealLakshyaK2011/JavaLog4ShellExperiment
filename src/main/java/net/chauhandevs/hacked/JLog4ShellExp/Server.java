package net.chauhandevs.hacked.JLog4ShellExp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Server {
    public static Logger l = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        ServerSocket listner;
        Socket connection;
        InputStream is;
        OutputStream os;


        String thisMessage;
        String thatMessage;

        try {
            listner = new ServerSocket(4321);
        }catch (Exception e){
            throw new RuntimeException(e);
        }

        while (true) {
            try {
                connection = listner.accept();

                is = connection.getInputStream();
                os = connection.getOutputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                l.error("Receiving message from the client. . .");
                thatMessage = reader.readLine();

                l.error("Message received from client: ");
                l.error(thatMessage);

                l.error("Enter a message to send to the client: ");
                thisMessage = sc.nextLine();
                os.write((thisMessage+"\n").getBytes(StandardCharsets.UTF_8));
                os.flush();
                l.error("Message Sent!");

                connection.close();
            } catch (Exception e) {
                l.error("An Exception Occurred! : ");
                e.printStackTrace();
            }
        }
    }
}
