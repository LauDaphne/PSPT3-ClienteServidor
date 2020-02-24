package es.studium.JuegoClienteServidor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.SwingConstants;
import java.awt.Color;

public class ClienteJuego extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	Socket socket;
	String nombre;
	DataInputStream fentrada;
	DataOutputStream fsalida;
	JTextField mensaje = new JTextField();
	private JScrollPane scrollpane;
	static JTextArea textarea;
	JButton boton = new JButton("Enviar");
	JButton desconectar = new JButton("Salir");
	boolean repetir = true;
	int numJugador;
	//Imagenes del juego
	static ImageIcon iconJuego = new ImageIcon("iconoJuego.png");
	static ImageIcon iconGanador = new ImageIcon("iconoGanador.png");
	// Para la espera de 3 seg
	boolean bloqueo = false;
	// Componentes de ganador
	JLabel lblIconoGanador = new JLabel("");
	JLabel lblTituloGanador;
	JLabel lblGanador;
	// Componente de error
	JLabel lblError;

	public ClienteJuego(Socket socket, String nombre) {
		// Prepara la pantalla. Se recibe el socket creado y el nombre del cliente
		super(" JUGADOR: " + nombre);
		getContentPane().setBackground(new Color(240, 255, 240));
		getContentPane().setLayout(null);
		mensaje.setBounds(10, 10, 405, 30);
		setResizable(false);
		getContentPane().add(mensaje);
		textarea = new JTextArea();
		scrollpane = new JScrollPane(textarea);
		scrollpane.setBounds(10, 67, 405, 283);
		setIconImage(iconJuego.getImage());
		getContentPane().add(scrollpane);
		boton.setBounds(425, 10, 94, 30);
		getContentPane().add(boton);
		desconectar.setBounds(425, 320, 94, 30);
		getContentPane().add(desconectar);
		textarea.setEditable(false);
		boton.addActionListener(this);
		this.getRootPane().setDefaultButton(boton);

		lblTituloGanador = new JLabel("GANADOR");
		lblTituloGanador.setHorizontalAlignment(SwingConstants.CENTER);
		lblTituloGanador.setVisible(false);
		lblTituloGanador.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblTituloGanador.setBounds(428, 146, 94, 23);
		getContentPane().add(lblTituloGanador);

		lblGanador = new JLabel("");
		lblGanador.setHorizontalAlignment(SwingConstants.CENTER);
		lblGanador.setVisible(false);
		lblGanador.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblGanador.setBounds(432, 180, 90, 23);
		getContentPane().add(lblGanador);
		lblIconoGanador.setVisible(false);
		lblIconoGanador.setIcon(iconGanador);
		lblIconoGanador.setBounds(440, 67, 69, 64);

		getContentPane().add(lblIconoGanador);

		lblError = new JLabel("");
		lblError.setForeground(new Color(255, 0, 0));
		lblError.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblError.setBounds(10, 42, 400, 14);
		getContentPane().add(lblError);
		desconectar.addActionListener(this);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.socket = socket;
		this.nombre = nombre;
		// Se ha anulado el cierre de la ventana para que la finalización del cliente se haga desde el botón Salir.
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		// El flujo de salida escribe un mensaje de que el cliente se ha unido al juego.
		// El HiloServidor recibe el mensaje y lo reenvía a los los clientes conectados
		try {
			fentrada = new DataInputStream(socket.getInputStream());
			fsalida = new DataOutputStream(socket.getOutputStream());
			String texto = "Entra en el juego... " + nombre;
			fsalida.writeUTF(texto);
		} catch (IOException ex) {
			System.out.println("Error de E/S");
			ex.printStackTrace();
			System.exit(0);
		}
	}

	// Se crea la conexión al servidor y se crear la pantalla del juego (ClientJuegp) lanzando su ejecución (ejecutar()).
	public static void main(String[] args) throws Exception {
		int puerto = 44444;
		String nombre;
		//Bucle que pide el nombre al cliente, no sale hasta que se escriba algo
		do {
			nombre = (String) JOptionPane.showInputDialog(null, "Introduce tu nombre o nick:", "LOGIN",
					JOptionPane.INFORMATION_MESSAGE, iconJuego, null, "");
			Socket socket = null;
			try {
				socket = new Socket("127.0.0.1", puerto);
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(null, "Imposible conectar con el servidor \n" + ex.getMessage(),
						"<<Mensaje de Error:1>>", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
			//Crea el cliente si se introduce un nombre
			if (!nombre.trim().equals("")) {
				ClienteJuego cliente = new ClienteJuego(socket, nombre);
				cliente.setBounds(0, 0, 540, 400);
				cliente.setLocationRelativeTo(null);
				cliente.setVisible(true);
				cliente.ejecutar();
			} else {
				JOptionPane.showMessageDialog(null, "No se ha introducido ningún nombre o nick. Vuelva a intentarlo.\n",
						"ERROR", JOptionPane.ERROR_MESSAGE);
			}
		} while (nombre.trim().equals(""));
	}

	// Botón Enviar: el mensaje introducido se envía al servidor por el flujo de salida
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == boton) {
			if (mensaje.isEnabled()) {
				try {
					numJugador = Integer.parseInt(mensaje.getText());
					String texto =nombre + " piensa que el número es el  " + mensaje.getText();
					// Si el número se ha enviado bien se asegura de restablecer los componentes
					try {
						lblError.setText("");
						mensaje.setText("");
						fsalida.writeUTF(texto);
						bloqueo= true;
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				} catch (Exception e1) {
					// Aparece un mensaje si se escribe algo que no se pueda pasar a integer
					mensaje.setText("");
					lblError.setText("NO ha introducido el número correctamente.");
				}
			}
		}
		// Botón Salir: envía un mensaje indicando que el cliente abandona el juego con un # para indicar al servidor que el cliente se ha cerrado
		if (e.getSource() == desconectar) {
			try {
				fsalida.writeUTF("#Ha abandonado el juego el jugador... "+nombre);
				repetir = false;
				System.exit(0);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}


	public void ejecutar() {
		String texto = "";
		while (repetir) {
			try {
				texto = fentrada.readUTF();
				// Si no es el mensaje con el nombre del ganador entra
				if (!texto.startsWith("*")) {
					textarea.setText(texto);
					// Bloquea el poder escribir por 3s. si se ha introducido un numero incorrecto
					if(bloqueo & !texto.endsWith("¡¡¡¡Y HA ACERTADOOOO!!!!\n")) {
						mensaje.setEnabled(false);
						Thread.sleep(3000);
						mensaje.setEnabled(true);
						mensaje.requestFocus();
						bloqueo=false;
					}
				} else {
					// Si alguien ha ganado se bloquea el poder escribir y aparece el ganador
					lblIconoGanador.setVisible(true);
					lblTituloGanador.setVisible(true);
					lblGanador.setText(texto.substring(1, texto.length()));
					lblGanador.setVisible(true);
					mensaje.setEnabled(false);

				}

			} catch (IOException ex) {
				// Sale del bucle tras un mensaje de error
				JOptionPane.showMessageDialog(null, "Imposible conectar con el servidor \n" + ex.getMessage(),
						"<<Mensaje de Error:2>>", JOptionPane.ERROR_MESSAGE);
				repetir = false;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//Cerrar el socket y salir
		try {
			socket.close();
			System.exit(0);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

}