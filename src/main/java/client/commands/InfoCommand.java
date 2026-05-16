package client.commands;

import client.SimpleClient;
import common.network.CommandType;
import common.network.Request;
import common.network.Response;
import java.io.IOException;

/**
 * Команда INFO - вывод информации о коллекции.
 *
 * <p>Отправляет запрос на сервер для получения информации о коллекции:
 * тип коллекции, дата инициализации, количество элементов и т.д.</p>
 *
 * @author Полина
 * @version 1.0
 * @since 2026-05-16
 */
public class InfoCommand implements Command {

    /**
     * Выполняет команду INFO.
     *
     * <p>Формирует запрос на сервер с типом INFO и отправляет его.
     * После получения ответа выводит информацию о коллекции в консоль.</p>
     *
     * @param client клиент для отправки запроса на сервер
     * @param argument аргумент команды (не используется для INFO)
     * @throws IOException если произошла ошибка ввода-вывода при обмене с сервером
     * @throws ClassNotFoundException если не удалось десериализовать ответ сервера
     */
    @Override
    public void execute(SimpleClient client, String argument) throws IOException, ClassNotFoundException {
        Request request = new Request(CommandType.INFO, null, client.getCurrentUser());
        client.sendRequest(request);

        Response response = client.receiveResponse();

        if (response.isSuccess()) {
            System.out.println(response.getMessage());
        } else {
            System.err.println("Ошибка: " + response.getMessage());
        }
    }
}