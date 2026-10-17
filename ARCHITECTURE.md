# Architecture

## High-Level Goal

Архитектура должна быть простой, понятной и расширяемой.

Основные принципы:

- данные отделены от кода;
- игровая логика отделена от рендеринга;
- действия отделены от способа их отображения;
- не использовать сложные паттерны без необходимости;
- не использовать ECS на раннем этапе;
- не использовать тяжёлые фреймворки поверх libGDX без необходимости.

---

## Gradle Modules

Проект состоит из двух Gradle-модулей:

### `core`

Содержит игровую логику и общие классы.

Не содержит платформенный код запуска.

### `desktop`

Содержит десктопный запускатор на LWJGL3.

---

## Recommended Package Structure

В будущем рекомендуется такая структура пакетов:

```text
tower.core
├── TowerGame.java
├── screen
├── data
├── world
├── action
├── spell
├── spirit
└── util
```

### `tower.core`

Главный класс игры.

### `tower.core.screen`

Экраны игры:

- boot screen;
- room screen;
- future menu screens.

### `tower.core.data`

DTO-классы и загрузчики данных:

- floor data;
- room data;
- spell data;
- action data.

### `tower.core.world`

Игровое состояние:

- текущий этаж;
- текущая комната;
- известные заклинания;
- артефакты;
- флаги;
- mastery marks.

### `tower.core.action`

Система действий:

- доступные действия;
- условия доступности;
- выполнение действий.

### `tower.core.spell`

Модель заклинаний.

### `tower.core.spirit`

Модель духов.

---

## Data Format

Основной формат данных на старте:

- JSON.

Причины:

- встроенная поддержка в libGDX;
- минимум зависимостей;
- простая отладка.

В будущем можно рассмотреть YAML, но для MVP используем JSON.

Рекомендуемое расположение данных:

```text
assets/
└── data/
    ├── floors/
    ├── spells/
    ├── spirits/
    └── actions/
```

На этапе `TASK-001` данные ещё не добавляются.

---

## State Model

Игровое состояние должно быть отделимо от рендеринга.

В будущем ожидается состояние примерно такого вида:

- `currentFloorId`
- `currentRoomId`
- `knownSpells`
- `knownSpirits`
- `artifacts`
- `flags`
- `masteryMarks`
- `seed`, если понадобится лёгкая рандомизация

На этапе начальной задачи состояние не реализуется.

---

## Action System Principle

Будущая система действий должна работать так:

1. Игрок выбирает объект или комнату.
2. Игра собирает контекст.
3. Игра определяет список доступных действий.
4. UI отображает эти действия.
5. Игрок выбирает действие.
6. Действие выполняется.
7. Состояние мира меняется.
8. Список действий может измениться.

Важно:

- UI не должен сам решать, что можно сделать;
- игровая логика не должна зависеть от конкретного вида меню.

---

## Rendering

На старте рендеринг максимально простой:

- очистка экрана;
- в будущем: отрисовка фона комнаты;
- затем: отрисовка объектов и персонажа.

Пока не используем:

- scene2d complex layouts;
- particle systems;
- shaders;
- skeletal animation;
- isometric renderers.

---

## Dependencies Policy

Разрешено:

- `com.badlogicgames.gdx:gdx`
- `com.badlogicgames.gdx:gdx-backend-lwjgl3`
- `com.badlogicgames.gdx:gdx-platform` с `natives-desktop`

Не добавлять без необходимости:

- DI frameworks;
- ECS frameworks;
- Kotlin;
- Groovy;
- Scala;
- Jackson;
- Gson;
- YAML parsers;
- networking libraries;
- physics engines.

Если новая зависимость действительно нужна, агент должен объяснить это в отчёте.