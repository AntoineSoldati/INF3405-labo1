import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ClientHandler extends Thread {
    private Socket socket;
    private int clientNumber;
    private String currentPath = FileSystems.getDefault().getPath(".").toString();

    public ClientHandler(Socket socket, int clientNumber) {
        this.socket = socket;
        this.clientNumber = clientNumber;
        System.out.println("New connection with client#" + clientNumber + " at" + socket);
    }

    public void run() {
        try {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF("Hello from server - you are client#" + clientNumber);
            DataInputStream in = new DataInputStream(socket.getInputStream());

            String input;
            while (true) {
                input = in.readUTF();
                String[] inputArr = input.split("\\s+");
                String command = inputArr[0];
                String arg = "";
                if (inputArr.length > 1)
                    arg = inputArr[1];

                File cwd = new File(this.currentPath);
                System.out.println(command);

                switch (command) {
                    case "cd":
                        System.out.println(getHeader() + " cd " + arg);
                        String unchangedPath = this.currentPath;
                        if (arg.equals("..")) {
                            String[] pathList = this.currentPath.split("/");
                            if (pathList.length > 2) {
                                this.currentPath = "./";
                                for (int i = 1; i < pathList.length - 2; i++) {
                                    this.currentPath += pathList[i] + "/";
                                }
                                this.currentPath += pathList[pathList.length - 2];
                            } else
                                this.currentPath = "./";
                            out.writeUTF("Vous êtes maintenant dans le dossier " + this.currentPath);
                        } else {
                            File[] files = cwd.listFiles();
                            for (File file : files) {
                                if (file.isDirectory() && file.getName().equals(arg)) {
                                    if (this.currentPath.equals("./"))
                                        this.currentPath = this.currentPath + arg;
                                    else
                                        this.currentPath = this.currentPath + "/" + arg;
                                    break;
                                }
                            }
                            if (unchangedPath.equals(this.currentPath))
                                out.writeUTF("Le dossier " + arg + " n'existe pas.");
                            else
                                out.writeUTF("Vous êtes maintenant dans le dossier " + this.currentPath);
                        }
                        break;
                    case "ls":
                        System.out.println(getHeader() + " ls");
                        String returnString = "";
                        File[] files = cwd.listFiles();
                        for (File file : files) {
                            if (file.isFile())
                                returnString += "[File] ";
                            else
                                returnString += "[Folder] ";
                            returnString += file.getName() + "\n";
                        }
                        out.writeUTF(returnString);
                        break;
                    case "mkdir":
                        System.out.println(getHeader() + " mkdir " + arg);
                        File newDirectory = new File(cwd.getAbsolutePath() + "/" + arg);
                        out.writeUTF(newDirectory.mkdir() ? "Le dossier " + arg + " a été créé"
                                : "Erreur lors de la création du dossier "
                                        + arg);
                        break;
                    case "upload":
                        System.out.println(getHeader() + " upload " + arg);
                        File fileToUpload = new File(this.currentPath, arg);
                        if (fileToUpload.exists() && fileToUpload.isFile()) {
                            out.writeUTF("Le fichier " + arg + " existe déjà.");
                            break;
                        }
                        out.writeUTF("200");

                        FileOutputStream outFile = new FileOutputStream(currentPath + "/" + arg);
                        byte[] bytes = new byte[1024 * 8];
                        outFile.write(bytes, 0, in.read(bytes));

                        out.writeUTF("Le fichier " + arg + " a été téléversé avec succès.");
                        outFile.close();
                        break;
                    case "download":
                        System.out.println(getHeader() + " download " + arg);
                        File fileToDownload = new File(this.currentPath, arg);
                        if (!fileToDownload.exists() || !fileToDownload.isFile()) {
                            out.writeUTF("Le fichier " + arg + " n'existe pas.");
                            break;
                        }
                        out.writeUTF("200");

                        FileInputStream streamToUpload = new FileInputStream(fileToDownload);
                        byte[] bytesLeft = new byte[1024 * 8];
                        int read = streamToUpload.read(bytesLeft);
                        out.write(bytesLeft, 0, read);
                        out.writeUTF("Le fichier " + arg + " a été téléchargé avec succès.");
                        break;
                    case "exit":
                        System.out.println(getHeader() + " exit");
                        try {
                            out.writeUTF("Vous avez été déconnecté avec succès.");
                            out.flush();
                            socket.close();
                        } catch (IOException e) {
                            out.writeUTF("Erreur lors de la fermeture de la connexion");
                        }
                        break;
                    default:
                        break;
                }
                out.flush();
            }
        } catch (IOException e) {
            System.out.println("Error handling client# " + clientNumber + ": " + e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Couldn't close a socket, what's going on?");
            }
            System.out.println("Connection with client# " + clientNumber + " closed");
        }
    }

    private String getHeader() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd@HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return "[" + socket.getInetAddress().getHostAddress() + ":" + Integer.toString(socket.getPort()) + " - "
                + dtf.format(now) + "]";
    }
}