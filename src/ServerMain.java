import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerMain {

    private ServerSocket serverSocket;
    private ScheduledExecutorService threads;
    private volatile Map<String, String> schedule;
    private List<DataOutputStream> outs;

    public ServerMain(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.threads = Executors.newScheduledThreadPool(10);
        this.schedule = new LinkedHashMap<>();
        this.outs = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            schedule.put("1" + i + ":00", "wolny");
        }

        threads.scheduleWithFixedDelay(this::initConnections, 0, 1, TimeUnit.MILLISECONDS);
    }

    private void initConnections() {
        try {
            Socket socket = serverSocket.accept();
            System.out.println("Podłączono użytkownika.");
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            DataInputStream input = new DataInputStream(socket.getInputStream());
            outs.add(output);

            ClientHandler clientHandler = new ClientHandler(output, input);
            threads.scheduleWithFixedDelay(clientHandler::handleClient, 0, 1, TimeUnit.MILLISECONDS);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private class ClientHandler {

        private DataOutputStream output;
        private DataInputStream input;
        private String username;

        public ClientHandler(DataOutputStream output, DataInputStream input) throws IOException {
            this.output = output;
            this.input = input;
            this.username = input.readUTF(); //Get client username;
            System.out.println("Połączono " + username);
            for (Map.Entry<String, String> entry : schedule.entrySet()) {
                output.writeUTF(entry.getKey() + " - " + entry.getValue());
            }
            output.writeUTF("Podaj termin, który chcesz zarezerwować: ");
        }

        private void handleClient() {
            try {
                boolean flag = false;
                String appointment = input.readUTF();

                if (schedule.get(appointment).equals(username)) {
                    schedule.put(appointment, "wolny");
                    flag = true;
                } else if (schedule.get(appointment).equals("wolny")) {
                    schedule.put(appointment, username);
                    flag = true;
                } else {
                    output.writeUTF("Nie możesz zarezerwować tego terminu.");
                }

                if (flag) {
                    for (DataOutputStream outIter : outs) {
                        for (Map.Entry<String, String> entry : schedule.entrySet()) {
                            outIter.writeUTF(entry.getKey() + " - " + entry.getValue());
                        }
                        outIter.writeUTF("Podaj termin, który chcesz zarezerwować lub anulować: ");
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public static void main(String[] args) {
        try {
            new ServerMain(9090);
        }
        catch (IOException e){
            System.out.println(e.getMessage());
        }
    }
}

