import java.io.*;
import java.net.*;
import java.nio.charset.Charset;

// Proj redes grupo 41

public class user {
	public static void main(String args[]) throws IOException {

		// Preparacao para fazer a ligacao com o servidor, ou seja, uso do
		// comando user -n <address> -p <port>
		String input, cmd;
		String address = "localhost";
		String SSip = "localhost";
		String delims = "[ \n]";
		String[] tokens = {};
		String[] file_list;
		int port = 58041; // 58000 + 41(NG)
		int SSport = 58041;
		int n_files;

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		// Testa o comando se o comando user para fazer connect e usado corretamente

		if (args.length > 1) {
			// Teste da ausencia das opcoes -n e -p
			if (args.length > 1) {
				if (args[0].equals("-n") && args[2].equals("-p")) {
					address = args[1];
					port = Integer.parseInt(args[3]);
				} else if (args[0].equals("-p")) {
					port = Integer.parseInt(args[1]);
				} else if (args[0].equals("-n")) {
					address = args[1];
				}
			} else
				System.out.println("ERR: O user foi mal inicializado, por favor reinicialize o user com os devidos parametros.");
		}

		input = br.readLine();
		tokens = input.split(delims);

		while (!tokens[0].equals("exit")) {
			try {

				//Menu de comandos
				if (tokens[0].equals("list")) {
					// Coneccao cliente em UDP para uso do comando list
					DatagramSocket clientSocket = new DatagramSocket();
					InetAddress IPAddress = InetAddress.getByName(address);
					byte[] sendData = new byte[1024];
					byte[] receiveData = new byte[1024];
					cmd = "LST\n";
					sendData = cmd.getBytes();
					DatagramPacket sendPacket = new DatagramPacket(sendData,
							sendData.length, IPAddress, port);
					clientSocket.send(sendPacket);
					DatagramPacket receivePacket = new DatagramPacket(
							receiveData, receiveData.length);
					clientSocket.receive(receivePacket);
					String server_msg = new String(receivePacket.getData());
					clientSocket.close();

					// Tratamento da mensagem que recebe do LST
					file_list = server_msg.split(delims);
					n_files = Integer.parseInt(file_list[3]);

					if (file_list[0].equals("AWL")) {
						SSip = file_list[1];
						SSport = Integer.parseInt(file_list[2]);

						System.out.println("AWL" + " " + SSip + " " + SSport);

						for (int i = 4; i < n_files + 4; i++)
							System.out.println((i - 3) + " - " + file_list[i]);
					} else if(file_list[0].equals("ERR")){
						System.out.println("ERR: O servidor central nao conseguiu listar os ficheiros nos Storage Servers"); 
					}
				}
				// Fazer conexao tcp com o SS e enviar para o server
				// "REQ <filename>"
				else if (tokens[0].equals("retrieve")) {
					if (SSip == "null" || SSport == 0 || tokens.length < 2) {
						System.out.println("ERR: O IP e/ou o Porto do Storage Server nao estao definidos. Experimente fazer primeiramente um list para o Central Server");
					} else {
						Socket clientSocket = null;
						FileOutputStream fos = null;
						BufferedOutputStream bos = null;

						try {
							clientSocket = new Socket(SSip, SSport);

							DataOutputStream outToServer = new DataOutputStream(
									clientSocket.getOutputStream());

							outToServer.writeBytes("REQ " + tokens[1] + "\n");

							String status = "";
							String data_size = "";
							int d_size, error_flag = 0;
							
							InputStream is = clientSocket.getInputStream();
							char c;
							
							for (int d_count = 0; d_count < 3;) {

								c = (char) (is.read() & 0xFF);

								if (d_count == 0 && c == '\n') {
									error_flag = 1;
									break;
								}

								if (d_count == 1 && c != ' ') {
									status += c;
									if (status.equals("nok")){ 
										error_flag = 1;
										break;
									}
								} else if (d_count == 2 && c != ' ') {
									data_size += c;
								}
								if (c == ' ')
									d_count++;
							}
							
							if (error_flag == 1) {	
								System.out.println("ERR: O Storage Server nao enviou o a resposta devidamente formulada ou entao nao contem o ficheiro pretendido. Verifique o nome do ficheiro.");
							
							} else if (status.equals("ok")) {
								d_size = Integer.parseInt(data_size);
								byte[] mybytearray = new byte[d_size];

								fos = new FileOutputStream(tokens[1]);
								bos = new BufferedOutputStream(fos);

								int current = 0;
								int bytesRead = is.read(mybytearray, 0,
										mybytearray.length);
								current = bytesRead;

								do {
									bytesRead = is.read(mybytearray, current,
											(mybytearray.length - current));
									if (bytesRead >= 0)
										current += bytesRead;
								} while (bytesRead > 0);

								bos.write(mybytearray, 0, current);
								bos.flush();
								System.out.println("Retrieve do ficheiro " + tokens[1] + " feito com sucesso");
								
							} else {
								System.out.println("ERR: Nao foi possivel obter o pedido junto do SS. O Storage Server podera ter formulado mal a respota com os dados.");
							}

						} finally {
							if (fos != null)
								fos.close();
							if (bos != null)
								bos.close();
							if (clientSocket != null)
								clientSocket.close();
						}
					}
				}

				// Fazer conexao tcp com o CS e enviar para o server UPR
				// <filename> (para status) e UPC <size> <data> (para enviar o
				// <filename>
				else if (tokens[0].equals("upload")) {
					File f = new File(tokens[1]);
					
					if (!(tokens.length == 2)) { 
						System.out.println("ERR: O pedido foi mal formulado. Este deve ter a forma: \"upload <filename>\"");
					}if(!(f.exists())){
						System.out.println("O ficheiro nao existe na directoria especificada.");
					} else {
						String serverAns = "";
						String[] ans;
						Socket uploadSocket = null;
						FileInputStream fis = null;
						BufferedInputStream bis = null;
						OutputStream os = null;

						try {
							uploadSocket = new Socket(address, port);
							os = uploadSocket.getOutputStream();

							File upFile = new File(tokens[1]);

							BufferedReader inFromServer = new BufferedReader(
									new InputStreamReader(
											uploadSocket.getInputStream()));

							DataOutputStream outToServer = new DataOutputStream(
									uploadSocket.getOutputStream());

							outToServer.writeBytes("UPR " + tokens[1] + "\n");
							serverAns = inFromServer.readLine();
							ans = serverAns.split(delims);

							if (ans[1].equals("new")) {
								ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

								String upMsg = "UPC " + upFile.length() + " ";
								String endMsg = "\n";

								byte[] upByte = upMsg.getBytes(Charset
										.forName("UTF-8"));

								byte[] aux = new byte[(int) upFile.length()];
								
								byte[] endByte = endMsg.getBytes(Charset
										.forName("UTF-8"));

								fis = new FileInputStream(upFile);
								bis = new BufferedInputStream(fis);

								bis.read(aux, 0, aux.length);

								outputStream.write(upByte);
								outputStream.write(aux);
								outputStream.write(endByte);

								byte buffer[] = outputStream.toByteArray();

								os.write(buffer, 0, buffer.length);
								os.flush();

								serverAns = inFromServer.readLine();
								ans = serverAns.split(delims);
								
								if(ans[1].equals("ok")){
									System.out.println("Upload do ficheiro " + tokens[1] + " feito com sucesso!");
								}
								
								if(ans[1].equals("ERR")){
									System.out.println("ERR: Nao foi possivel fazer o upload do ficheiro " + tokens[1]);
								}

							} else if (ans[1].equals("dup")) {
								System.out.println("ERR: O ficheiro " + tokens[1] + " que esta para upload ja se encontra nos Storage Servers");
							} else {
								System.out.println("ERR: O pedido foi mal formulado, o upload nao teve sucesso");
							}

						} catch (Exception e) {
							System.out.println("ERR: Upload sem efeito, nao foi possivel fazer o upload num ou mais Storage Servers");
						} finally {
							if (bis != null)
								bis.close();
							if (os != null)
								os.close();
							if (uploadSocket != null)
								uploadSocket.close();
							if (fis != null)
								fis.close();
						}

					}

				} else if (!tokens[0].equals("exit")) {
					System.out.println("ERR: Comando inexistente");
				}

			} catch (Exception e) {
				System.out.println("ERR: O cliente nao esta a funcionar corretamente, por favor reinicialize o programa.");
			}

			input = br.readLine();
			tokens = input.split(delims);
		}
	}
}
