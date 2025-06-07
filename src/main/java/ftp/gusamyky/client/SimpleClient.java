package ftp.gusamyky.client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class SimpleClient {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 2121;
        try (Socket socket = new Socket(host, port);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                Scanner scanner = new Scanner(System.in)) {

            // Odczytaj powitanie serwera
            String welcome = reader.readLine();
            System.out.println(welcome);

            while (true) {
                System.out.print("> ");
                String cmd = scanner.nextLine();
                if (cmd.equalsIgnoreCase("exit"))
                    break;
                writer.write(cmd + "\n");
                writer.flush();
                String response = reader.readLine();
                System.out.println(response);
            }
        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }
}