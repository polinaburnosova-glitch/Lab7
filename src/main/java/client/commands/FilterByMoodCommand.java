package client.commands;

import client.SimpleClient;
import common.model.Mood;
import common.network.CommandType;
import common.network.Request;
import common.network.Response;
import java.io.IOException;

/**
 * Команда FILTER_BY_MOOD - фильтрация элементов коллекции по настроению.
 *
 * <p>Отправляет запрос на сервер для получения всех объектов HumanBeing,
 * у которых настроение (mood) совпадает с указанным.</p>
 *
 * @author Полина
 * @version 1.0
 * @since 2026-05-16
 */
public class FilterByMoodCommand implements Command {

    /**
     * Выполняет команду FILTER_BY_MOOD.
     *
     * <p>Проверяет наличие аргумента с настроением, преобразует его в enum Mood,
     * формирует запрос на сервер и отправляет его. При успешном выполнении
     * выводит отфильтрованные элементы коллекции.</p>
     *
     * @param client клиент для отправки запроса на сервер
     * @param argument аргумент команды (настроение: SORROW, LONGING, GLOOM, APATHY)
     * @throws IOException если произошла ошибка ввода-вывода при обмене с сервером
     * @throws ClassNotFoundException если не удалось десериализовать ответ сервера
     */
    @Override
    public void execute(SimpleClient client, String argument) throws IOException, ClassNotFoundException {
        if (argument == null) {
            System.out.println("Ошибка: не указано настроение. Доступные: SORROW, LONGING, GLOOM, APATHY");
            return;
        }

        try {
            Mood mood = Mood.valueOf(argument.toUpperCase());
            Request request = new Request(CommandType.FILTER_BY_MOOD, new Object[]{mood}, client.getCurrentUser());
            client.sendRequest(request);

            Response response = client.receiveResponse();

            if (response.isSuccess()) {
                System.out.println(response.getMessage());
            } else {
                System.err.println("Ошибка: " + response.getMessage());
            }

        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: неверное настроение. Доступные: SORROW, LONGING, GLOOM, APATHY");
        }
    }
}