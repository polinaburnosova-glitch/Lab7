package client.commands;

import client.SimpleClient;
import java.io.IOException;

/**
 * Интерфейс для всех команд клиентского приложения.
 *
 * <p>Реализует паттерн Command, позволяя инкапсулировать запросы
 * к серверу в виде отдельных объектов. Каждая конкретная команда
 * (ADD, UPDATE, SHOW и т.д.) реализует этот интерфейс.</p>
 *
 * @author Полина
 * @version 1.0
 * @since 2026-05-16
 * @see AddCommand
 * @see ShowCommand
 * @see UpdateCommand
 */
public interface Command {

    /**
     * Выполняет команду.
     *
     * <p>Отправляет соответствующий запрос на сервер и обрабатывает ответ.
     * Каждая конкретная реализация определяет свою логику формирования запроса
     * и обработки результата.</p>
     *
     * @param client клиент для отправки запроса на сервер
     * @param argument строковый аргумент команды (может быть null)
     * @throws IOException если произошла ошибка ввода-вывода при обмене с сервером
     * @throws ClassNotFoundException если не удалось десериализовать ответ сервера
     */
    void execute(SimpleClient client, String argument) throws IOException, ClassNotFoundException;
}