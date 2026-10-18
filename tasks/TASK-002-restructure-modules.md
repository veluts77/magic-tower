# TASK-002 — Restructure into domain / engine / desktop

## Goal

Переструктурировать проект из двух модулей (`core` + `desktop`) в три модуля:

- `domain` — чистая игровая логика, ноль зависимостей;
- `engine` — интеграция с libGDX, зависит от `domain`;
- `desktop` — запускатор, зависит от `engine`.

После выполнения задачи модуль `core` должен быть удалён.

---

## Prerequisites

- TASK-001 выполнен.
- Проект собирается через `./gradlew build`.
- Десктопное окно открывается через `./gradlew desktop:run`.

---

## Steps

### Step 1. Update `settings.gradle`

Заменить содержимое `settings.gradle`:

```gradle
rootProject.name = 'tower-game'

include 'domain'
include 'engine'
include 'desktop'
```

### Step 2. Create `domain` module

Создать файл `domain/build.gradle`:

```gradle
plugins {
    id 'java-library'
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

// No external dependencies.
// This module uses only Java stdlib.
```

Создать структуру пакетов:

```text
domain/src/main/java/tower/domain/
```

Создать файл-заглушку `domain/src/main/java/tower/domain/package-info.java`:

```java
/**
 * Pure domain logic for Tower Game.
 * This module has zero external dependencies.
 */
package tower.domain;
```

### Step 3. Create `engine` module

Создать файл `engine/build.gradle`:

```gradle
plugins {
    id 'java-library'
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    api project(':domain')
    implementation "com.badlogicgames.gdx:gdx:$gdxVersion"
}
```

Создать структуру пакетов:

```text
engine/src/main/java/tower/engine/
```

### Step 4. Move TowerGame to engine

Переместить файл:

```text
FROM: core/src/main/java/tower/core/TowerGame.java
TO:   engine/src/main/java/tower/engine/TowerGame.java
```

Обновить содержимое файла:

```java
package tower.engine;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;

public class TowerGame extends Game {

    @Override
    public void create() {
        // Game initialization will be added later.
    }

    @Override
    public void render() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        super.render();
    }

    @Override
    public void dispose() {
        // Resource disposal will be added later.
    }
}
```

Изменения по сравнению с предыдущей версией:

- пакет изменён с `tower.core` на `tower.engine`.

### Step 5. Update `desktop/build.gradle`

Заменить содержимое `desktop/build.gradle`:

```gradle
plugins {
    id 'java'
    id 'application'
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    implementation project(':engine')
    implementation "com.badlogicgames.gdx:gdx-backend-lwjgl3:$gdxVersion"
    runtimeOnly "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop"
}

application {
    mainClass = 'tower.desktop.DesktopLauncher'
}

tasks.named('run') {
    workingDir = rootProject.file('assets').path
    jvmArgs += '-XstartOnFirstThread'
}
```

Изменения по сравнению с предыдущей версией:

- `implementation project(':core')` заменён на `implementation project(':engine')`.

### Step 6. Update DesktopLauncher

Обновить `desktop/src/main/java/tower/desktop/DesktopLauncher.java`:

```java
package tower.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import tower.engine.TowerGame;

public class DesktopLauncher {

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();

        config.setTitle("Tower Game");
        config.setWindowedMode(960, 540);
        config.setResizable(false);
        config.useVsync(true);
        config.setForegroundFPS(60);

        new Lwjgl3Application(new TowerGame(), config);
    }
}
```

Изменения по сравнению с предыдущей версией:

- импорт изменён с `tower.core.TowerGame` на `tower.engine.TowerGame`.

### Step 7. Delete `core` module

Удалить директорию `core/` целиком.

### Step 8. Verify

Выполнить проверку сборки:

```bash
./gradlew build
```

Если GUI доступен, дополнительно проверить запуск:

```bash
./gradlew desktop:run
```

---

## Expected Final Structure

```text
tower-game/
├── settings.gradle
├── build.gradle
├── gradle.properties
├── .gitignore
├── assets/
│   └── .gitkeep
├── domain/
│   ├── build.gradle
│   └── src/
│       └── main/
│           └── java/
│               └── tower/
│                   └── domain/
│                       └── package-info.java
├── engine/
│   ├── build.gradle
│   └── src/
│       └── main/
│           └── java/
│               └── tower/
│                   └── engine/
│                       └── TowerGame.java
├── desktop/
│   ├── build.gradle
│   └── src/
│       └── main/
│           └── java/
│               └── tower/
│                   └── desktop/
│                       └── DesktopLauncher.java
├── docs/
│   └── CODING_AGENT_RULES.md
└── tasks/
    ├── TASK-001-bootstrap-project.md
    └── TASK-002-restructure-modules.md
```

---

## Acceptance Criteria

1. Модуль `core` удалён.
2. Модуль `domain` существует, собирается, не имеет внешних зависимостей.
3. Модуль `engine` существует, собирается, зависит от `domain` и `gdx`.
4. Модуль `desktop` существует, собирается, зависит от `engine`.
5. `./gradlew build` завершается успешно.
6. `./gradlew desktop:run` открывает окно (если GUI доступен).
7. Класс `TowerGame` находится в пакете `tower.engine`.
8. Класс `DesktopLauncher` импортирует `tower.engine.TowerGame`.
9. Обновлён `STATE.md`.
10. Подготовлен краткий отчёт.

---

## Constraints

Запрещено:

- добавлять игровую логику;
- добавлять новые классы в `domain`, кроме `package-info.java`;
- добавлять новые классы в `engine`, кроме `TowerGame.java`;
- менять `build.gradle` в корне;
- менять `gradle.properties`;
- добавлять новые зависимости.

---

## Required State Update

После выполнения задачи `STATE.md` должен быть обновлён примерно так:

```md
# Project State

Last updated: after TASK-002.

## Completed

- TASK-001-bootstrap-project.md
- TASK-002-restructure-modules.md

## In Progress

None.

## Next Task

- WAITING_FOR_TASK-003

## Known Blockers

None.

## Notes

Проект разделён на три модуля: domain, engine, desktop.
Модуль core удалён.
```