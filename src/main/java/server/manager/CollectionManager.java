package server.manager;

import common.model.HumanBeing;
import common.model.Mood;
import server.database.HumanBeingDAO;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

/**
 * Менеджер коллекции объектов HumanBeing на сервере.
 * Обеспечивает хранение, управление и операции над коллекцией.
 * Все операции синхронизированы с помощью ReentrantReadWriteLock.
 *
 * @author Полина
 * @version 2.0
 * @since 2026-05-16
 */
public class CollectionManager {

    private final Deque<HumanBeing> collection = new ArrayDeque<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Загружает все объекты из базы данных в коллекцию в памяти.
     */
    public void loadFromDatabase() {
        lock.writeLock().lock();
        try {
            collection.clear();
            collection.addAll(HumanBeingDAO.loadAll());
            System.out.println("Загружено " + collection.size() + " объектов из БД");
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Добавляет новый объект в коллекцию.
     *
     * @param human объект для добавления
     * @param ownerUsername имя владельца
     * @return true если добавление успешно
     */
    public boolean add(HumanBeing human, String ownerUsername) {
        human.setCreationDate(LocalDateTime.now());
        boolean saved = HumanBeingDAO.save(human, ownerUsername);
        if (!saved) return false;

        lock.writeLock().lock();
        try {
            collection.add(human);
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Обновляет существующий объект в коллекции.
     *
     * @param human объект с новыми данными
     * @param ownerUsername имя владельца
     * @return true если обновление успешно
     */
    public boolean update(HumanBeing human, String ownerUsername) {
        if (!HumanBeingDAO.existsAndOwnedBy(human.getId(), ownerUsername)) {
            return false;
        }

        boolean updated = HumanBeingDAO.update(human, ownerUsername);
        if (!updated) return false;

        lock.writeLock().lock();
        try {
            collection.removeIf(h -> h.getId().equals(human.getId()));
            collection.add(human);
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Удаляет объект по ID.
     *
     * @param id ID удаляемого объекта
     * @param ownerUsername имя владельца
     * @return true если удаление успешно
     */
    public boolean removeById(long id, String ownerUsername) {
        if (!HumanBeingDAO.existsAndOwnedBy(id, ownerUsername)) {
            return false;
        }

        boolean deleted = HumanBeingDAO.delete(id, ownerUsername);
        if (!deleted) return false;

        lock.writeLock().lock();
        try {
            collection.removeIf(h -> h.getId() == id);
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Удаляет все объекты, принадлежащие указанному пользователю.
     *
     * @param ownerUsername имя владельца
     */
    public void clearByOwner(String ownerUsername) {
        lock.writeLock().lock();
        try {
            collection.removeIf(h -> h.getOwner().equals(ownerUsername));
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Возвращает копию коллекции.
     *
     * @return копия коллекции
     */
    public Deque<HumanBeing> getCollection() {
        lock.readLock().lock();
        try {
            return new ArrayDeque<>(collection);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Возвращает размер коллекции.
     *
     * @return количество элементов
     */
    public int size() {
        lock.readLock().lock();
        try {
            return collection.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Проверяет, пуста ли коллекция.
     *
     * @return true если пуста
     */
    public boolean isEmpty() {
        lock.readLock().lock();
        try {
            return collection.isEmpty();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Находит элемент с минимальным ID.
     *
     * @return элемент с минимальным ID или null
     */
    public HumanBeing findMinById() {
        lock.readLock().lock();
        try {
            return collection.stream()
                    .min(HumanBeing::compareTo)
                    .orElse(null);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Проверяет, является ли impactSpeed минимальным.
     *
     * @param impactSpeed проверяемое значение
     * @return true если минимальное
     */
    public boolean isImpactSpeedMin(float impactSpeed) {
        lock.readLock().lock();
        try {
            return collection.stream()
                    .mapToDouble(HumanBeing::getImpactSpeed)
                    .min()
                    .orElse(Double.MAX_VALUE) >= impactSpeed;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Проверяет, является ли impactSpeed максимальным.
     *
     * @param impactSpeed проверяемое значение
     * @return true если максимальное
     */
    public boolean isImpactSpeedMax(float impactSpeed) {
        lock.readLock().lock();
        try {
            return collection.stream()
                    .mapToDouble(HumanBeing::getImpactSpeed)
                    .max()
                    .orElse(-Double.MAX_VALUE) <= impactSpeed;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Проверяет, существует ли объект с ID и принадлежит ли пользователю.
     *
     * @param id ID объекта
     * @param ownerUsername имя владельца
     * @return true если существует и принадлежит
     */
    public boolean existsById(long id) {
        lock.readLock().lock();
        try {
            return collection.stream().anyMatch(h -> h.getId() == id);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean existsAndOwnedBy(long id, String ownerUsername) {
        lock.readLock().lock();
        try {
            return collection.stream()
                    .anyMatch(h -> h.getId() == id && h.getOwner().equals(ownerUsername));
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Возвращает первый элемент, принадлежащий пользователю.
     *
     * @param ownerUsername имя владельца
     * @return первый элемент или null
     */
    public HumanBeing getFirstOwnedBy(String ownerUsername) {
        lock.readLock().lock();
        try {
            return collection.stream()
                    .filter(h -> h.getOwner().equals(ownerUsername))
                    .findFirst()
                    .orElse(null);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Фильтрует коллекцию по настроению.
     *
     * @param mood настроение
     * @return отфильтрованная коллекция
     */
    public Deque<HumanBeing> filterByMood(Mood mood) {
        lock.readLock().lock();
        try {
            return collection.stream()
                    .filter(h -> h.getMood() == mood)
                    .sorted()
                    .collect(Collectors.toCollection(ArrayDeque::new));
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Фильтрует коллекцию по префиксу названия саундтрека.
     *
     * @param prefix префикс
     * @return отфильтрованная коллекция
     */
    public Deque<HumanBeing> filterBySoundtrack(String prefix) {
        lock.readLock().lock();
        try {
            return collection.stream()
                    .filter(h -> h.getSoundtrackName().startsWith(prefix))
                    .sorted()
                    .collect(Collectors.toCollection(ArrayDeque::new));
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Возвращает информацию о коллекции.
     *
     * @return строка с информацией
     */
    public String info() {
        lock.readLock().lock();
        try {
            return "Тип коллекции: " + collection.getClass().getName() +
                    "\nКоличество элементов: " + collection.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Возвращает строковое представление всех элементов.
     *
     * @return строка с элементами
     */
    public String show() {
        lock.readLock().lock();
        try {
            if (collection.isEmpty()) {
                return "Коллекция пуста";
            }
            return collection.stream()
                    .sorted()
                    .map(HumanBeing::toString)
                    .collect(Collectors.joining("\n\n"));
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Возвращает справку по командам.
     *
     * @return строка со справкой
     */
    public String help() {
        return "Доступные команды:\n" +
                "  help - показать справку\n" +
                "  info - информация о коллекции\n" +
                "  show - показать все элементы\n" +
                "  add - добавить новый элемент\n" +
                "  add_if_min - добавить элемент, если его impactSpeed минимальный\n" +
                "  add_if_max - добавить элемент, если его impactSpeed максимальный\n" +
                "  min_by_id - показать элемент с минимальным ID\n" +
                "  remove_by_id <id> - удалить элемент по ID\n" +
                "  clear - очистить коллекцию (только свои элементы)\n" +
                "  remove_first - удалить первый элемент\n" +
                "  update <id> - обновить элемент\n" +
                "  filter_by_mood <mood> - фильтр по настроению\n" +
                "  filter_starts_with_soundtrack_name <prefix> - фильтр по саундтреку\n" +
                "  execute_script <file> - выполнить скрипт\n" +
                "  exit - завершить работу клиента";
    }

    /**
     * Возвращает элемент с минимальным ID в виде строки.
     *
     * @return строка с элементом
     */
    public String minById() {
        HumanBeing min = findMinById();
        if (min == null) {
            return "Коллекция пуста";
        }
        return "Элемент с минимальным ID (" + min.getId() + "):\n" + min;
    }

    /**
     * Удаляет первый элемент коллекции.
     *
     * @return сообщение о результате
     */
    public String removeFirst() {
        lock.writeLock().lock();
        try {
            if (collection.isEmpty()) {
                return "Коллекция пуста";
            }
            HumanBeing removed = collection.removeFirst();
            return "Удалён первый элемент:\n" + removed;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Добавляет элемент, если его impactSpeed минимальный.
     *
     * @param human объект для добавления
     * @param ownerUsername имя владельца
     * @return сообщение о результате
     */
    public String addIfMin(HumanBeing human, String ownerUsername) {
        if (isImpactSpeedMin(human.getImpactSpeed())) {
            boolean added = add(human, ownerUsername);
            if (added) {
                return "Добавлен элемент с минимальным impactSpeed, ID: " + human.getId();
            }
            return "Ошибка при добавлении элемента";
        }
        return "Элемент не добавлен: impactSpeed не является минимальным";
    }

    /**
     * Добавляет элемент, если его impactSpeed максимальный.
     *
     * @param human объект для добавления
     * @param ownerUsername имя владельца
     * @return сообщение о результате
     */
    public String addIfMax(HumanBeing human, String ownerUsername) {
        if (isImpactSpeedMax(human.getImpactSpeed())) {
            boolean added = add(human, ownerUsername);
            if (added) {
                return "Добавлен элемент с максимальным impactSpeed, ID: " + human.getId();
            }
            return "Ошибка при добавлении элемента";
        }
        return "Элемент не добавлен: impactSpeed не является максимальным";
    }

    /**
     * Удаляет все элементы текущего пользователя.
     *
     * @param ownerUsername имя владельца
     * @return сообщение о результате
     */
    public String clear(String ownerUsername) {
        lock.writeLock().lock();
        try {
            int removed = (int) collection.stream()
                    .filter(h -> h.getOwner().equals(ownerUsername))
                    .count();
            collection.removeIf(h -> h.getOwner().equals(ownerUsername));
            return "Удалено элементов: " + removed;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Обновляет объект по ID.
     *
     * @param id ID объекта
     * @param human объект с новыми данными
     * @param ownerUsername имя владельца
     * @return true если обновление успешно
     */
    public boolean update(long id, HumanBeing human, String ownerUsername) {
        if (!existsAndOwnedBy(id, ownerUsername)) {
            return false;
        }
        human.setId(id);
        lock.writeLock().lock();
        try {
            collection.removeIf(h -> h.getId() == id);
            collection.add(human);
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public HumanBeing findById(long id) {
        lock.readLock().lock();
        try {
            return collection.stream()
                    .filter(h -> h.getId() == id)
                    .findFirst()
                    .orElse(null);
        } finally {
            lock.readLock().unlock();
        }
    }
}