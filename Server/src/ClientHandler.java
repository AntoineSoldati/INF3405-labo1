import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClientHandler extends Thread {
	private Socket socket; 
	private int clientNumber; 
	public ClientHandler(Socket socket, int clientNumber) {
		this.socket = socket;
		this.clientNumber = clientNumber; System.out.println("New connection with client#" + clientNumber + " at" + socket);
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
					if(inputArr.length > 1)
						arg = inputArr[1];
					
					String directoryFile = FileSystems.getDefault().getPath(".").toString();
					File cwd = new File(directoryFile);
					
					switch (command) {
						case "cd" :
							break;
						case "ls" :
							System.out.println("Commande ls à traiter");
							String returnString = "";
							File[] files = cwd.listFiles();
							for(File file : files) {
								if(file.isFile())
									returnString += "[File] ";
								else
									returnString += "[Folder] ";
								returnString += file.getName() + "\n";
							}
							out.writeUTF(returnString);
							break;
						case "mkdir" : 
							System.out.println("Commande mkdir à traiter avec répertoire " + arg);
							File newDirectory = new File(cwd.getAbsolutePath() + "/" + arg);
							out.writeUTF(newDirectory.mkdir() ? 
									"Le dossier " + arg + " a été créé" : 
									"Erreur lors de la création du dossier " 
									+ arg);
							break;
						case "upload" : 
							out.writeUTF(inputArr[1]);
							break;
						case "download" :
							out.writeUTF(inputArr[1]);
							break;
						case "exit" :
							System.out.println("Commande exit à traiter");
							try {
								out.writeUTF("Vous avez été déconnecté avec succès.");
								out.flush();
								socket.close();						
							} catch (IOException e) {
								out.writeUTF("Erreur lors de la fermeture de la connexion");
							}
							break;
						 default :
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
					System.out.println("Couldn't close a socket, what's going on?");}
				System.out.println("Connection with client# " + clientNumber+ " closed");}
		}			
	}