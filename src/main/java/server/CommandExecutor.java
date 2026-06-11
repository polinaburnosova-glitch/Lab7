package server;

import common.model.HumanBeing;
import common.model.Mood;
import common.model.User;
import common.network.*;
import server.database.UserDAO;
import server.manager.CollectionManager;
import java.util.Collections;
import java.util.Deque;

/**
 * Исполнитель команд на сервере.
 * Получает запрос от клиента, выполняет соответствующую операцию
 * над коллекцией через CollectionManager и формирует ответ.
 *
 * @author Полина
 * @version 2.0
 * @since 2026-05-16
 */
public class CommandExecutor {

    /** Менеджер коллекции для выполнения операций. */
    private final CollectionManager collectionManager;

    /**
     * Конструктор исполнителя команд.
     *
     * @param collectionManager менеджер коллекции
     */
    public CommandExecutor(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    /**
     * Выполняет команду из запроса и возвращает ответ.
     *
     * @param request запрос от клиента
     * @return ответ сервера
     */
    public Response execute(Request request) {
        CommandType type = request.getCommandType();
        Object[] args = request.getArgs();
        User user = request.getUser();

        if (user == null && type != CommandType.LOGIN && type != CommandType.REGISTER) {
            return new Response(ResponseStatus.UNAUTHORIZED, "Необходимо авторизоваться");
        }

        try {
            switch (type) {
                case LOGIN:
                    return handleLogin(args);

                case REGISTER:
                    return handleRegister(args);

                case INFO:
                    return ok(collectionManager.info());

                case SHOW:
                    return new Response(ResponseStatus.OK, collectionManager.show(),
                            collectionManager.getCollection().stream().collect(java.util.stream.Collectors.toList()));

                case HELP:
                    return ok(collectionManager.help());

                case EXIT:
                    return ok("До свидания!");

                case ADD:
                    HumanBeing newHuman = getArg(args, 0, HumanBeing.class);
                    if (newHuman == null) {
                        return validationError("не указан объект для добавления");
                    }
                    boolean added = collectionManager.add(newHuman, user.getUsername());
                    if (added) {
                        return ok("Добавлен элемент с ID: " + newHuman.getId());
                    } else {
                        return serverError("Ошибка сохранения в БД");
                    }

                case ADD_IF_MIN:
                    HumanBeing minHuman = getArg(args, 0, HumanBeing.class);
                    if (minHuman == null) {
                        return validationError("не указан объект для добавления");
                    }
                    String minResult = collectionManager.addIfMin(minHuman, user.getUsername());
                    if (minResult.contains("не добавлен")) {
                        return warning(minResult);
                    }
                    return ok(minResult);

                case ADD_IF_MAX:
                    HumanBeing maxHuman = getArg(args, 0, HumanBeing.class);
                    if (maxHuman == null) {
                        return validationError("не указан объект для добавления");
                    }
                    String maxResult = collectionManager.addIfMax(maxHuman, user.getUsername());
                    if (maxResult.contains("не добавлен")) {
                        return warning(maxResult);
                    }
                    return ok(maxResult);

                case MIN_BY_ID:
                    return ok(collectionManager.minById());

                case CHECK_OWNERSHIP:
                    return handleCheckOwnership(args, user);

                case REMOVE_BY_ID:
                    Long idToRemove = getArg(args, 0, Long.class);
                    if (idToRemove == null) {
                        return validationError("не указан id для удаления");
                    }
                    Response removeAccess = verifyOwnershipAccess(
                            idToRemove, user.getUsername(), "Нет прав на удаление этого объекта");
                    if (removeAccess != null) {
                        return removeAccess;
                    }
                    boolean deleted = collectionManager.removeById(idToRemove, user.getUsername());
                    if (deleted) {
                        return ok("Элемент с ID " + idToRemove + " удалён");
                    } else {
                        return notFound("Элемент с ID " + idToRemove + " не найден");
                    }

                case CLEAR:
                    return ok(collectionManager.clear(user.getUsername()));

                case REMOVE_FIRST:
                    return ok(collectionManager.removeFirst());

                case UPDATE:
                    Long updateId = getArg(args, 0, Long.class);
                    if (updateId == null) {
                        return validationError("не указан id для обновления");
                    }
                    Response updateAccess = verifyOwnershipAccess(
                            updateId, user.getUsername(), "Нет прав на изменение этого объекта");
                    if (updateAccess != null) {
                        return updateAccess;
                    }
                    HumanBeing updateHuman = getArg(args, 1, HumanBeing.class);
                    if (updateHuman == null) {
                        return validationError("не указан объект для обновления");
                    }
                    boolean updated = collectionManager.update(updateId, updateHuman, user.getUsername());
                    if (updated) {
                        return ok("Элемент с ID " + updateId + " успешно обновлён");
                    } else {
                        return notFound("Элемент с ID " + updateId + " не найден");
                    }

                case FILTER_BY_MOOD:
                    Mood mood = getArg(args, 0, Mood.class);
                    if (mood == null) {
                        return validationError("не указано настроение для фильтрации");
                    }
                    Deque<HumanBeing> filteredByMood = collectionManager.filterByMood(mood);
                    if (filteredByMood.isEmpty()) {
                        return ok("Нет элементов с настроением " + mood);
                    }
                    StringBuilder moodResult = new StringBuilder();
                    for (HumanBeing h : filteredByMood) {
                        moodResult.append(h).append("\n\n");
                    }
                    return ok(moodResult.toString().trim());

                case FILTER_STARTS_WITH_SOUNDTRACK_NAME:
                    String prefix = getArg(args, 0, String.class);
                    if (prefix == null) {
                        return validationError("не указана подстрока для фильтрации");
                    }
                    Deque<HumanBeing> filteredBySoundtrack = collectionManager.filterBySoundtrack(prefix);
                    if (filteredBySoundtrack.isEmpty()) {
                        return ok("Нет элементов с саундтреком, начинающимся на '" + prefix + "'");
                    }
                    StringBuilder soundtrackResult = new StringBuilder();
                    for (HumanBeing h : filteredBySoundtrack) {
                        soundtrackResult.append(h).append("\n\n");
                    }
                    return ok(soundtrackResult.toString().trim());

                case EXECUTE_SCRIPT:
                    return ok("Команда выполнена");

                case ATTACK:
                    Long attackerId = getArg(args, 0, Long.class);
                    Long defenderId = getArg(args, 1, Long.class);
                    if (attackerId == null || defenderId == null) {
                        return validationError("не указаны ID атакующего и защитника");
                    }

                    if (!collectionManager.existsAndOwnedBy(attackerId, user.getUsername())) {
                        return new Response(ResponseStatus.FORBIDDEN, "Можно атаковать только своим героем");
                    }

                    HumanBeing attacker = collectionManager.findById(attackerId);
                    HumanBeing defender = collectionManager.findById(defenderId);

                    if (attacker == null || defender == null) {
                        return notFound("Герой не найден");
                    }

                    int attackerPower = (int) attacker.getImpactSpeed();
                    int defenderPower = (int) defender.getImpactSpeed();

                    double chance = (double) attackerPower / (attackerPower + defenderPower);
                    boolean attackerWins = Math.random() < chance;

                    if (attackerWins) {
                        attacker.setImpactSpeed(attackerPower + 10);
                        defender.setImpactSpeed(Math.max(1, defenderPower / 2));
                    } else {
                        defender.setImpactSpeed(defenderPower + 10);
                        attacker.setImpactSpeed(Math.max(1, attackerPower / 2));
                    }
                    collectionManager.update(attacker, attacker.getOwner());
                    collectionManager.update(defender, defender.getOwner());

                    return ok("Битва окончена! " + attacker.getName() + " " +
                            (attackerWins ? "победил" : "проиграл") +
                            ". Новые силы: " + attacker.getName() + "=" + attacker.getImpactSpeed() +
                            ", " + defender.getName() + "=" + defender.getImpactSpeed());

                default:
                    return unknownCommand(type);
            }
        } catch (Exception e) {
            return serverError("Ошибка выполнения: " + e.getMessage());
        }
    }

    /**
     * Обрабатывает команду LOGIN.
     *
     * @param args аргументы команды
     * @return ответ с объектом User при успехе
     */
    private Response handleLogin(Object[] args) {
        if (args == null || args.length < 2) {
            return validationError("Не указан логин или пароль");
        }
        String username = (String) args[0];
        String password = (String) args[1];

        User user = UserDAO.login(username, password);
        if (user == null) {
            System.out.println("Результат: пользователь НЕ найден");
            return new Response(ResponseStatus.UNAUTHORIZED, "Неверный логин или пароль");
        }
        return new Response(ResponseStatus.OK, "Авторизация успешна", Collections.singletonList(user));
    }

    /**
     * Обрабатывает команду REGISTER.
     *
     * @param args аргументы команды
     * @return ответ о результате регистрации
     */
    private Response handleRegister(Object[] args) {
        if (args == null || args.length < 2) {
            return validationError("Не указан логин или пароль");
        }
        String username = (String) args[0];
        String password = (String) args[1];

        if (UserDAO.userExists(username)) {
            return new Response(ResponseStatus.VALIDATION_ERROR, "Пользователь уже существует");
        }

        boolean registered = UserDAO.register(username, password);
        if (!registered) {
            return new Response(ResponseStatus.SERVER_ERROR, "Ошибка регистрации");
        }
        return ok("Регистрация успешна. Теперь войдите через LOGIN");
    }

    /**
     * Безопасно извлекает аргумент из массива по индексу.
     *
     * @param args массив аргументов
     * @param index индекс аргумента
     * @param type ожидаемый тип
     * @param <T> тип аргумента
     * @return аргумент или null
     */
    private Response handleCheckOwnership(Object[] args, User user) {
        Long id = getArg(args, 0, Long.class);
        String forbiddenMessage = getArg(args, 1, String.class);
        if (forbiddenMessage == null) {
            forbiddenMessage = "Нет прав на этот объект";
        }
        Response denied = verifyOwnershipAccess(id, user.getUsername(), forbiddenMessage);
        return denied != null ? denied : ok("");
    }

    /**
     * @return ответ с ошибкой или {@code null}, если доступ разрешён
     */
    private Response verifyOwnershipAccess(Long id, String username, String forbiddenMessage) {
        if (id == null) {
            return validationError("не указан id");
        }
        if (!collectionManager.existsById(id)) {
            return notFound("Элемент с ID " + id + " не найден");
        }
        if (!collectionManager.existsAndOwnedBy(id, username)) {
            return new Response(ResponseStatus.FORBIDDEN, forbiddenMessage);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> T getArg(Object[] args, int index, Class<T> type) {
        if (args != null && args.length > index && type.isInstance(args[index])) {
            return (T) args[index];
        }
        return null;
    }

    /**
     * Создаёт успешный ответ.
     *
     * @param message текст сообщения
     * @return объект Response
     */
    private Response ok(String message) {
        return new Response(ResponseStatus.OK, message);
    }

    /**
     * Создаёт ответ-предупреждение.
     *
     * @param message текст сообщения
     * @return объект Response
     */
    private Response warning(String message) {
        return new Response(ResponseStatus.WARNING, message);
    }

    /**
     * Создаёт ответ "не найдено".
     *
     * @param message текст сообщения
     * @return объект Response
     */
    private Response notFound(String message) {
        return new Response(ResponseStatus.NOT_FOUND, message);
    }

    /**
     * Создаёт ответ с ошибкой валидации.
     *
     * @param message текст сообщения
     * @return объект Response
     */
    private Response validationError(String message) {
        return new Response(ResponseStatus.VALIDATION_ERROR, "Ошибка: " + message);
    }

    /**
     * Создаёт ответ с ошибкой сервера.
     *
     * @param message текст сообщения
     * @return объект Response
     */
    private Response serverError(String message) {
        return new Response(ResponseStatus.SERVER_ERROR, message);
    }

    /**
     * Создаёт ответ "неизвестная команда".
     *
     * @param type тип неизвестной команды
     * @return объект Response
     */
    private Response unknownCommand(CommandType type) {
        return new Response(ResponseStatus.UNKNOWN_COMMAND, "Команда не реализована: " + type);
    }
}