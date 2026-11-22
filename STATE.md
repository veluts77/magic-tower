# Project State

Last updated: after TASK-017 object positioning.

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

## In Progress

None.

## Next Task

- Полировка координат объектов / UI-иконки / сохранение состояния (по приоритету).

## Known Blockers

None.

## Notes

Объекты позиционируются по явным относительным координатам (role, position, size, z, wall, sprite, transition). Автоматическая раскладка убрана. Надписи к объектам убраны. Декоративные объекты рисуются, но не участвуют в выборе. Отрисовка учитывает z-порядок. Нижняя панель без изменений (120 px).

MVP Floor готов. Игра проходится от прихожей «Пыльного архива» до подъёма на следующий этаж.
На этаже 5 комнат, 3 заклинания, 1 дух, 1 артефакт, 1 существо и 8 печатей. Для подъёма нужно 7.
Текст рисуется шрифтом PT Sans.
Ключ скрытой кельи — флаг `gatekeeper_key_found`. Комнатные спрайты и фоны подключены.
UI-иконки, effects, walk sheet пока не в рендере.
