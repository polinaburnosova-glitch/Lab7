package server;

import server.manager.CollectionManager;
import common.EnvLoader;
import server.database.DatabaseManager;

public class ServerMain {

    public static void main(String[] args) {
        int port = EnvLoader.getInt("SERVER_PORT", 5555);
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