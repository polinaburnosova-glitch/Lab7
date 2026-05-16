package client.commands;

import client.SimpleClient;
import common.network.CommandType;
import common.network.Request;
import common.network.Response;
import java.io.IOException;

/**
 * Команда REMOVE_FIRST - удаление первого элемента коллекции.
 *
 * <p>Отправляет запрос на сервер для удаления первого элемента коллекции.
 * Удалить можно только тот элемент, который принадлежит текущему пользователю.</p>
 *
 * @author Полина
 * @version 1.0
 * @since 2026-05-16
 */
public class RemoveFirstCommand implements Command {

    /**
     * Выполняет команду REMOVE_FIRST.
     *
     * <p>Формирует запрос на сервер с типом REMOVE_FIRST и отправляет его.
     * После получения ответа выводит результат операции в консоль.</p>
     *
     * @param client клиент для отправки запроса на сервер
     * @param argument аргумент команды (не используется для REMOVE_FIRST)
     * @throws IOException если произошла ошибка ввода-вывода при обмене с сервером
     * @throws ClassNotFoundException если не удалось десериализовать ответ сервера
     */
    @Override
    public void execute(SimpleClient client, String argument) throws IOException, ClassNotFoundException {
        Request request = new Request(CommandType.REMOVE_FIRST, null, client.getCurrentUser());
        client.sendRequest(request);

        Response response = client.receiveResponse();

        if (response.isSuccess()) {
            System.out.println(response.getMessage());
        } else {
            System.err.println("Ошибка: " + response.getMessage());
        }
    }
}