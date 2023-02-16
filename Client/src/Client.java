import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.FileSystems;
import java.util.regex.Pattern;

public class Client {
    private static Socket socket;

    public static void main(String[] args) throws Exception {
        String serverAddress;
        Integer port;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        do {
            System.out.println("Enter the server's IP address");
            serverAddress = reader.readLine();
            System.out.println("Enter the server's port");
            try {
                port = Integer.parseInt(reader.readLine());
            } catch (Exception erreur) {
                port = 0;
            }
        } while (!verifyIP(serverAddress) || !verifyPort(port));

        try {
            socket = new Socket(serverAddress, port);
        } catch (Exception erreur) {
            System.out.println("Oupsi, connection to server failed");
            System.exit(0);
        }

        System.out.format("Serveur lancé sur [%s:%d]", serverAddress, port);
        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        String helloMessageFromServer = in.readUTF();
        System.out.println(helloMessageFromServer);
        String command = "";

        while (!(command.equals("exit"))) {
            String input = reader.readLine();

            String[] inputArr = input.split("\\s+");
            command = inputArr[0];
            String arg = "";
            if (inputArr.length > 1)
                arg = inputArr[1];

            switch (command) {
                case "cd":
                    out.writeUTF(input);
                    System.out.println(in.readUTF());
                    break;
                case "ls":
                    out.writeUTF(input);
                    System.out.println(in.readUTF());
                    break;
                case "mkdir":
                    out.writeUTF(input);
                    System.out.println(in.readUTF());
                    break;
                case "upload":
                    File file = new File(arg);
                    if (!file.exists()) {
                        System.out.println("Ce fichier n'existe pas sur votre machine");
                        break;
                    }
                    out.writeUTF(command + " " + file.getName());
                    if (!in.readUTF().equals("200")) {
                        System.out.println("Ce fichier existe déjà sur le serveur");
                        break;
                    }
                    InputStream streamToUpload = new FileInputStream(file);

                    byte[] bytesLeft = new byte[1024 * 8];
                    int currentCount = streamToUpload.read(bytesLeft);
                    out.write(bytesLeft, 0, currentCount);
                    System.out.println(in.readUTF());
                    streamToUpload.close();
                    break;
                case "download":
                    file = new File(arg);
                    if (file.exists()) {
                        System.out.println("Ce fichier existe déjà sur votre machine");
                        break;
                    }
                    out.writeUTF(command + " " + file.getName());
                    String response = in.readUTF();
                    if (!response.equals("200")) {
                        System.out.println(response);
                        break;
                    }
                    FileOutputStream outFile = new FileOutputStream(arg);
                    byte[] bytes = new byte[1024 * 8];
                    outFile.write(bytes, 0, in.read(bytes));
                    System.out.println(in.readUTF());
                    outFile.close();
                    break;
                case "exit":
                    out.writeUTF(command);
                    System.out.println(in.readUTF());
                    break;
                default:
                    break;
            }
        }
        socket.close();
    }

    public static boolean verifyIP(String IP) {
        String regex = "(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])"; // Source
                                                                                                                                       // :
                                                                                                                                       // https://www.geeksforgeeks.org/how-to-validate-an-ip-address-using-regex/
        Pattern p = Pattern.compile(regex);
        return p.matcher(IP).matches();
    }

    public static boolean verifyPort(int port) {
        return 5000 <= port && 5050 >= port;
    }
}