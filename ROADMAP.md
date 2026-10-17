# Roadmap

## Milestone 0 — Bootstrap

Цель: проект существует и собирается.

Задачи:

- создать Gradle-проект;
- настроить Java 21;
- подключить libGDX;
- создать модули `core` и `desktop`;
- сделать минимальный запуск окна;
- подготовить структуру папок.

Текущая задача:

- `tasks/TASK-001-bootstrap-project.md`

---

## Milestone 1 — Application Shell

Цель: получить базовую оболочку игры.

Возможные будущие задачи:

- простой `Screen` abstraction;
- загрузка базовых ресурсов;
- работа с `assets` folder;
- логирование;
- обработка выхода из игры.

---

## Milestone 2 — Data Loading

Цель: научиться читать игровые данные из JSON.

Будущие элементы:

- `assets/data/` folder;
- simple DTO classes;
- loader for floors / rooms / spells;
- validation of missing files.

---

## Milestone 3 — Room View

Цель: показать первую комнату.

Будущие элементы:

- placeholder background;
- object hotspots;
- simple selection;
- debug text.

---

## Milestone 4 — Action System

Цель: сделать базовую систему действий.

Будущие элементы:

- `GameAction`;
- available actions provider;
- action execution;
- simple placeholder UI.

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