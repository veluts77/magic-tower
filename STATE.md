# Project State

Last updated: after full sprite wiring in RoomRenderer.

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

## In Progress

None.

## Next Task

- Milestone 6: сохранение и загрузка состояния.

## Known Blockers

None.

## Notes

MVP Floor готов. Игра проходится от прихожей «Пыльного архива» до подъёма на следующий этаж.
На этаже 5 комнат, 3 заклинания, 1 дух, 1 артефакт, 1 существо и 8 печатей. Для подъёма нужно 7.
Искра и печать искры даются при старте. Толчок, кольцо, огонь, слизень, ключ, искатель и руна проходятся существующими действиями.
Второй этаж — пустая комната с сообщением «Этот этаж будет добавлен позже.»
Текст рисуется шрифтом PT Sans: встроенный BitmapFont libGDX не содержит кириллицу.
Ключ скрытой кельи — флаг `gatekeeper_key_found`, не отдельный объект. Слизень блокирует старый сундук. Осмотр сундука после искры ставит флаг и открывает переход.
Комнатные объекты с ассетами рисуются спрайтами (включая свитки, кольцо, фолиант, руну, слизня). Состояния: фонарь lit → `obj_magic_lantern_lit`, сундук после ключа → open, слизень после нейтрализации → neutralized. Игрок — `player_idle`; призванный seeker — рядом с игроком. Без ассета (`dusty_floor`, `sealed_box`) — цветные заглушки. Фоны комнат из `assets/room_backgrounds/`.
UI-иконки, effects, walk sheet и stairs/bookshelf пока не в рендере (нет слота в UI/данных).
Следующие шаги: сохранение, полировка UI-иконок.
