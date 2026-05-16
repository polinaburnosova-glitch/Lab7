package client;

import client.commands.CommandHandler;
import client.console.ConsoleInputReader;
import client.script.ScriptExecutor;
import common.EnvLoader;
import common.model.User;
import common.network.CommandType;
import common.network.Request;
import common.network.Response;
import common.network.ResponseStatus;
import java.io.IOException;
import java.util.Scanner;

/**
 * Главный класс клиентского приложения.
 *
 * <p>Обеспечивает запуск клиента, авторизацию пользователя и основной цикл
 * обработки команд. Поддерживает как интерактивный режим, так и выполнение
 * скриптов из файлов.</p>
 *
 * @author Полина
 * @version 1.0
 * @since 2026-05-16
 */
public class ClientMain {

    /**
     * Точка входа в клиентское приложение.
     *
     * @param args аргументы командной строки: [host] [port]
     */
    public static void main(String[] args) {
        String host = EnvLoader.get("CLIENT_HOST", "localhost");
        int port = EnvLoader.getInt("CLIENT_PORT", 5555);

        if (args.length > 0) {
            host = args[0];
        }
        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Неверный порт, используется " + port);
            }
        }

        SimpleClient client = new SimpleClient(host, port);

        try {
            client.connect();

            Scanner scanner = new Scanner(System.in);
            User currentUser = null;

            while (currentUser == null) {
                System.out.println("Выберите действие:");
                System.out.println("1 - Вход (LOGIN)");
                System.out.println("2 - Регистрация (REGISTER)");
                System.out.print("> ");
                String choice = scanner.nextLine().trim();

                try {
                    if (choice.equals("1")) {
                        currentUser = login(scanner, client);
                    } else if (choice.equals("2")) {
                        currentUser = register(scanner, client);
                    } else {
                        System.out.println("Неверный выбор. Попробуйте снова.");
                    }
                } catch (ClassNotFoundException e) {
                    System.err.println("Ошибка получения данных: " + e.getMessage());
                }
            }

            System.out.println("Добро пожаловать, " + currentUser.getUsername() + "!");
            System.out.println("Введите help для списка команд");

            ConsoleInputReader inputReader = new ConsoleInputReader();
            CommandHandler commandHandler = new CommandHandler(client, inputReader);
            ScriptExecutor scriptExecutor = new ScriptExecutor(client);

            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) continue;

                String[] parts = input.split("\\s+", 2);
                String command = parts[0].toUpperCase();
                String argument = parts.length > 1 ? parts[1] : null;

                try {
                    if (command.equals("EXECUTE_SCRIPT")) {
                        if (argument == null) {
                            System.out.println("Ошибка: не указан файл");
                            continue;
                        }
                        scriptExecutor.execute(argument);
                        continue;
                    }

                    boolean shouldContinue = commandHandler.handle(command, argument);
                    if (!shouldContinue) break;

                } catch (IOException e) {
                    System.err.println("Ошибка связи с сервером: " + e.getMessage());
                    break;
                } catch (ClassNotFoundException e) {
                    System.err.println("Ошибка получения данных: " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    System.err.println("Ошибка: " + e.getMessage());
                }
            }

            client.disconnect();

        } catch (IOException e) {
            System.err.println("Не удалось подключиться к серверу: " + e.getMessage());
            System.err.println("Убедитесь, что сервер запущен на " + host + ":" + port);
        }
    }

    /**
     * Выполняет вход пользователя в систему.
     *
     * @param scanner сканер для чтения ввода
     * @param client клиент для отправки запросов
     * @return объект User при успешном входе, иначе null
     * @throws IOException при ошибке сетевого соединения
     * @throws ClassNotFoundException при ошибке десериализации
     */
    private static User login(Scanner scanner, SimpleClient client) throws IOException, ClassNotFoundException {
        System.out.print("Логин: ");
        String username = scanner.nextLine().trim();
        System.out.print("Пароль: ");
        String password = scanner.nextLine().trim();

        Request request = new Request(CommandType.LOGIN, new Object[]{username, password}, null);
        client.sendRequest(request);
        Response response = client.receiveResponse();

        if (response.getStatus() == ResponseStatus.OK
                && response.getData() != null
                && !response.getData().isEmpty()
                && response.getData().get(0) instanceof User) {
            User user = (User) response.getData().get(0);
            client.setCurrentUser(user);
            return user;
        } else {
            System.err.println("Ошибка входа: " + response.getMessage());
            return null;
        }
    }

    /**
     * Регистрирует нового пользователя.
     *
     * @param scanner сканер для чтения ввода
     * @param client клиент для отправки запросов
     * @return null (после регистрации требуется отдельный вход)
     * @throws IOException при ошибке сетевого соединения
     * @throws ClassNotFoundException при ошибке десериализации
     */
    private static User register(Scanner scanner, SimpleClient client) throws IOException, ClassNotFoundException {
        System.out.print("Придумайте логин: ");
        String username = scanner.nextLine().trim();
        System.out.print("Придумайте пароль: ");
        String password = scanner.nextLine().trim();

        Request request = new Request(CommandType.REGISTER, new Object[]{username, password}, null);
        client.sendRequest(request);
        Response response = client.receiveResponse();

        if (response.getStatus() == ResponseStatus.OK) {
            System.out.println("Регистрация успешна! Теперь войдите.");
            return null;
        } else {
            System.err.println("Ошибка регистрации: " + response.getMessage());
            return null;
        }
    }
}