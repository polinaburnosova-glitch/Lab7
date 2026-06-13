package common.model;

import java.io.Serializable;

/**
 * Класс, представляющий пользователя системы.
 * Содержит логин и хэш пароля для аутентификации.
 *
 * @author Полина
 * @version 1.0
 * @since 2026-05-16
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String username;
    private final String passwordHash;

    /**
     * Конструктор пользователя.
     *
     * @param username логин пользователя
     * @param passwordHash хэш пароля
     */
    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }

    /**
     * Возвращает логин пользователя.
     *
     * @return логин
     */
    public String getUsername() {
        return username;
    }

    /**
     * Возвращает хэш пароля.
     *
     * @return хэш пароля
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Сравнивает двух пользователей по логину.
     *
     * @param obj объект для сравнения
     * @return true если логины совпадают
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return username.equals(user.username);
    }

    /**
     * Возвращает хэш-код на основе логина.
     *
     * @return хэш-код
     */
    @Override
    public int hashCode() {
        return username.hashCode();
    }
}