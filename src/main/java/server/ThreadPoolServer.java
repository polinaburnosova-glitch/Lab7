package server;

import common.network.Request;
import common.network.Response;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolServer {
    private final int port;
    private final CommandExecutor commandExecutor;
    private volatile boolean running = true;

    private final ExecutorService acceptPool = Executors.newCachedThreadPool();

    public ThreadPoolServer(int port, CommandExecutor commandExecutor) {
        this.port = port;
        this.commandExecutor = commandExecutor;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("ThreadPoolServer запущен на порту " + port);
            System.out.println("Ожидание подключений");

            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Клиент подключился: " + clientSocket.getInetAddress());
                acceptPool.submit(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("Ошибка сервера: " + e.getMessage());
        }
    }

    private void handleClient(Socket clientSocket) {
        try (ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
             ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream())) {

            Request request = (Request) ois.readObject();
            System.out.println("Получена команда: " + request.getCommandType());

            Response response = commandExecutor.execute(request);

            oos.writeObject(response);
            oos.flush();
            System.out.println("Ответ отправлен");

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Ошибка при обработке клиента: " + e.getMessage());
        }
    }

    public void stop() {
        running = false;
        acceptPool.shutdown();
        System.out.println("Сервер остановлен");
    }
}