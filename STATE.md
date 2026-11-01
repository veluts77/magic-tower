# Project State

Last updated: after TASK-006.

## Completed

- TASK-001-bootstrap-project.md
- TASK-002-restructure-modules.md
- TASK-003-data-loading.md
- TASK-004-screen-and-room-view.md
- TASK-005-domain-state-and-actions.md
- TASK-006-command-panel-and-navigation.md

## In Progress

None.

## Next Task

- Ожидается следующая задача.
  По дорожной карте дальше — один игровой этаж: заклинания, дух, артефакт.

## Known Blockers

None.

## Notes

Навигация между комнатами работает с игрового экрана.
Доступные действия показываются списком и выполняются клавишами 1–9.
Осмотр показывает описание, переход обновляет комнату, объекты, список действий и отладочную строку.
Недоступная клавиша не меняет комнату и оставляет сообщение домена до следующего действия.
