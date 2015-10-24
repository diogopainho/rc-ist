import java.io.*;
import java.net.*;
import java.nio.charset.Charset;

public class SS {
	public static void main(String[] args) throws Exception {

		int port = 59000;
		int stopFlag, d_size;
		if (args.length >= 1 && args[0].equals("-p")) {
			port = Integer.parseInt(args[1]);
		}
		String delims = "[ ]";
		String serverResponse, clientRequest, endMsg, fileName, data_size;
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		OutputStream os = null;
		ServerSocket serverSocket = null;
		Socket connectionSocket = null;

		try {
			serverSocket = new ServerSocket(port);

			while (true) {
				try {
					clientRequest = "";
					fileName = "";
					data_size = "";
					stopFlag = 0;
					connectionSocket = serverSocket.accept();

					BufferedReader inFromClient = new BufferedReader(
							new InputStreamReader(
									connectionSocket.getInputStream()));
					os = connectionSocket.getOutputStream();

					InputStream is = connectionSocket.getInputStream();
					char c;
					
					while(stopFlag != 1) {

						c = (char) (is.read() & 0xFF);
						clientRequest += c;
						// Teste ao retrive do cliente formulacao do pedido no SS
						if(clientRequest.equals("REQ")){
							//Le o espaco.
							c = (char) (is.read() & 0xFF);
							//Le o nome do ficheiro.
							c = (char) (is.read() & 0xFF);
							while(c != '\n'){
								fileName += c;
								c = (char) (is.read() & 0xFF);
							}
							stopFlag = 1;
						}
						// Teste ao UPS do CS e formulacao do pedido no SS
						else if(clientRequest.equals("UPS")){
							//Le o espaco.
							c = (char) (is.read() & 0xFF);
							//Le o nome do ficheiro.
							c = (char) (is.read() & 0xFF);
							while(c != ' '){
								fileName += c;
								c = (char) (is.read() & 0xFF);
							}
							//Le caracteres com o tamanho do ficheiro.
							c = (char) (is.read() & 0xFF);
							while(c != ' '){
								data_size += c;
								c = (char) (is.read() & 0xFF);
							}
							stopFlag = 1;
						} 

		
					}
					if(!clientRequest.equals("REQ") && !clientRequest.equals("UPS")) {
						System.out.println("ERR: Pedido mal formulado pelo Cliente ou Central Server.");
					}

					//Falta fazer print do pedido do cliente

					try {
						if (clientRequest.equals("REQ")) {

							//Imprime o pedido do utilizador.
							//================TESTAR SE IP'S ESTAO CERTOS!!!!!!!================
							System.out.println(fileName + " " + connectionSocket.getInetAddress() + " " + connectionSocket.getPort()); 
							
							File myFile = new File(fileName);
							
							ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
							
							serverResponse = "REP ok " + myFile.length() + " ";
							endMsg = "\n";
							
							byte[] resp = serverResponse.getBytes(Charset
									.forName("UTF-8"));
							byte[] aux = new byte[(int) myFile.length()];

							byte[] endByte = endMsg.getBytes(Charset
										.forName("UTF-8"));

							fis = new FileInputStream(myFile);
							bis = new BufferedInputStream(fis);
							
							bis.read(aux, 0, aux.length);

							outputStream.write( resp );
							outputStream.write( aux );
							outputStream.write( endByte );

							byte buffer[] = outputStream.toByteArray( );
							
							os.write(buffer, 0, buffer.length);
							os.flush();

						} else if(clientRequest.equals("UPS")){
							


							DataOutputStream outToCS = new DataOutputStream(
									connectionSocket.getOutputStream());
							
							try{
								d_size = Integer.parseInt(data_size);
								byte[] mybytearray = new byte[d_size];

								FileOutputStream fos = new FileOutputStream(fileName);
								BufferedOutputStream bos = new BufferedOutputStream(fos);

								int current = 0;
								int bytesRead = is.read(mybytearray, 0, mybytearray.length);
								current = bytesRead;
								
								do {
									bytesRead = is.read(mybytearray, current,
											(mybytearray.length - current));
									if (bytesRead >= 0)
										current += bytesRead;
								} while (bytesRead > 0);

								bos.write(mybytearray, 0, current);
								bos.flush();
								
								File f = new File(fileName);
								
								if(f.exists()){
									outToCS.writeBytes("AWS ok\n");
									//O servidor ecoa que o ficheiro foi proveniente do CS foi guardado.
									System.out.println("O ficheiro " + fileName + " originario do servidor central foi guardado com sucesso"); 
								} else
									outToCS.writeBytes("AWS nok\n");
							} catch(Exception e){
							 	outToCS.writeBytes("ERR\n"); //===== Verificar se o CS apanha este ERR ====//
							}

						} else {
							serverResponse = "ERR\n";
							byte[] resp = serverResponse.getBytes(Charset.forName("UTF-8"));

							os.write(resp, 0, resp.length); //===== Verificar se o CS apanha este ERR ====//
						}

					} catch (FileNotFoundException e) {
						// Envia resposta para o cliente com status a avisar que
						// o ficheiro nao foi encontrado e os outros campos a
						// null
						serverResponse = "REP nok " + 0 + " " + null;
						byte[] resp = serverResponse.getBytes(Charset.forName("UTF-8"));

						os.write(resp, 0, resp.length);
					}
				} finally {
					if (bis != null)
						bis.close();
					if (os != null)
						os.close();
					if (connectionSocket != null)
						connectionSocket.close();
					if (fis != null)
						fis.close();
				}

			}
		} catch( java.net.BindException e){
			System.out.println("Porto ja em uso, use a opcao -p para definir novo porto");
		} finally {
			if (serverSocket != null)
				serverSocket.close();
		}
	}
}
