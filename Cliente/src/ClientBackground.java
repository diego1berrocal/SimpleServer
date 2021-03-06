import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JOptionPane;

public class ClientBackground {
	static Socket s;
	static ObjectOutputStream oos;
	static ObjectInputStream ois;
	static boolean connected = true;
	static VentanaCliente vc;
	static Date date = new Date();
	static Thread t;

	public static void startBackground(int port, String ip) {
		try {
			Constantes.port = port;
			Constantes.host = ip;
		} catch (NumberFormatException e) {
			System.out.println("Valor no v�lido");
			System.exit(0);
		}
		vc = new VentanaCliente();
		vc.setVisible(true);
		String mensaje;
		

	}
	public static void createThread() {
		t = new Thread(new Runnable() {

			public void run() {
				while (connected) {

					try {

						Object o = ois.readObject();
						if (o instanceof Mensaje) {

							DateFormat hourdateFormat = new SimpleDateFormat("HH:mm:ss");
							System.out.println("Hora y fecha: " + hourdateFormat.format(date));
							System.out.println("Nuevo mensaje: " + ((Mensaje) o).getMensaje());
							vc.ta.setText(vc.ta.getText() + hourdateFormat.format(date) + " : "
									+ ((Mensaje) o).getMensaje() + "\n");

							// apagarpc();
						}
						if (o instanceof Comando) {
							if (!((Comando) o).isReset()) {
								if (((Comando) o).isDisplay()) {
									vc.ta.setText(vc.ta.getText() + "Conectado" + "\n");
									vc.b.setEnabled(true);
									vc.connect.setText("Disconnect");
									JOptionPane.showMessageDialog(null, ((Comando) o).getMensaje());
									vc.ta.setText(vc.ta.getText() + ((Comando) o).getMensaje() + "\n");
								} else {
									System.out.println(((Usuario) o).getNombre());
								}
							} else {
								vc.reset();
							}
						}
						if (o instanceof Usuario) {
							System.out.println("A�adiendo usuario");
							vc.a�adirUsuario((Usuario) o);
						}
						if (o instanceof ArrayList) {

							ArrayList<String> datos = ((ArrayList) o);
							for (int i = 0; i < datos.size(); i++) {
								vc.a�adirDato(datos.get(i));
							}
						}

					} catch (ClassNotFoundException e) {
						connected = false;
						vc.ta.setText("Desconectado" + "\n");

					} catch (IOException e) {
						connected = false;
						vc.ta.setText("Desconectado" + "\n");

					}

				}
			}

		});
	}

	public static void enviar() {
		String mensaje = "";
		mensaje = JOptionPane.showInputDialog(null);
		if (mensaje != null) {
			enviarMensajeAlServidor(mensaje);
			if (mensaje.equals("salir")) {
				disconnect();
				System.exit(0);
			}
		}

	}

	public static void enviarMensajeAlServidor(String mensaje) {
		Mensaje m = new Mensaje(mensaje);
		try {
			oos.writeObject(m);
		} catch (IOException e) {
			disconnect();
			e.printStackTrace();
		}

	}

	public static void enviarUsuarioAlServidor(Usuario u) {

		try {
			oos.writeObject(u);
		} catch (IOException e) {
			disconnect();
			e.printStackTrace();
		}

	}

	public static void connect() {
		
		DateFormat hourdateFormat = new SimpleDateFormat("dd/MM/yyyy");
		vc.ta.setText(vc.ta.getText() + hourdateFormat.format(date) + " ... " + System.getProperty("os.name") + "\n");
		vc.ta.setText(vc.ta.getText() + "Java version: " + System.getProperty("java.version") + "\n");
		connected = true;
		createThread();
		try {
			String usuario = JOptionPane.showInputDialog(null);
			if (usuario != null) {
				if (!usuario.equals("")) {

					s = new Socket(Constantes.host, Constantes.port);
					vc.connect.setText("Disconnect");

					OutputStream os = s.getOutputStream();
					InputStream is = s.getInputStream();
					oos = new ObjectOutputStream(os);
					ois = new ObjectInputStream(is);
					
					t.start();
					vc.ta.setText(vc.ta.getText() + "Enviando peticion de usuario" + "\n");
					enviarUsuarioAlServidor(new Usuario(usuario, s.getInetAddress().getHostAddress()));

				}
			}
		} catch (UnknownHostException e) {
			System.out.println("No se ha podido conectar");

			vc.ta.setText(vc.ta.getText() + "No se ha podido conectar" + "\n");

			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("No hay conexion a internet");

			vc.ta.setText(vc.ta.getText() + "No hay conexion a Internet" + "\n");

			e.printStackTrace();
		}

	}

	public static void disconnect() {

		System.out.println("Desconectado");
		connected = false;
		vc.b.setEnabled(false);
		vc.reset();
		vc.update();
		vc.connect.setText("Connect");

		try {
			s.close();
		} catch (IOException e) {
			System.out.println("No se pudo cerrar el socket");

			e.printStackTrace();
		}
	}

	public static void nuevaAplicacion() {

		String comando = "";

		comando = "java -jar Cliente.jar";

		try {
			Runtime.getRuntime().exec(comando);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

}
