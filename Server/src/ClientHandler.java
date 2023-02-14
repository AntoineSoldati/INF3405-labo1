import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
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
							System.out.println(getHeader() + " cd " + arg);
							break;
						case "ls" :
							System.out.println(getHeader() + " ls");
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
							System.out.println(getHeader() + " mkdir " + arg);
							File newDirectory = new File(cwd.getAbsolutePath() + "/" + arg);
							out.writeUTF(newDirectory.mkdir() ? 
									"Le dossier " + arg + " a été créé" : 
									"Erreur lors de la création du dossier " 
									+ arg);
							break;
						case "upload" : 
							System.out.println(getHeader() + " upload " + arg);
							out.writeUTF(inputArr[1]);
							break;
						case "download" :
							System.out.println(getHeader() + " download " + arg);
							out.writeUTF(inputArr[1]);
							break;
						case "exit" :
							System.out.println(getHeader() + " exit");
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
		
		private String getHeader() {
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd@HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();
			return "[" + socket.getInetAddress().getHostAddress() + ":" + Integer.toString(socket.getPort()) + " - " + dtf.format(now) + "]";
		}
	}