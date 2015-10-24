import java.io.*;
import java.net.*;
import java.util.Random;
import java.nio.charset.Charset;

class UDPServer extends Thread {
	int _udpport = 58041;
	int clientPort;
	int num_files;
	int num_SS;
	
	String delims = "[ \n]";
	String serverResponse = "";
	String num_servers;
	String port_to_client;
	String ss_to_client;
	
	String[] receivedString, readline;
	
	Random gerador = new Random();
	
	UDPServer(int port){
		_udpport = port;
	}
	
	public void run(){
		
		//Criacao do serverSocket UDP
		DatagramSocket serverSocket = null;
		try {
			serverSocket = new DatagramSocket(_udpport);
		} catch (SocketException e) {
			System.out.println("ERR: Nao consigo criar o serverSocket no port dado"); 
		}
		
		InetAddress clientAddress; 
		
		byte[] receiveData = new byte[1024];
		byte[] sendData = new byte[1024]; 

		while(true){
			num_files = 0;
			//Criacao e recepcao dos pacotes de cliente UDP 
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			try {
				serverSocket.receive(receivePacket);
			} catch (IOException e) {
				System.out.println("ERR: Nao consigo receber pacotes do cliente"); 
			}
			clientAddress = receivePacket.getAddress();
			clientPort = receivePacket.getPort();
			String sentence = new String( receivePacket.getData()); // Guarda dados recebidos numa string

			String clientIP = clientAddress.getHostAddress().toString();

			//Impressao do pedido do cliente em UDP
			receivedString = sentence.split(delims);
			System.out.println(receivedString[0] + " " + clientIP + " " + receivePacket.getPort()); 

			
			//Caso o pedido seja LST("list").
			if(receivedString[0].equals("LST")){
				int j = 0;
				int m; 
				BufferedReader ssinfo = null;
				try {
					ssinfo = new BufferedReader(new FileReader("ssinfo.txt"));
				} catch (FileNotFoundException e) {
					System.out.println("Nao consegui abrir o ficheiro"); 
				}
				//Guarda, a partir da leitura do ficheiro ssinfo.txt o numero de Storage Servers existentes.
				String[] lines = new String[30];
			
				//Le os a informacao dos SS
				
				try {
					for(j = 0; (lines[j] = ssinfo.readLine()) != null; j++){
						num_SS++;
					}
				} catch (IOException e1) {
					System.out.println("ERR: Nao consegui ler a informacao dos SS");
				}
				
				BufferedReader filesinfo = null;
				try {
					filesinfo = new BufferedReader(new FileReader("filesinfo.txt"));
				} catch (FileNotFoundException e) {
					System.out.println("Nao consegui abrir o ficheiro"); 
				}
				
				String[] files = new String[31];
				
				//Guarda o nome dos ficheiros num array para enviar para o cliente
				try {
					for(m = 0; (files[m] = filesinfo.readLine()) != null; m++){
						num_files++;
					}
				} catch (IOException e1) {
					System.out.println("ERR: Nao consegui ler a informacao dos Files");
				}
	
				
				for(int k=0; k<num_SS; k++){
					readline = lines[k].split(delims); 
					if(readline[0].equals(clientIP)){ 
						ss_to_client = readline[1];
						port_to_client = readline[2]; 
						break;
					} else {
						ss_to_client = "127.0.0.1"; // Especificar aqui o IP default
						port_to_client = "59000"; // Especificar aqui  o PORT default
					}
				}
				//Elaboracao da resposta do servidor em UDP
				serverResponse = "AWL" + " " + ss_to_client + " " +  port_to_client + " " + num_files;
				if(!(num_files == 0)){
					for(int i = 0; i < num_files; i++)
						serverResponse +=  " " + files[i];
				}
				serverResponse += "\n"; 
				
				sendData = serverResponse.getBytes(); 
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);    
				try {
					serverSocket.send(sendPacket);
				} catch (IOException e) {
					System.out.println("Nao consigo enviar os pacotes de dados para o cliente"); 				}

				try {
					ssinfo.close();
					filesinfo.close();
				} catch (IOException e) {
					System.out.println("Nao consigo fechar o ficheiro \"ssinfo.txt\""); 
				}
			readline = null;
			lines = null;
			num_SS = 0;
			num_files = 0;
			}
		}
	}
}

class TCPServer extends Thread {
	int _tcpport = 58041;
	int ssPort = 59000; 
	int k, d_size; 
	int d_count;
	int duplicado;
	int num_files;
	int num_SS;
	int j = 0;
	int m;

	
	String address = "localhost";
	String delims = "[ \n]";
	String num_servers;
	String ansToClient;
	String ssAns;
	
	String[] readline;
	String[] storageServer;
	String[] ans; 
	
	Random gerador = new Random();
	OutputStream os = null;
	Socket connectionSocket = null;
	Socket uploadSocket = null;
	BufferedReader br = null;
	ServerSocket serverTcpSocket = null;
	InputStream is = null;
	DataOutputStream outToClient = null;
	BufferedReader inFromSS = null;
	
	TCPServer(int port){
		_tcpport = port;
	}
	
	public void run(){

		//Criacao do serverSocket TCP
		
		try {
			serverTcpSocket = new ServerSocket(_tcpport);
		} catch (IOException e1) {
			System.out.println("Nao consegui criar o serverSocket");
		}
		char c = 0;
		while(true){
			
			num_SS = 0;
			num_files = 0;
			duplicado = 0;
			
			//Espera e aceita a ligacao TCP com o cliente.
			try {
				connectionSocket = serverTcpSocket.accept();
			} catch (IOException e1) {
				System.out.println("Nao consegui aceitar a conexao");
			}

			BufferedReader ssinfo = null;
			try {
				ssinfo = new BufferedReader(new FileReader("ssinfo.txt"));
			} catch (FileNotFoundException e) {
				System.out.println("Nao consegui abrir o ficheiro"); 
			}

			String[] lines = new String[30];
		
			//Le os a informacao dos SS
			
			try {
				for(j = 0; (lines[j] = ssinfo.readLine()) != null; j++){
					num_SS++;
				}
			} catch (IOException e1) {
				System.out.println("ERR: Nao consegui ler a informacao dos SS");
			}
			try {
				ssinfo.close();
			} catch (IOException e1) {
				System.out.println("Nao consegui fechar o ficheiro \"ssinfo.txt\"");
			}

			try {
				outToClient = new DataOutputStream(connectionSocket.getOutputStream());
			} catch (IOException e1) {
				System.out.println("Nao consegui criar o OutputStream para o cliente");
			}

			int stopFlag = 0;
			String clientRequest = "";
			String fileName = "";
			
			
			try {
				is = connectionSocket.getInputStream();
			} catch (IOException e1) {
				System.out.println("Nao consegui criar o InputStream para do cliente");
			}
			//Le o pedido enviado pelo cliente atraves da conneccao TCP.
			while(stopFlag != 1) {

				try {
					c = (char) (is.read() & 0xFF);
				} catch (IOException e) {
					System.out.println("Pedido mal formulado, nao recebi UPR");
				}
				clientRequest += c;

				if(clientRequest.equals("UPR")){
					//Le o espaco.
					System.out.println("UPR " + connectionSocket.getInetAddress().getHostAddress() + " " + connectionSocket.getPort());
					try {
						c = (char) (is.read() & 0xFF);
					} catch (IOException e) {
						System.out.println("Pedido mal formulado, falta o espaco entre o UPR e <filename>");
					}
					//Le o nome do ficheiro.
					try {
						c = (char) (is.read() & 0xFF);
					} catch (IOException e) {
						System.out.println("Pedido mal formulado, falta o nome do ficheiro");
					}
					while(c != '\n'){
						fileName += c;
						try {
							c = (char) (is.read() & 0xFF);
						} catch (IOException e) {
							System.out.println("Pedido mal formulado, falta o \\n"); 
						}
					}
					stopFlag = 1;
				}
			}

			//Caso o pedido seja UPR("upload").
			//Verifica se o pedido tem a sintaxe correcta verificando se o primeiro
			//elemento, e se e' composto por apenas 2 elementos.
			if(clientRequest.equals("UPR")){
				
				//=============== ACRESCEITEI ISTO AQUI ========//
				BufferedReader filesinfo = null;
				try {
					filesinfo = new BufferedReader(new FileReader("filesinfo.txt"));
				} catch (FileNotFoundException e) {
					System.out.println("Nao consegui abrir o ficheiro"); 
				}
				String[] files = new String[31];
				
				//Guarda o nome dos ficheiros num array para comparar no upload
				
				try {
					for(m = 0; (files[m] = filesinfo.readLine()) != null; m++){
						num_files++;
					}
				} catch (IOException e1) {
					System.out.println("ERR: Nao consegui ler a informacao dos Files");
				}
				
				//Compara o input do cliente com o nome dos ficheiros no array para ver se esta duplicado
				for(k = 0; k < num_files && duplicado != 1; k++){
					if(fileName.equals(files[k]))
						duplicado = 1;
				}
				
				
				if(duplicado == 1){
					//Envia resposta ao cliente avisando que o ficheiro esta' duplicado.
					try {
						outToClient.writeBytes("AWR dup\n");
					} catch (IOException e) {
						System.out.println("ERR: Nao foi possivel enviar a resposta \"AWR dup\" para o cliente.");
					}

				} // Testa as condicoes do ficheiro, tamanho, tamanho do nome e vazio
				else if(fileName.length() > 20){
					try{
						outToClient.writeBytes("ERR\n");
						System.out.println("ERR: O servidor nao aceita ficheiros com nome superior a 20 caracteres.");
					} catch (IOException e) {
						System.out.println("ERR: Nao foi possivel enviar a resposta \"ERR\" para o cliente.");
					}

				}
				else{
					//Envia resposta ao cliente avisando que o ficheiro nao esta' duplicado.
					try {
						outToClient.writeBytes("AWR new\n");
					} catch (IOException e1) {
						System.out.println("ERR: Nao foi possivel enviar a resposta \"AWR new\" para o cliente");
						}

					// Espera envio de dados pelo cliente...
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					
					String data_size = "";
					
					//Le a informacao enviada pelo cliente bit a bit e guarda o tamanho do ficheiro a receber.
					for (d_count = 0; d_count < 2;) {
						try {
							c = (char) (is.read() & 0xFF);
						} catch (IOException e) {
							System.out.println("ERR: Nao foi possivel receber os dados do cliente");
						}

						if (d_count == 1 && c != ' ') {
							data_size += c;
						}
						if (c == ' ')
							d_count++;
					}

					//Converte string com o tamanho do ficheiro no numero inteiro correspondente.
					d_size = Integer.parseInt(data_size);
					if(d_size == 0){
						try{
							outToClient.writeBytes("ERR\n");
							System.out.println("ERR: O servidor nao pode aceitar ficheiros vazios.");
						} catch (IOException e) {
							System.out.println("ERR: Nao foi possivel enviar a resposta \"ERR\" para o cliente.");
						}
					}
					else if(d_size > 5000000){
						try{
							outToClient.writeBytes("ERR\n");
							System.out.println("ERR: O servidor nao pode aceitar ficheiros com tamanho superior a 5 megabytes.");
						} catch (IOException e) {
							System.out.println("ERR: Nao foi possivel enviar a resposta \"ERR\" para o cliente.");
						}
					}
					else{
						//Guarda a data do ficheiro num buffer de bytes.
						byte[] mybytearray = new byte[d_size];

						int current = 0;
						int bytesRead = 0;
						try {
							bytesRead = is.read(mybytearray, 0, mybytearray.length);
						} catch (IOException e) {
							System.out.println("ERR: Nao consegui guardar os bytes do ficheiro no buffer");
						}
						current = bytesRead;

						do {
							try {
								bytesRead = is.read(mybytearray, current, (mybytearray.length - current));
							} catch (IOException e) {
								System.out.println("ERR: Nao foi possivel guardar os bytes do ficheiro no buffer");
							}
							if (bytesRead >= 0)
								current += bytesRead;
						} while (bytesRead > 0);

						//Constroi buffer de bytes para encaminhar a data do ficheiro para os SS.
						String upMsg = "UPS " + fileName + " " + data_size + " ";
						byte[] upByte = upMsg.getBytes(Charset.forName("UTF-8"));

						try {
							outputStream.write(upByte);
						} catch (IOException e1) {
							System.out.println("ERR: Nao foi possivel escrever o upByte no OutputStream");
						}
						try {
							outputStream.write(mybytearray);
						} catch (IOException e1) {
							System.out.println("ERR: Nao foi possivel escrever o mybytearray no OutputStream");
						}

						byte buffer[] = outputStream.toByteArray();

						//Caso nenhum dos SS devolva nok ou ERR o CS devolve a mensagem abaixo, caso
						//contrario para o ciclo usando a flag "upError" e envia a mensagem correspondente.
						ansToClient = "AWS ok\n";
						int upError = 0; 
					
						
						//Ciclo para encaminhamento da data do ficheiro para todos os SS registados.
						for(int j = 0; j < num_SS && upError == 0; j++){
							storageServer = lines[j].split(delims);
							ssPort = Integer.parseInt(storageServer[2]);
							address = storageServer[1];
							
							try {
								uploadSocket = new Socket(address, ssPort);
							} catch (UnknownHostException e) {
								System.out.println("ERR: Nao foi estabelecer a conexao com o cliente, o Host e desconhecido.");
							} catch (IOException e) {
								System.out.println("ERR: Nao foi possivel criar o uploadSocket.");
							} // Coneccao com o SS

							
							try {
								inFromSS = new BufferedReader(new InputStreamReader(uploadSocket.getInputStream()));
							} catch (IOException e) {
								System.out.println("ERR: Nao foi possivel criar o InputStream para os SS.");
							}

							//Cria canal de output para o SS atual e envia a data do ficheiro.
							try {
								os = uploadSocket.getOutputStream();
							} catch (IOException e) {
								System.out.println("ERR: Nao foi possivel criar o OutputStream para os SS.");
							}
							try {
								os.write(buffer, 0, buffer.length);
							} catch (IOException e) {
								System.out.println("ERR: Nao foi possivel escrever para o  OutputStream dos SS.");
							}
							try {
								os.flush();
							} catch (IOException e) {
								System.out.println("ERR: Nao foi possivel limpar o OutputStream dos SS.");
							}

							try {
								ssAns = inFromSS.readLine();	
							} catch (IOException e) {
								System.out.println("ERR: Nao foi possivel ler a resposta dos SS.");
							}
							ans = ssAns.split(delims);

							if(ans.length < 2){
								ansToClient = "ERR\n";
								upError = 1;
							}
							else if(ans[1].equals("nok")){
								ansToClient = "AWS nok\n";
								upError = 1;
							}
						}
						if (ansToClient.equals("AWS ok\n")){

							String auxString = null;
							File old = new File("filesinfo.txt");
				            old.delete();
							
							try{
				            	PrintWriter newFile = new PrintWriter("filesinfo.txt", "UTF-8");
							
								// Se o ficheiro de texto com as informacoes dos SS 
								// tiver menos de 30 ficheiro guardados em cada SS atualiza a lista normalmente.
								if(num_files < 30){
									for(j = 0; j < num_files; j++){
										auxString = files[j].replace("\n", "");
										newFile.println(auxString);
									}
									newFile.println(fileName);
									
								}
								// caso contrario retira o ficheiro mais antigo e adiciona o novo 'a lista.	
								else{
									for(j = 1; j < num_files; j++){
										auxString = files[j].replace("\n", "");
										newFile.println(auxString);
									}
									newFile.println(fileName);
								}
								newFile.close();

							} catch(Exception e){
								ansToClient = "ERR\n";
							}
						}

						try {
							outToClient.writeBytes(ansToClient);
						} catch (IOException e) {
							System.out.println("Nao consigo enviar a resposta para o cliente"); 
						}
					}
				}

			} else{
				//Envia resposta de erro ao cliente devido 'a sintaxe.
				try {
					outToClient.writeBytes("ERR\n");
				} catch (IOException e) {
					System.out.println("ERR: Nao consigo enviar a resposta para o cliente");
				}
			}
		}
	}
}
	

public class CS {
	public static void main(String[] args) throws Exception{
		int port = 58041; 
		
		if (args.length > 1 && args[0].equals("-p")) {
			port = Integer.parseInt(args[1]);
		}

		UDPServer udpconnection = new UDPServer(port); 
		TCPServer tcpconnection = new TCPServer(port); 
		
		udpconnection.start();
		tcpconnection.start();

			
	}
}
