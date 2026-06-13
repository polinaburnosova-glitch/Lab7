package common.network;

import java.io.Serializable;
import java.util.List;

/**
 * Класс, представляющий ответ от сервера клиенту.
 * Содержит результат выполнения команды на сервере: статус,
 * текстовое сообщение и опциональные данные (например, коллекцию объектов
 * для команды SHOW). Объекты Response сериализуются и передаются по сети.
 *
 * @author Полина
 * @version 2.0
 * @since 2026-05-16
 * @see Request
 * @see common.model.HumanBeing
 */
public class Response implements Serializable {

    /** Версия для сериализации, обеспечивающая совместимость версий. */
    private static final long serialVersionUID = 2L;

    /** Статус ответа. */
    private final ResponseStatus status;

    /** Текстовое сообщение с результатом выполнения или описанием ошибки. */
    private final String message;

    /**
     * Дополнительные данные, передаваемые клиенту.
     * Используется для команды SHOW для передачи отсортированной коллекции.
     * Может быть null, если данные не требуются.
     */
    private final List<?> data;

    /**
     * Конструктор ответа с данными.
     *
     * @param status статус выполнения команды
     * @param message текстовое сообщение о результате
     * @param data список данных (например, коллекция объектов для SHOW)
     */
    public Response(ResponseStatus status, String message, List<?> data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    /**
     * Конструктор ответа без данных.
     *
     * @param status статус выполнения команды
     * @param message текстовое сообщение о результате
     */
    public Response(ResponseStatus status, String message) {
        this(status, message, null);
    }

    /**
     * Проверяет, успешно ли выполнена команда.
     *
     * @return true, если команда выполнена успешно; false в случае ошибки
     */
    public boolean isSuccess() {
        return status == ResponseStatus.OK;
    }

    /**
     * Возвращает статус ответа.
     *
     * @return статус ответа
     */
    public ResponseStatus getStatus() {
        return status;
    }

    /**
     * Возвращает текстовое сообщение ответа.
     *
     * @return сообщение о результате выполнения команды
     */
    public String getMessage() {
        return message;
    }

    /**
     * Возвращает дополнительные данные ответа.
     *
     * @return список данных (обычно коллекция HumanBeing) или null, если данных нет
     */
    public List<?> getData() {
        return data;
    }
}