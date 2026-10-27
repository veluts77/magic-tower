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

Статус: **выполнено**.

---

## Milestone 1 — Application Shell

Цель: получить базовую оболочку игры.

Статус: **выполнено**. Есть игровой экран, папка `assets`, лог загрузки и выход по ESC.

Возможные будущие задачи:

- простой `Screen` abstraction в `engine`;
- загрузка базовых ресурсов;
- работа с `assets` folder;
- логирование;
- обработка выхода из игры.

---

## Milestone 2 — Data Loading

Цель: научиться читать игровые данные из JSON.

Статус: **выполнено**.

Будущие элементы:

- `assets/data/` folder;
- DTO classes в `domain.data`;
- `DataLoader` interface в `domain.service`;
- `JsonDataLoader` implementation в `engine.data`;
- validation of missing files.

---

## Milestone 3 — Room View

Цель: показать первую комнату.

Статус: **выполнено**. Первая комната рисуется программными заглушками. Выбор объектов по клику не делался.

Будущие элементы:

- `RoomRenderer` в `engine.render`;
- placeholder background;
- object hotspots;
- simple selection;
- debug text.

---

## Milestone 4 — Action System

Цель: сделать базовую систему действий.

Статус: **частично**. В `domain` есть модель действий, резолвер и экзекутор для осмотра и переходов. В интерфейсе команды ещё не показаны.

Будущие элементы:

- `GameAction`, `ActionResolver`, `ActionExecutor` в `domain`;
- available actions provider;
- action execution;
- simple placeholder UI в `engine`.

---

## Milestone 5 — MVP Floor

Цель: реализовать один игровой этаж.

Статус: **не начато**.

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

Статус: **не начато**.

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

Статус: **не начато**.

Возможно:

- простые эффекты;
- звук;
- более удобный интерфейс;
- визуальные подсказки;
- замена placeholder-графики.