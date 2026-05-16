package client.commands;

import client.SimpleClient;
import common.network.CommandType;
import common.network.Request;
import common.network.Response;
import java.io.IOException;

/**
 * Команда FILTER_STARTS_WITH_SOUNDTRACK_NAME - фильтрация элементов по префиксу названия саундтрека.
 *
 * <p>Отправляет запрос на сервер для получения всех объектов HumanBeing,
 * у которых название саундтрека (soundtrackName) начинается с указанной подстроки.</p>
 *
 * @author Полина
 * @version 1.0
 * @since 2026-05-16
 */
public class FilterBySoundtrackCommand implements Command {

    /**
     * Выполняет команду FILTER_STARTS_WITH_SOUNDTRACK_NAME.
     *
     * <p>Проверяет наличие аргумента (подстроки), формирует запрос на сервер
     * и отправляет его. При успешном выполнении выводит отфильтрованные
     * элементы коллекции.</p>
     *
     * @param client клиент для отправки запроса на сервер
     * @param argument аргумент команды (префикс названия саундтрека)
     * @throws IOException если произошла ошибка ввода-вывода при обмене с сервером
     * @throws ClassNotFoundException если не удалось десериализовать ответ сервера
     */
    @Override
    public void execute(SimpleClient client, String argument) throws IOException, ClassNotFoundException {
        if (argument == null) {
            System.out.println("Ошибка: не указана подстрока");
            return;
        }

        Request request = new Request(CommandType.FILTER_STARTS_WITH_SOUNDTRACK_NAME, new Object[]{argument}, client.getCurrentUser());
        client.sendRequest(request);

        Response response = client.receiveResponse();

        if (response.isSuccess()) {
            System.out.println(response.getMessage());
        } else {
            System.err.println("Ошибка: " + response.getMessage());
        }
    }
}