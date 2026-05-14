package server;

import common.network.CommandType;
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
    private final ExecutorService pool = Executors.newCachedThreadPool();

    public ThreadPoolServer(int port, CommandExecutor commandExecutor) {
        this.port = port;
        this.commandExecutor = commandExecutor;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен на порту " + port);

            while (running) {
                Socket clientSocket = serverSocket.accept();
                pool.submit(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket clientSocket) {
        try (ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
             ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream())) {

            while (true) {
                Request request = (Request) ois.readObject();
                System.out.println("Команда: " + request.getCommandType());

                Response response = commandExecutor.execute(request);

                oos.writeObject(response);
                oos.flush();
                oos.reset();

                if (request.getCommandType() == CommandType.EXIT) {
                    break;
                }
            }
        } catch (EOFException e) {
            // клиент закрыл соединение
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }

    public void stop() {
        running = false;
        pool.shutdown();
    }
}