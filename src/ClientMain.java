import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClientMain {

    private Socket socket;
    private DataOutputStream output;
    private DataInputStream input;
    private ScheduledExecutorService threads;
    private String username;
    private Scanner scanner;

    public ClientMain(String ip, int port, String username) throws IOException {
        this.socket = new Socket(ip, port);
        this.output = new DataOutputStream(socket.getOutputStream());
        this.input = new DataInputStream(socket.getInputStream());

        this.threads = Executors.newScheduledThreadPool(2);
        this.scanner = new Scanner(System.in);
        this.username = username;

        threads.scheduleWithFixedDelay(this::serverListener, 0, 1, TimeUnit.MILLISECONDS);
        output.writeUTF(username);

        System.out.println("Nawiązano połączenie.");

        threads.scheduleWithFixedDelay(this::reserveTerm, 0, 1, TimeUnit.MILLISECONDS);
    }


    private void reserveTerm() {
        String appointment = scanner.nextLine();
        if (appointment.matches("1\\d:00")) {
            try {
                output.writeUTF(appointment);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Nieprawidłowy format danych. Powinno być hh:mm");
        }
    }

    private void serverListener() {
        try {
            System.out.println(input.readUTF());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            new ClientMain("localhost", 9090, args[0]);
        }
        catch (IOException e){
            System.out.println(e.getMessage());
        }
    }
}
