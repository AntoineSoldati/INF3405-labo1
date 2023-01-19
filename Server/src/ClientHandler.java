import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

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
					out.writeUTF(executeCommand(input));
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
		
		private String executeCommand(String input) {
			String[] inputArr = input.split("\\s+");
			String returnString = "";
			switch (inputArr[0]) {
				case "cd" :
					returnString = inputArr[1];
					break;
				case "ls" : 
					break;
				case "mkdir" : 
					returnString = inputArr[1];
					break;
				case "upload" : 
					returnString = inputArr[1];
					break;
				case "download" :
					returnString = inputArr[1];
					break;
				case "exit" :
					break;
			}
			return returnString;
		}
	}