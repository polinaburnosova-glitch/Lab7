package client.commands;

import client.SimpleClient;
import client.console.ConsoleInputReader;
import common.model.HumanBeing;
import common.network.CommandType;
import common.network.Request;
import common.network.Response;
import java.io.IOException;

/**
 * Команда UPDATE - обновление существующего элемента коллекции.
 *
 * <p>Запрашивает у пользователя новые данные для объекта HumanBeing с указанным ID,
 * отправляет запрос на сервер для обновления. Обновить можно только те элементы,
 * которые принадлежат текущему пользователю.</p>
 *
 * @author Полина
 * @version 1.0
 * @since 2026-05-16
 */
public class UpdateCommand implements Command {

    /** Объект для интерактивного чтения данных HumanBeing с консоли. */
    private final ConsoleInputReader inputReader;

    /**
     * Конструктор команды UPDATE.
     *
     * @param inputReader объект для чтения данных с консоли
     */
    public UpdateCommand(ConsoleInputReader inputReader) {
        this.inputReader = inputReader;
    }

    /**
     * Выполняет команду UPDATE.
     *
     * <p>Проверяет наличие аргумента ID, преобразует его в число,
     * запрашивает у пользователя новые данные через ConsoleInputReader,
     * создаёт объект HumanBeing, формирует запрос на сервер и отправляет его.
     * После получения ответа выводит результат в консоль.</p>
     *
     * @param client клиент для отправки запроса на сервер
     * @param argument аргумент команды (ID обновляемого элемента)
     * @throws IOException если произошла ошибка ввода-вывода при обмене с сервером
     * @throws ClassNotFoundException если не удалось десериализовать ответ сервера
     */
    @Override
    public void execute(SimpleClient client, String argument) throws IOException, ClassNotFoundException {
        if (argument == null) {
            System.out.println("Ошибка: не указан ID. Пример: update 5");
            return;
        }

        try {
            long id = Long.parseLong(argument);
            System.out.println("Введите новые данные для элемента с ID " + id + ":");
            HumanBeing updatedHuman = inputReader.readHumanBeing();

            Request request = new Request(CommandType.UPDATE, new Object[]{id, updatedHuman}, client.getCurrentUser());
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