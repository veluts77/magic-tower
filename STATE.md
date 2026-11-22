# Project State

Last updated: after TASK-018 debug start room.

## Completed

- TASK-001-bootstrap-project.md
- TASK-002-restructure-modules.md
- TASK-003-data-loading.md
- TASK-004-screen-and-room-view.md
- TASK-005-domain-state-and-actions.md
- TASK-006-command-panel-and-navigation.md
- TASK-007-object-interaction.md
- TASK-008-spells-and-spell-actions.md
- TASK-009-spirits.md
- TASK-010-artifacts.md
- TASK-011-creatures.md
- TASK-012-mastery-marks-and-floor-progression.md
- TASK-013-mvp-floor.md
- TASK-014-bugfix-chest-and-key.md
- TASK-015-single-sprite-integration-lantern.md
- TASK-016-ui-layout-fix.md
- TASK-017-object-positioning.md
- TASK-018-debug-start-room.md

## In Progress

None.

## Next Task

- Полировка координат объектов / UI-иконки / сохранение состояния (по приоритету).

## Known Blockers

None.

## Notes

Отладочный запуск в произвольной комнате: `./gradlew desktop:run --args="--room=<room_id>"`. Без аргумента или при неизвестной комнате — стандартный старт (с предупреждением в лог для некорректного id). Состояние игрока при debug-старте остаётся начальным.

Объекты позиционируются по явным относительным координатам. Автораскладка и надписи убраны. Нижняя панель 120 px.
MVP Floor готов. UI-иконки, effects, walk sheet пока не в рендере.
