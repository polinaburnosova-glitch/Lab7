package client.script;

import client.SimpleClient;
import common.network.Request;
import common.network.Response;
import common.network.ResponseStatus;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Исполнитель скриптов.
 *
 * <p>Читает файл скрипта построчно, парсит каждую строку в команду
 * и последовательно выполняет их на сервере. Поддерживает рекурсивный
 * вызов скриптов с ограничением глубины вложенности.</p>
 *
 * @author Полина
 * @version 1.0
 * @since 2026-05-16
 */
public class ScriptExecutor {

    /** Максимальная глубина рекурсии при выполнении скриптов. */
    private static final int MAX_SCRIPT_DEPTH = 5;

    /** Текущая глубина рекурсии. */
    private static int scriptDepth = 0;

    /** Клиент для отправки запросов на сервер. */
    private final SimpleClient client;

    /**
     * Конструктор исполнителя скриптов.
     *
     * @param client клиент для отправки запросов на сервер
     */
    public ScriptExecutor(SimpleClient client) {
        this.client = client;
    }

    /**
     * Выполняет скрипт из указанного файла.
     *
     * <p>Проверяет глубину рекурсии, существование файла и права на чтение.
     * Читает файл построчно, игнорирует пустые строки и комментарии,
     * выполняет каждую команду. При обнаружении команды EXECUTE_SCRIPT
     * вызывает саму себя рекурсивно.</p>
     *
     * @param filename путь к файлу скрипта
     */
    public void execute(String filename) {
        if (scriptDepth >= MAX_SCRIPT_DEPTH) {
            System.out.println("Ошибка: превышена максимальная глубина вложенности скриптов (" + MAX_SCRIPT_DEPTH + ")");
            return;
        }

        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("Ошибка: файл '" + filename + "' не найден");
            return;
        }

        if (!file.canRead()) {
            System.out.println("Ошибка: нет прав на чтение файла '" + filename + "'");
            return;
        }

        scriptDepth++;
        System.out.println("Выполнение скрипта: " + filename + " (глубина: " + scriptDepth + ")");

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                System.out.println("\n[Скрипт:" + filename + " строка " + lineNumber + "] > " + line);

                String[] parts = line.split("\\s+", 2);
                String command = parts[0].toUpperCase();
                String argument = parts.length > 1 ? parts[1] : null;

                try {
                    if (command.equals("EXECUTE_SCRIPT")) {
                        if (argument == null) {
                            System.err.println("Ошибка: не указан файл скрипта");
                            continue;
                        }
                        execute(argument);
                        continue;
                    }

                    Request request = ScriptCommandParser.parse(command, argument);
                    client.sendRequest(request);
                    Response response = client.receiveResponse();

                    if (response.getStatus() == ResponseStatus.OK) {
                        System.out.println(response.getMessage());
                        if (command.equals("SHOW") && response.getData() != null && !response.getData().isEmpty()) {
                            System.out.println("\nЭлементы коллекции");
                            for (Object obj : response.getData()) {
                                System.out.println(obj);
                            }
                        }
                    } else {
                        System.err.println("Ошибка: " + response.getMessage());
                    }

                    if (command.equals("EXIT")) {
                        System.out.println("Клиент завершил работу");
                        break;
                    }

                } catch (IllegalArgumentException e) {
                    System.err.println("Ошибка в строке " + lineNumber + ": " + e.getMessage());
                } catch (Exception e) {
                    System.err.println("Ошибка в строке " + lineNumber + ": " + e.getMessage());
                    System.err.println("Выполнение скрипта прервано");
                    break;
                }
            }

            System.out.println("\nСкрипт '" + filename + "' успешно выполнен");

        } catch (IOException e) {
            System.out.println("Ошибка чтения файла: " + e.getMessage());
        } finally {
            scriptDepth--;
        }
    }
}