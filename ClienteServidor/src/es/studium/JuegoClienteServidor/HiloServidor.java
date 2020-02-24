package es.studium.JuegoClienteServidor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class HiloServidor extends Thread {
	DataInputStream fentrada;
	Socket socket;
	String nombreJugador;
	int numJugador;
	String[] partesMensaje;
	String compNums;
	Boolean nuevoJugador = true;

	public HiloServidor(Socket socket) {
		this.socket = socket;
		try {
			fentrada = new DataInputStream(socket.getInputStream());
		} catch (IOException e) {
			System.out.println("Error de E/S");
			e.printStackTrace();
		}
	}

	// Envia todos los mensajes actuales al cliente que se acaba de incorporar
	public void run() {
		ServidorJuego.mensaje.setText("Número de conexiones actuales: " + ServidorJuego.ACTUALES);
		String texto = ServidorJuego.textarea.getText();
		EnviarMensajes(texto);
		// Bucle en el que se recibe lo que el cliente escribe, lo comprueba y envia un resultado a todos los clientes.
		while (true) {
			String cadena = "";
			try {
				cadena = fentrada.readUTF();
				// Cuando un cliente finaliza con el botón Salir, se envía un # al servidor del juego y se sale del bucle.
				if (cadena.startsWith("#")) {
					ServidorJuego.ACTUALES--;
					ServidorJuego.mensaje.setText("Número de conexiones actuales: " + ServidorJuego.ACTUALES);
					cadena=cadena.replace("#", "");
					ServidorJuego.textarea.append(cadena+"\n");
					texto = ServidorJuego.textarea.getText();
					EnviarMensajes(texto);
					break;
				}
				// El texto que el cliente escribe se añade al textarea del servidor y se reenvía a todos los clientes
				else {
					//Se tiene en cuenta si es un jugador nuevo o no para permitir mostrar el mensaje de que se ha unido.
					if (!nuevoJugador) {
						partesMensaje = cadena.split(" ");
						nombreJugador = partesMensaje[0];
						numJugador = Integer.parseInt(partesMensaje[partesMensaje.length - 1]);
						//Se comprueba si ha acertado o no el número
						if (numJugador != ServidorJuego.numAdivinar) {
							if (numJugador < ServidorJuego.numAdivinar) {
								cadena = cadena + ". Pero el número es mayor a " + numJugador;
							} else {
								cadena = cadena + ". Pero el número es menor a " + numJugador;
							}
							ServidorJuego.textarea.append(cadena + "\n");
							texto = ServidorJuego.textarea.getText();
							EnviarMensajes(texto);
						} else {
							cadena = cadena+ " ¡¡¡¡Y HA ACERTADOOOO!!!!\n";
							ServidorJuego.textarea.append(cadena);
							texto = ServidorJuego.textarea.getText();
							EnviarMensajes(texto);
							EnviarMensajes("*"+nombreJugador);
						}
					} else {
						ServidorJuego.textarea.append(cadena + "\n");
						texto = ServidorJuego.textarea.getText();
						EnviarMensajes(texto);
						nuevoJugador=false;
					}
				}
			} catch (Exception ex) {
				break;
			}
		}
	}

	// Envía el texto del textarea a todos los sockets de la tabla de sockets, todos ven la conversación.
	private void EnviarMensajes(String texto) {
		for (int i = 0; i < ServidorJuego.CONEXIONES; i++) {
			Socket socket = ServidorJuego.tabla[i];
			try {
				// El programa abre un stream de salida para escribir el texto en el socket
				DataOutputStream fsalida = new DataOutputStream(socket.getOutputStream());
				fsalida.writeUTF(texto);
			} catch (IOException e) {
			
			}
		}
	}
}
