package Server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.Iterator;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;



public class MessageServer {

	/**
	 * @param args
	 */
	public static final int MESSAGE_PORT = 9000;
	
	Multimap<String, String> messageMultimap = ArrayListMultimap.create();
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("S: Connecting...");
		 
        //create a server socket. A server socket waits for requests to come in over the network.
        ServerSocket serverSocket = null;
		
    	try {
    			serverSocket = new ServerSocket(MESSAGE_PORT);	
    	}
    	catch (IOException e)
		{
            e.printStackTrace();
        }

        MessageServer server = new MessageServer();
    	
    	while(true){
    		Socket socket = null;
    		try{
    				System.out.println("S: Waiting...");
    				socket = serverSocket.accept();
    		}
    		catch (IOException e)
			{
                e.printStackTrace();
            }
    		
    		Thread thread = new Thread(server.new ProcessThread(socket));
    		thread.start();    		
    	}
	}
	
	private class ProcessThread implements Runnable {
    	
        private Socket socket;
        
        ProcessThread(Socket socket) {
            this.socket = socket;
        }

        public void run() {
	        BufferedReader in = null;
	        PrintWriter out = null;
	        try {
	            try {
	                /* obtain an input stream to this client ... */
	                in = new BufferedReader(new InputStreamReader(
	                		socket.getInputStream()));
	                /* ... and an output stream to the same client */
	                out = new PrintWriter(new BufferedWriter(
	                		new OutputStreamWriter(socket.getOutputStream())), true);
	            } catch (IOException e) {
	                e.printStackTrace();
	                System.out.println("S: " + e.getMessage());
	                if(socket != null)
	                	socket.close();
	                return;
	            }
	
	            String msg;
	            String messages = "";
	            String clientAddr = socket.getRemoteSocketAddress().toString();		
	            try {
	                while ((msg = in.readLine()) != null) {
	                	messages += msg + "\n";
	                }
	                if (messages != "")
					{
						String from = messages.substring(0, messages.indexOf("\n"));						
						String destAddr = InetAddress.getByName(from).toString();									
						String content = messages.substring(messages.indexOf("\n")+1);
						
						if(clientAddr.equals(destAddr))
						{
							System.out.println("S: received message from " + 
										clientAddr + " -- " + content);
						}
						else
						{
							System.out.println("S: received message from " + 
										clientAddr + " targeting " + destAddr);
							messageMultimap.put(destAddr, content);
						}
					}
	                
	                Collection<String> selectedMessages = messageMultimap.get(clientAddr);
	                for (Iterator<String> iterator = selectedMessages.iterator(); iterator.hasNext(); )
	                {
	                    String outMessage = (String) iterator.next();
	                    if (out != null && !out.checkError()) 
					    {
					    	out.println(outMessage);
					        out.flush();
					    }
	                }
	                messageMultimap.removeAll(clientAddr);
	                
	            } catch (IOException e) {
	                e.printStackTrace();
	                System.out.println("S: " + e.getMessage());
	            }
			}catch (IOException e) {
                e.printStackTrace();
                System.out.println("S: " + e.getMessage());
            }finally {
            	System.out.println("S: client processed");
            }
        }
    };
	
}
