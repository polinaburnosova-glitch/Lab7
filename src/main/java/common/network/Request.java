package common.network;

import java.io.Serializable;
import java.util.Arrays;
import common.model.User;

/**
 * Класс, представляющий запрос от клиента к серверу.
 * Содержит тип команды, массив аргументов и пользователя-отправителя.
 * Объекты Request сериализуются и передаются по сети между клиентом и сервером.
 *
 * @author Полина
 * @version 2.0
 * @since 2026-05-16
 * @see CommandType
 * @see Response
 * @see common.model.HumanBeing
 */
public class Request implements Serializable {

    /** Версия для сериализации, обеспечивающая совместимость версий. */
    private static final long serialVersionUID = 2L;

    /** Тип команды, определяющий действие, которое нужно выполнить на сервере. */
    private final CommandType commandType;

    /**
     * Массив аргументов команды.
     * Содержит параметры, необходимые для выполнения команды.
     * Может быть null для команд без аргументов.
     */
    private final Object[] args;

    /** Пользователь, отправивший запрос. */
    private final User user;

    /**
     * Конструктор запроса.
     *
     * @param commandType тип команды (не может быть null)
     * @param args массив аргументов команды (может быть null)
     * @param user пользователь, отправивший запрос
     */
    public Request(CommandType commandType, Object[] args, User user) {
        this.commandType = commandType;
        this.args = args;
        this.user = user;
    }

    /**
     * Возвращает тип команды.
     *
     * @return тип команды
     */
    public CommandType getCommandType() {
        return commandType;
    }

    /**
     * Возвращает массив аргументов команды.
     *
     * @return массив аргументов или null, если аргументы отсутствуют
     */
    public Object[] getArgs() {
        return args;
    }

    /**
     * Возвращает пользователя, отправившего запрос.
     *
     * @return пользователь или null, если запрос неавторизованный
     */
    public User getUser() {
        return user;
    }

    /**
     * Возвращает строковое представление запроса.
     *
     * @return строка с типом команды, аргументами и пользователем
     */
    @Override
    public String toString() {
        return "Request(" +
                "commandType=" + commandType +
                ", args=" + Arrays.toString(args) +
                ", user=" + (user != null ? user.getUsername() : "null") +
                "}";
    }
}