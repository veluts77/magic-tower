# Architecture

## High-Level Goal

Архитектура должна быть простой, понятной и расширяемой.

Основные принципы:

- доменная логика полностью отделена от фреймворка;
- данные отделены от кода;
- игровая логика отделена от рендеринга;
- действия отделены от способа их отображения;
- не использовать сложные паттерны без необходимости;
- не использовать ECS на раннем этапе;
- не использовать тяжёлые фреймворки поверх libGDX без необходимости.

---

## Gradle Modules

Проект состоит из трёх Gradle-модулей:

### `domain`

Чистая игровая логика. **Ноль внешних зависимостей.**

Содержит:

- модели данных (этажи, комнаты, заклинания, духи, артефакты);
- состояние игры;
- игровую логику (прогрессия, доступные действия, загадки);
- DTO-классы для загрузки данных;
- интерфейсы для внешних сервисов (загрузка данных, сохранение).

Не содержит:

- никаких ссылок на libGDX;
- рендеринга;
- работы с файловой системой напрямую;
- обработки ввода.

### `engine`

Интеграция с libGDX. Зависит от `domain` и от `libGDX`.

Содержит:

- рендеринг (спрайты, фоны, камера);
- экраны (реализации libGDX Screen);
- обработку ввода и трансляцию в доменные действия;
- загрузку данных из файлов (реализация доменных интерфейсов);
- сохранение прогресса;
- главный класс игры, наследующий `com.badlogic.gdx.Game`.

Не содержит:

- игровой логики (правил, условий, прогрессии);
- определения того, какие действия доступны.

### `desktop`

Десктопный запускатор. Зависит от `engine`.

Содержит:

- `DesktopLauncher` с конфигурацией окна;
- платформенные настройки.

---

## Dependency Graph

```text
desktop
  └── engine
        ├── domain   (чистая Java, ноль зависимостей)
        └── libGDX   (gdx)
```

В терминах Gradle:

```text
desktop  → implementation project(':engine')
engine   → api project(':domain')
engine   → implementation gdx
desktop  → implementation gdx-backend-lwjgl3
desktop  → runtimeOnly gdx-platform natives-desktop
domain   → (ничего)
```

---

## Package Structure

### Модуль `domain`

```text
tower.domain
├── model/
│   ├── FloorId.java
│   ├── RoomId.java
│   ├── ObjectId.java
│   ├── SpellId.java
│   ├── SpiritId.java
│   ├── ArtifactId.java
│   └── MasteryMarkId.java
├── state/
│   ├── GameState.java
│   ├── FloorState.java
│   ├── PlayerState.java
│   └── RoomState.java
├── logic/
│   ├── FloorProgression.java
│   ├── ActionResolver.java
│   ├── ActionExecutor.java
│   └── PuzzleChecker.java
├── action/
│   ├── GameAction.java
│   ├── ActionType.java
│   ├── ActionContext.java
│   └── ActionResult.java
├── data/
│   ├── FloorDefinition.java
│   ├── RoomDefinition.java
│   ├── ObjectDefinition.java
│   ├── SpellDefinition.java
│   └── SpiritDefinition.java
└── service/
    ├── DataLoader.java
    ├── SaveService.java
    └── RandomProvider.java
```

На ранних этапах не все классы будут существовать.
Структура приведена как целевая, классы создаются по мере необходимости.

### Модуль `engine`

```text
tower.engine
├── TowerGame.java
├── render/
│   ├── RoomRenderer.java
│   └── SpriteManager.java
├── screen/
│   ├── GameScreen.java
│   └── BootScreen.java
├── input/
│   ├── InputHandler.java
│   └── ActionMapper.java
├── data/
│   └── JsonDataLoader.java
└── save/
    └── GdxSaveService.java
```

### Модуль `desktop`

```text
tower.desktop
└── DesktopLauncher.java
```

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

Парсинг JSON происходит в модуле `engine`.
Модуль `domain` определяет только структуру DTO-классов.

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

Все эти поля живут в модуле `domain`.

---

## Action System Principle

Будущая система действий должна работать так:

1. Игрок выбирает объект или комнату.
2. Модуль `engine` определяет, какой объект выбран.
3. Модуль `engine` запрашивает у `domain` список доступных действий.
4. Модуль `domain` анализирует состояние и возвращает список.
5. Модуль `engine` отображает действия через UI.
6. Игрок выбирает действие.
7. Модуль `engine` передаёт действие в `domain`.
8. Модуль `domain` выполняет действие и возвращает результат.
9. Модуль `engine` отображает результат.

Важно:

- `domain` не знает, как отображаются действия;
- `engine` не решает, какие действия доступны;
- `engine` не содержит игровых правил.

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

## Domain Module Rules

Модуль `domain`:

- использует только `java.*` и `javax.*`;
- не использует `com.badlogicgames.*`;
- не использует `Gdx.*`;
- для логирования использует `java.util.logging`;
- DTO-классы имеют публичные поля или стандартные геттеры/сеттеры;
- не содержит статических mutable состояний.

---

## Engine Module Rules

Модуль `engine`:

- может использовать `com.badlogicgames.gdx.*`;
- получает данные из `domain` и передаёт их в рендеринг;
- реализует интерфейсы из `domain.service`;
- не содержит игровых правил и условий;
- не определяет, какие действия доступны.

---

## Dependencies Policy

Разрешено:

- `com.badlogicgames.gdx:gdx` (в `engine`)
- `com.badlogicgames.gdx:gdx-backend-lwjgl3` (в `desktop`)
- `com.badlogicgames.gdx:gdx-platform` с `natives-desktop` (в `desktop`)

Модуль `domain` не имеет внешних зависимостей.

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