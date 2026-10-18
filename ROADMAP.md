# Roadmap

## Milestone 0 — Bootstrap

Цель: проект существует и собирается.

Статус: **выполнено**.

Выполненные задачи:

- создан Gradle-проект;
- настроена Java 21;
- подключён libGDX;
- создан минимальный запуск окна.

---

## Milestone 0.5 — Module Restructuring

Цель: разделить проект на три модуля с чистой архитектурой.

Задачи:

- создать модуль `domain` (чистая Java, ноль зависимостей);
- создать модуль `engine` (зависит от `domain` + libGDX);
- обновить модуль `desktop` (зависит от `engine`);
- удалить модуль `core`;
- перенести существующий код в соответствующие модули;
- убедиться, что проект собирается и запускается.

Текущая задача:

- `tasks/TASK-002-restructure-modules.md`

---

## Milestone 1 — Application Shell

Цель: получить базовую оболочку игры.

Возможные будущие задачи:

- простой `Screen` abstraction в `engine`;
- загрузка базовых ресурсов;
- работа с `assets` folder;
- логирование;
- обработка выхода из игры.

---

## Milestone 2 — Data Loading

Цель: научиться читать игровые данные из JSON.

Будущие элементы:

- `assets/data/` folder;
- DTO classes в `domain.data`;
- `DataLoader` interface в `domain.service`;
- `JsonDataLoader` implementation в `engine.data`;
- validation of missing files.

---

## Milestone 3 — Room View

Цель: показать первую комнату.

Будущие элементы:

- `RoomRenderer` в `engine.render`;
- placeholder background;
- object hotspots;
- simple selection;
- debug text.

---

## Milestone 4 — Action System

Цель: сделать базовую систему действий.

Будущие элементы:

- `GameAction`, `ActionResolver`, `ActionExecutor` в `domain`;
- available actions provider;
- action execution;
- simple placeholder UI в `engine`.

---

## Milestone 5 — MVP Floor

Цель: реализовать один игровой этаж.

Ожидаемый объём:

- 4–6 комнат;
- 2–3 заклинания;
- 1 дух;
- 1 артефакт;
- 1 пассивное существо;
- 8 mastery marks;
- требование примерно 7 из 8 для прогресса.

---

## Milestone 6 — Progression and State

Цель: сохранить и восстановить прогресс.

Будущие элементы:

- `SaveService` interface в `domain`;
- `GdxSaveService` implementation в `engine`;
- local save file;
- floor completion state;
- known spells and spirits;
- flags.

---

## Milestone 7 — Polish

Цель: улучшить ощущение игры.

Возможно:

- простые эффекты;
- звук;
- более удобный интерфейс;
- визуальные подсказки;
- замена placeholder-графики.