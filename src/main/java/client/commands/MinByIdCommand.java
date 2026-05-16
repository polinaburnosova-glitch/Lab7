package client.commands;

import client.SimpleClient;
import common.network.CommandType;
import common.network.Request;
import common.network.Response;
import java.io.IOException;

/**
 * Команда MIN_BY_ID - вывод элемента с минимальным ID.
 *
 * <p>Отправляет запрос на сервер для поиска и получения элемента коллекции,
 * имеющего наименьший идентификатор ID.</p>
 *
 * @author Полина
 * @version 1.0
 * @since 2026-05-16
 */
public class MinByIdCommand implements Command {

    /**
     * Выполняет команду MIN_BY_ID.
     *
     * <p>Формирует запрос на сервер с типом MIN_BY_ID и отправляет его.
     * После получения ответа выводит элемент с минимальным ID в консоль.</p>
     *
     * @param client клиент для отправки запроса на сервер
     * @param argument аргумент команды (не используется для MIN_BY_ID)
     * @throws IOException если произошла ошибка ввода-вывода при обмене с сервером
     * @throws ClassNotFoundException если не удалось десериализовать ответ сервера
     */
    @Override
    public void execute(SimpleClient client, String argument) throws IOException, ClassNotFoundException {
        Request request = new Request(CommandType.MIN_BY_ID, null, client.getCurrentUser());
        client.sendRequest(request);

        Response response = client.receiveResponse();

        if (response.isSuccess()) {
            System.out.println(response.getMessage());
        } else {
            System.err.println("Ошибка: " + response.getMessage());
        }
    }
}