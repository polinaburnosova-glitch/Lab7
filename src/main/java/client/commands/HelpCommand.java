package client.commands;

import client.SimpleClient;
import common.network.CommandType;
import common.network.Request;
import common.network.Response;
import java.io.IOException;

/**
 * Команда HELP - вывод справки по всем доступным командам.
 *
 * <p>Отправляет запрос на сервер для получения списка всех доступных
 * команд с кратким описанием их функционала.</p>
 *
 * @author Полина
 * @version 1.0
 * @since 2026-05-16
 */
public class HelpCommand implements Command {

    /**
     * Выполняет команду HELP.
     *
     * <p>Формирует запрос на сервер с типом HELP и отправляет его.
     * После получения ответа выводит полученную справку в консоль.</p>
     *
     * @param client клиент для отправки запроса на сервер
     * @param argument аргумент команды (не используется для HELP)
     * @throws IOException если произошла ошибка ввода-вывода при обмене с сервером
     * @throws ClassNotFoundException если не удалось десериализовать ответ сервера
     */
    @Override
    public void execute(SimpleClient client, String argument) throws IOException, ClassNotFoundException {
        Request request = new Request(CommandType.HELP, null, client.getCurrentUser());
        client.sendRequest(request);

        Response response = client.receiveResponse();

        if (response.isSuccess()) {
            System.out.println(response.getMessage());
        } else {
            System.err.println("Ошибка: " + response.getMessage());
        }
    }
}