package client.commands;

import client.SimpleClient;
import client.console.ConsoleInputReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Обработчик команд клиента.
 *
 * <p>Реализует паттерн Command, храня карту соответствия между строковыми
 * именами команд и их объектами-обработчиками. Выбирает нужную команду
 * и делегирует ей выполнение.</p>
 *
 * @author Полина
 * @version 1.0
 * @since 2026-05-16
 * @see Command
 */
public class CommandHandler {

    /** Карта соответствия: имя команды → объект команды. */
    private final Map<String, Command> commands = new HashMap<>();

    /** Клиент для отправки запросов на сервер. */
    private final SimpleClient client;

    /**
     * Конструктор обработчика команд.
     *
     * <p>Инициализирует карту команд, регистрируя все доступные команды
     * с их обработчиками. Командам, требующим интерактивного ввода,
     * передаётся ссылка на ConsoleInputReader.</p>
     *
     * @param client клиент для отправки запросов на сервер
     * @param inputReader объект для чтения данных с консоли
     */
    public CommandHandler(SimpleClient client, ConsoleInputReader inputReader) {
        this.client = client;

        commands.put("ADD", new AddCommand(inputReader));
        commands.put("ADD_IF_MIN", new AddIfMinCommand(inputReader));
        commands.put("ADD_IF_MAX", new AddIfMaxCommand(inputReader));
        commands.put("UPDATE", new UpdateCommand(inputReader));
        commands.put("REMOVE_BY_ID", new RemoveByIdCommand());
        commands.put("FILTER_BY_MOOD", new FilterByMoodCommand());
        commands.put("FILTER_STARTS_WITH_SOUNDTRACK_NAME", new FilterBySoundtrackCommand());
        commands.put("SHOW", new ShowCommand());
        commands.put("INFO", new InfoCommand());
        commands.put("HELP", new HelpCommand());
        commands.put("MIN_BY_ID", new MinByIdCommand());
        commands.put("CLEAR", new ClearCommand());
        commands.put("REMOVE_FIRST", new RemoveFirstCommand());
        commands.put("EXIT", new ExitCommand());
    }

    /**
     * Обрабатывает команду, введённую пользователем.
     *
     * <p>Находит соответствующий объект команды в карте по имени.
     * Если команда найдена — выполняет её. Если нет — выводит сообщение
     * о неизвестной команде.</p>
     *
     * @param command имя команды (в верхнем регистре)
     * @param argument строковый аргумент команды
     * @return true, если нужно продолжить работу клиента; false, если
     *         команда EXIT (клиент должен завершить работу)
     * @throws IOException если произошла ошибка ввода-вывода при обмене с сервером
     * @throws ClassNotFoundException если не удалось десериализовать ответ сервера
     */
    public boolean handle(String command, String argument) throws IOException, ClassNotFoundException {
        Command cmd = commands.get(command);

        if (cmd == null) {
            System.out.println("Неизвестная команда. Введите help");
            return true;
        }

        cmd.execute(client, argument);

        return !command.equals("EXIT");
    }
}