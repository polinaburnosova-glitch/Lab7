package client.commands;

import client.SimpleClient;
import common.network.CommandType;
import common.network.Request;
import common.network.Response;
import java.io.IOException;

/**
 * Команда EXIT - завершение работы клиента.
 *
 * <p>Отправляет серверу уведомление о завершении работы, получает
 * подтверждение, закрывает соединение и завершает клиентское приложение.</p>
 *
 * @author Полина
 * @version 1.0
 * @since 2026-05-16
 */
public class ExitCommand implements Command {

    /**
     * Выполняет команду EXIT.
     *
     * <p>Формирует запрос на сервер с типом EXIT, отправляет его и ожидает ответ.
     * После получения подтверждения закрывает сокет и выводит сообщение
     * о завершении работы клиента.</p>
     *
     * @param client клиент для отправки запроса на сервер
     * @param argument аргумент команды (не используется для EXIT)
     * @throws IOException если произошла ошибка ввода-вывода при обмене с сервером
     * @throws ClassNotFoundException если не удалось десериализовать ответ сервера
     */
    @Override
    public void execute(SimpleClient client, String argument) throws IOException, ClassNotFoundException {
        Request request = new Request(CommandType.EXIT, null, client.getCurrentUser());
        client.sendRequest(request);

        Response response = client.receiveResponse();
        System.out.println(response.getMessage());

        client.disconnect();
        System.out.println("Клиент завершил работу");
    }
}