package client.commands;

import client.SimpleClient;
import client.console.ConsoleInputReader;
import common.model.HumanBeing;
import common.network.CommandType;
import common.network.Request;
import common.network.Response;
import java.io.IOException;

/**
 * Команда ADD_IF_MIN - добавление элемента, если его impactSpeed является минимальным.
 *
 * <p>Запрашивает у пользователя данные для создания нового объекта HumanBeing,
 * отправляет запрос на сервер. Сервер проверяет, является ли impactSpeed нового
 * элемента минимальным в коллекции. Если да - элемент добавляется, иначе - нет.</p>
 *
 * @author Полина
 * @version 1.0
 * @since 2026-05-16
 */
public class AddIfMinCommand implements Command {

    /** Объект для интерактивного чтения данных HumanBeing с консоли. */
    private final ConsoleInputReader inputReader;

    /**
     * Конструктор команды ADD_IF_MIN.
     *
     * @param inputReader объект для чтения данных с консоли
     */
    public AddIfMinCommand(ConsoleInputReader inputReader) {
        this.inputReader = inputReader;
    }

    /**
     * Выполняет команду ADD_IF_MIN.
     *
     * <p>Запрашивает у пользователя ввод данных через ConsoleInputReader,
     * создаёт объект HumanBeing, формирует запрос на сервер и отправляет его.
     * Сервер самостоятельно проверяет условие добавления и возвращает результат.</p>
     *
     * @param client клиент для отправки запроса на сервер
     * @param argument аргумент команды (не используется)
     * @throws IOException если произошла ошибка ввода-вывода при обмене с сервером
     * @throws ClassNotFoundException если не удалось десериализовать ответ сервера
     */
    @Override
    public void execute(SimpleClient client, String argument) throws IOException, ClassNotFoundException {
        System.out.println("Введите данные для нового элемента:");
        HumanBeing newHuman = inputReader.readHumanBeing();

        Request request = new Request(CommandType.ADD_IF_MIN, new Object[]{newHuman}, client.getCurrentUser());
        client.sendRequest(request);

        Response response = client.receiveResponse();

        if (response.isSuccess()) {
            System.out.println(response.getMessage());
        } else {
            System.err.println("Ошибка: " + response.getMessage());
        }
    }
}