package client.commands;

import client.SimpleClient;
import common.network.CommandType;
import common.network.Request;
import common.network.Response;
import java.io.IOException;
import java.util.List;

/**
 * Команда SHOW - вывод всех элементов коллекции.
 *
 * <p>Отправляет запрос на сервер для получения списка всех объектов HumanBeing,
 * хранящихся в коллекции. Элементы выводятся в отсортированном по ID порядке.</p>
 *
 * @author Полина
 * @version 1.0
 * @since 2026-05-16
 */
public class ShowCommand implements Command {

    /**
     * Выполняет команду SHOW.
     *
     * <p>Формирует запрос на сервер с типом SHOW и отправляет его.
     * При успешном ответе выводит сообщение и перебирает полученные данные,
     * выводя каждый объект HumanBeing в консоль.</p>
     *
     * @param client клиент для отправки запроса на сервер
     * @param argument аргумент команды (не используется для SHOW)
     * @throws IOException если произошла ошибка ввода-вывода при обмене с сервером
     * @throws ClassNotFoundException если не удалось десериализовать ответ сервера
     */
    @Override
    public void execute(SimpleClient client, String argument) throws IOException, ClassNotFoundException {
        Request request = new Request(CommandType.SHOW, null, client.getCurrentUser());
        client.sendRequest(request);

        Response response = client.receiveResponse();

        if (response.isSuccess()) {
            System.out.println(response.getMessage());
            if (response.getData() != null && !response.getData().isEmpty()) {
                System.out.println("\nЭлементы коллекции");
                for (Object obj : response.getData()) {
                    System.out.println(obj);
                }
            }
        } else {
            System.err.println("Ошибка: " + response.getMessage());
        }
    }
}