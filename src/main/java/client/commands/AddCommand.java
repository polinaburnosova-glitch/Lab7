package client.commands;

import client.SimpleClient;
import client.console.ConsoleInputReader;
import common.model.HumanBeing;
import common.network.CommandType;
import common.network.Request;
import common.network.Response;
import java.io.IOException;

/**
 * Команда ADD - добавление нового элемента в коллекцию.
 *
 * <p>Запрашивает у пользователя данные для создания нового объекта HumanBeing,
 * отправляет запрос на сервер и выводит результат выполнения.</p>
 *
 * @author Полина
 * @version 1.0
 * @since 2026-05-16
 */
public class AddCommand implements Command {

    /** Объект для интерактивного чтения данных HumanBeing с консоли. */
    private final ConsoleInputReader inputReader;

    /**
     * Конструктор команды ADD.
     *
     * @param inputReader объект для чтения данных с консоли
     */
    public AddCommand(ConsoleInputReader inputReader) {
        this.inputReader = inputReader;
    }

    /**
     * Выполняет команду ADD.
     *
     * <p>Запрашивает у пользователя ввод данных через ConsoleInputReader,
     * создаёт объект HumanBeing, формирует запрос на сервер и отправляет его.
     * После получения ответа выводит результат в консоль.</p>
     *
     * @param client клиент для отправки запроса на сервер
     * @param argument аргумент команды (не используется для ADD)
     * @throws IOException если произошла ошибка ввода-вывода при обмене с сервером
     * @throws ClassNotFoundException если не удалось десериализовать ответ сервера
     */
    @Override
    public void execute(SimpleClient client, String argument) throws IOException, ClassNotFoundException {
        System.out.println("Введите данные для нового элемента:");
        HumanBeing newHuman = inputReader.readHumanBeing();

        Request request = new Request(CommandType.ADD, new Object[]{newHuman}, client.getCurrentUser());
        client.sendRequest(request);

        Response response = client.receiveResponse();

        if (response.isSuccess()) {
            System.out.println(response.getMessage());
        } else {
            System.err.println("Ошибка: " + response.getMessage());
        }
    }
}