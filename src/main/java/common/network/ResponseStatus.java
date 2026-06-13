package common.network;

import java.io.Serializable;

/**
 * Статусы ответов сервера.
 * Определяет результат выполнения команды на сервере.
 *
 * @author Полина
 * @version 1.0
 * @since 2026-05-16
 */
public enum ResponseStatus implements Serializable {
    /** Успешное выполнение команды. */
    OK,
    /** Запрашиваемый элемент не найден. */
    NOT_FOUND,
    /** Ошибка валидации данных. */
    VALIDATION_ERROR,
    /** Внутренняя ошибка сервера. */
    SERVER_ERROR,
    /** Неизвестная команда. */
    UNKNOWN_COMMAND,
    /** Предупреждение (команда выполнена с оговорками). */
    WARNING,
    /** Не авторизован (требуется вход). */
    UNAUTHORIZED,
    /** Доступ запрещён (нет прав на объект). */
    FORBIDDEN;
}