package client.commands;

import client.SimpleClient;
import common.network.CommandType;
import common.network.Request;
import common.network.Response;
import java.io.IOException;

/**
 * Команда REMOVE_BY_ID - удаление элемента коллекции по идентификатору.
 *
 * <p>Отправляет запрос на сервер для удаления объекта HumanBeing с указанным ID.
 * Удалить можно только те элементы, которые принадлежат текущему пользователю.</p>
 *
 * @author Полина
 * @version 1.0
 * @since 2026-05-16
 */
public class RemoveByIdCommand implements Command {

    /**
     * Выполняет команду REMOVE_BY_ID.
     *
     * <p>Проверяет наличие аргумента ID, преобразует его в число,
     * формирует запрос на сервер и отправляет его. При успешном удалении
     * выводит подтверждение, иначе - сообщение об ошибке.</p>
     *
     * @param client клиент для отправки запроса на сервер
     * @param argument аргумент команды (ID удаляемого элемента)
     * @throws IOException если произошла ошибка ввода-вывода при обмене с сервером
     * @throws ClassNotFoundException если не удалось десериализовать ответ сервера
     */
    @Override
    public void execute(SimpleClient client, String argument) throws IOException, ClassNotFoundException {
        if (argument == null) {
            System.out.println("Ошибка: не указан ID. Пример: remove_by_id 5");
            return;
        }

        try {
            long id = Long.parseLong(argument);
            Request request = new Request(CommandType.REMOVE_BY_ID, new Object[]{id}, client.getCurrentUser());
            client.sendRequest(request);

            Response response = client.receiveResponse();

            if (response.isSuccess()) {
                System.out.println(response.getMessage());
            } else {
                System.err.println("Ошибка: " + response.getMessage());
            }

        } catch (NumberFormatException e) {
            System.out.println("Ошибка: ID должен быть числом");
        }
    }
}