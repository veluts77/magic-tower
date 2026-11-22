# Project State

Last updated: after TASK-016 UI layout fix.

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

## In Progress

None.

## Next Task

- Позиционирование объектов (явные координаты вместо автоматической раскладки).

## Known Blockers

None.

## Notes

Макет игрового экрана исправлен: нижняя панель зафиксирована на 120 px, комната занимает всё пространство выше (1280×600 при 1280×720). Большая чёрная рамка вокруг комнаты убрана; у панели — тонкая граница. Фон масштабируется по ширине с обрезкой по высоте; объекты масштабируются с сохранением пропорций. Автораскладка и подписи объектов сохранены.

MVP Floor готов. Игра проходится от прихожей «Пыльного архива» до подъёма на следующий этаж.
На этаже 5 комнат, 3 заклинания, 1 дух, 1 артефакт, 1 существо и 8 печатей. Для подъёма нужно 7.
Искра и печать искры даются при старте. Толчок, кольцо, огонь, слизень, ключ, искатель и руна проходятся существующими действиями.
Второй этаж — пустая комната с сообщением «Этот этаж будет добавлен позже.»
Текст рисуется шрифтом PT Sans: встроенный BitmapFont libGDX не содержит кириллицу.
Ключ скрытой кельи — флаг `gatekeeper_key_found`, не отдельный объект. Слизень блокирует старый сундук. Осмотр сундука после искры ставит флаг и открывает переход.
Комнатные объекты с ассетами рисуются спрайтами. Состояния: фонарь lit → `obj_magic_lantern_lit`, сундук после ключа → open, слизень после нейтрализации → neutralized. Игрок — `player_idle`; призванный seeker — рядом с игроком. Без ассета — цветные заглушки. Фоны комнат из `assets/room_backgrounds/`.
UI-иконки, effects, walk sheet и stairs/bookshelf пока не в рендере.
