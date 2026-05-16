package client.commands;

import client.SimpleClient;
import common.network.CommandType;
import common.network.Request;
import common.network.Response;
import java.io.IOException;

/**
 * Команда CLEAR - удаление всех элементов коллекции, принадлежащих текущему пользователю.
 *
 * <p>Отправляет запрос на сервер на удаление всех объектов HumanBeing,
 * созданных текущим авторизованным пользователем. Объекты других пользователей
 * остаются в коллекции.</p>
 *
 * @author Полина
 * @version 1.0
 * @since 2026-05-16
 */
public class ClearCommand implements Command {

    /**
     * Выполняет команду CLEAR.
     *
     * <p>Формирует запрос на сервер с типом CLEAR и отправляет его.
     * Сервер удаляет все объекты текущего пользователя из коллекции и базы данных.
     * После получения ответа выводит результат в консоль.</p>
     *
     * @param client клиент для отправки запроса на сервер
     * @param argument аргумент команды (не используется для CLEAR)
     * @throws IOException если произошла ошибка ввода-вывода при обмене с сервером
     * @throws ClassNotFoundException если не удалось десериализовать ответ сервера
     */
    @Override
    public void execute(SimpleClient client, String argument) throws IOException, ClassNotFoundException {
        Request request = new Request(CommandType.CLEAR, null, client.getCurrentUser());
        client.sendRequest(request);

        Response response = client.receiveResponse();

        if (response.isSuccess()) {
            System.out.println(response.getMessage());
        } else {
            System.err.println("Ошибка: " + response.getMessage());
        }
    }
}