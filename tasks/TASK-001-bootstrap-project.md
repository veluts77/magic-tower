# TASK-001 — Bootstrap Gradle + libGDX Desktop Project

## Goal

Создать минимальный рабочий Java-проект с Gradle и libGDX, который:

- собирается через Gradle;
- запускает десктопное окно;
- показывает пустой тёмный экран;
- закрывается по `ESC`;
- имеет структуру для будущего развития игры.

Эта задача не добавляет геймплей.

---

## Prerequisites

Ожидается, что на машине доступны:

- Java 21 JDK;
- Gradle 8.x или возможность создать Gradle Wrapper;
- доступ в интернет для загрузки зависимостей из Maven Central.

Если Gradle Wrapper ещё отсутствует, агент может один раз выполнить:

```bash
gradle wrapper --gradle-version 8.10.2
```

Если Gradle недоступен, агент должен сообщить об этом как о блокере.

---

## Required Project Structure

В корне проекта должна получиться следующая структура:

```text
tower-game/
├── settings.gradle
├── build.gradle
├── gradle.properties
├── .gitignore
├── assets/
│   └── .gitkeep
├── core/
│   ├── build.gradle
│   └── src/
│       └── main/
│           └── java/
│               └── tower/
│                   └── core/
│                       └── TowerGame.java
└── desktop/
    ├── build.gradle
    └── src/
        └── main/
            └── java/
                └── tower/
                    └── desktop/
                        └── DesktopLauncher.java
```

Также должны существовать документы проекта, уже переданные пользователем:

```text
AGENT_PROMPT.md
PROJECT_BRIEF.md
GAME_DESIGN.md
ARCHITECTURE.md
ROADMAP.md
STATE.md
REPORT_TEMPLATE.md
docs/CODING_AGENT_RULES.md
tasks/TASK-001-bootstrap-project.md
```

Их удалять нельзя.

---

## Expected Files

Ниже приведено ожидаемое содержимое файлов.
Агент может минимально адаптировать его, если этого требует версия Gradle,
но не должен менять архитектуру или добавлять лишние зависимости.

---

## `settings.gradle`

```gradle
rootProject.name = 'tower-game'

include 'core'
include 'desktop'
```

---

## `gradle.properties`

```properties
org.gradle.jvmargs=-Xmx2G
gdxVersion=1.12.1
```

---

## Root `build.gradle`

```gradle
allprojects {
    group = 'tower'
    version = '0.1.0'

    repositories {
        mavenCentral()
    }
}

subprojects {
    tasks.withType(JavaCompile).configureEach {
        options.encoding = 'UTF-8'
    }
}
```

---

## `core/build.gradle`

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
    api "com.badlogicgames.gdx:gdx:$gdxVersion"
}
```

---

## `desktop/build.gradle`

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
    implementation project(':core')
    implementation "com.badlogicgames.gdx:gdx-backend-lwjgl3:$gdxVersion"
    runtimeOnly "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop"
}

application {
    mainClass = 'tower.desktop.DesktopLauncher'
}

tasks.named('run') {
    workingDir = rootProject.file('assets').path
}
```

---

## `core/src/main/java/tower/core/TowerGame.java`

```java
package tower.core;

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

---

## `desktop/src/main/java/tower/desktop/DesktopLauncher.java`

```java
package tower.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import tower.core.TowerGame;

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

---

## `assets/.gitkeep`

Создать пустой файл:

```text
assets/.gitkeep
```

Он нужен, чтобы папка `assets` существовала в репозитории.

---

## Commands to Verify

После создания файлов агент должен проверить сборку:

```bash
./gradlew build
```

Если используется Windows:

```bat
gradlew.bat build
```

Если GUI доступен и задача проверяется локально, можно дополнительно выполнить:

```bash
./gradlew desktop:run
```

или в Windows:

```bat
gradlew.bat desktop:run
```

Но если агент работает без GUI, запуск окна не обязателен.
В этом случае в отчёте нужно написать:

```text
Run result: GUI not tested
```

---

## Acceptance Criteria

Задача считается выполненной, если:

1. Создана требуемая структура файлов.
2. `./gradlew build` завершается успешно.
3. Нет ошибок компиляции.
4. Не добавлены лишние зависимости.
5. Не добавлены игровые механики.
6. Обновлён `STATE.md`.
7. Подготовлен краткий отчёт.

Если агент смог запустить приложение:

- окно открывается;
- заголовок окна `Tower Game`;
- фон тёмный;
- `ESC` закрывает приложение.

Если агент не может запустить GUI:

- достаточно успешной сборки;
- в отчёте указать, что GUI не проверялся.

---

## Constraints

Запрещено:

- добавлять игровую логику;
- добавлять экраны;
- добавлять JSON-загрузку;
- добавлять спрайты;
- добавлять звук;
- добавлять новые модули;
- менять язык проекта;
- добавлять изометрию;
- добавлять физику;
- добавлять сложные UI-компоненты.

---

## Required State Update

После выполнения задачи `STATE.md` должен быть обновлён примерно так:

```md
# Project State

Last updated: after TASK-001.

## Completed

- TASK-001-bootstrap-project.md

## In Progress

None.

## Next Task

- WAITING_FOR_TASK-002

## Known Blockers

None.

## Notes

Базовый Gradle-проект с libGDX создан и собирается.
```

Если задача выполнена частично или заблокирована, обновить состояние соответственно.