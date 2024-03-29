package es.studium.JuegoClienteServidor;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ServidorJuego extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	static ServerSocket servidor;
	// Datos de la conexion
	static final int PUERTO = 44444;
	static int CONEXIONES = 0;
	static int ACTUALES = 0;
	static int MAXIMO = 25;
	static JTextField mensaje = new JTextField("");
	static JTextField mensaje2 = new JTextField("");
	private JScrollPane scrollpane1;
	static JTextArea textarea;
	JButton salir = new JButton("Salir");
	static Socket[] tabla = new Socket[MAXIMO];
	// Datos para el n�mero generado por el juego
	public static int numAdivinar;
	static int numMaxAdivinar=100;
	static Random rd = new Random();

	public ServidorJuego() {
		// Construimos el entorno gr�fico
		super(" SERVIDOR DEL JUEGO ");
		getContentPane().setBackground(new Color(245, 255, 250));
		getContentPane().setLayout(null);
		mensaje.setBounds(10, 10, 400, 30);
		setResizable(false);
		getContentPane().add(mensaje);
		setIconImage(ClienteJuego.iconJuego.getImage());
		mensaje.setEditable(false);
		mensaje2.setBounds(10, 348, 400, 30);
		getContentPane().add(mensaje2);
		mensaje2.setEditable(false);
		textarea = new JTextArea();
		scrollpane1 = new JScrollPane(textarea);
		scrollpane1.setBounds(10, 50, 400, 300);
		getContentPane().add(scrollpane1);
		salir.setBounds(420, 10, 100, 30);
		getContentPane().add(salir);
		textarea.setEditable(false);
		salir.addActionListener(this);
		// Se ha anulado el cierre de la ventana para que la finalizaci�n del servidor se haga desde el bot�n Salir.
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	}

	public static void main(String args[]) throws Exception {
		// Se inicia el servidor y las variables y se prepara la pantalla.
		servidor = new ServerSocket(PUERTO);
		System.out.println("Servidor iniciado...");
		ServidorJuego pantalla = new ServidorJuego();
		pantalla.setBounds(0, 0, 540, 450);
		pantalla.setVisible(true);
		mensaje.setText("N�mero de conexiones actuales: " + 0);
		numAdivinar = rd.nextInt(numMaxAdivinar) + 1;
		System.out.println(numAdivinar);
		// Bucle para controlar el n�mero de conexiones.
		// En el bucle el servidor espera la conexi�n del cliente y cuando se conecta se crea un socket
		while (CONEXIONES < MAXIMO) {
			Socket socket;
			try {
				socket = servidor.accept();
			} catch (SocketException sex) {
				//Sale por aqu� si pulsamos el bot�n salir
				break;
			}
			//El socket se a�ade a la tabla, se incrementa el n�mero de conexiones y se lanza el hilo para gestionar los mensajes del cliente que se acaba de conectar
			tabla[CONEXIONES] = socket;
			CONEXIONES++;
			ACTUALES++;
			HiloServidor hilo = new HiloServidor(socket);
			hilo.start();
		}
		// 25 conexiones o el bot�n Salir hace que el programa se salga del bucle.
		if (!servidor.isClosed()) {
			try {
				mensaje2.setForeground(Color.red);
				mensaje2.setText("M�ximo N� de conexiones establecidas: " + CONEXIONES);
				servidor.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		} else {
			System.out.println("Servidor finalizado...");
		}
	}

	public void actionPerformed(ActionEvent e) {
		// Al pulsar Salir se cierra el ServerSocket
		if (e.getSource() == salir) {
			try {
				servidor.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			System.exit(0);
		}
	}
}
