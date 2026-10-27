# Project State

Last updated: after TASK-005.

## Completed

- TASK-001-bootstrap-project.md
- TASK-002-restructure-modules.md
- TASK-003-data-loading.md
- TASK-004-screen-and-room-view.md
- TASK-005-domain-state-and-actions.md

## In Progress

None.

## Next Task

- Подключить команды и навигацию к игровому экрану.
  Модель действий уже готова в `domain` (осмотр и переходы), но ещё не выставлена в интерфейсе.

## Known Blockers

None.

## Notes

Игровой экран по-прежнему показывает первую комнату этажа программными заглушками.
Состояние игры, доступные действия и переходы между комнатами живут в `domain` и покрыты юнит-тестами.
