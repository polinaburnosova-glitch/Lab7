package server;

import common.network.CommandType;
import common.network.Request;
import common.network.Response;
import common.network.ResponseStatus;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Многопоточный сервер с пулом потоков.
 * Использует CachedThreadPool для обработки клиентских подключений.
 *
 * @author Полина
 * @version 1.0
 * @since 2026-05-16
 */
public class ThreadPoolServer {

    /** Порт для прослушивания подключений. */
    private final int port;

    /** Исполнитель команд. */
    private final CommandExecutor commandExecutor;

    /** Флаг работы сервера. */
    private volatile boolean running = true;

    /** Пул потоков для обработки клиентов. */
    private final ExecutorService pool = Executors.newCachedThreadPool();

    /**
     * Конструктор сервера.
     *
     * @param port порт для прослушивания
     * @param commandExecutor исполнитель команд
     */
    public ThreadPoolServer(int port, CommandExecutor commandExecutor) {
        this.port = port;
        this.commandExecutor = commandExecutor;
    }

    /**
     * Запускает сервер.
     * Принимает клиентские подключения и передаёт их в пул потоков.
     */
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

    /**
     * Обрабатывает одного клиента.
     *
     * @param clientSocket сокет клиента
     */
    private void handleClient(Socket clientSocket) {
        try (ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream())) {

            while (true) {
                Request request = (Request) ois.readObject();
                System.out.println("Команда: " + request.getCommandType());

                Response response;
                try {
                    response = commandExecutor.execute(request);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    response = new Response(
                            ResponseStatus.SERVER_ERROR,
                            "Внутренняя ошибка сервера: " + ex.getMessage());
                }

                oos.writeObject(response);
                oos.flush();
                oos.reset();

                if (request.getCommandType() == CommandType.EXIT) {
                    break;
                }
            }
        } catch (EOFException e) {
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }

    /**
     * Останавливает сервер.
     * Закрывает пул потоков и прекращает приём подключений.
     */
    public void stop() {
        running = false;
        pool.shutdown();
    }
}