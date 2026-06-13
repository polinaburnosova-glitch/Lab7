package server;

import server.manager.CollectionManager;
import common.EnvLoader;
import server.database.DatabaseManager;

/**
 * Главный класс серверного приложения.
 * Загружает коллекцию из БД, создаёт исполнитель команд и запускает сервер.
 *
 * @author Полина
 * @version 1.0
 * @since 2026-05-16
 */
public class ServerMain {

    /**
     * Точка входа в серверное приложение.
     *
     * @param args аргументы командной строки (порт)
     */
    public static void main(String[] args) {
        int port = EnvLoader.getInt("SERVER_PORT", 5556);
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Неверный порт, используется " + port);
            }
        }

        CollectionManager collectionManager = new CollectionManager();
        collectionManager.loadFromDatabase();

        CommandExecutor commandExecutor = new CommandExecutor(collectionManager);

        ThreadPoolServer server = new ThreadPoolServer(port, commandExecutor);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Остановка...");
            server.stop();
            DatabaseManager.closeConnection();
        }));

        server.start();
    }
}