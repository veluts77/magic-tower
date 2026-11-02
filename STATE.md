# Project State

Last updated: after TASK-011.

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

## In Progress

None.

## Next Task

- Ожидается следующая задача.
  По дорожной карте дальше — mastery marks и полный этаж.

## Known Blockers

None.

## Notes

Существа подключены к той же системе действий, что осмотр, заклинания, духи и артефакты.
В боковой нише лежит пепельный слизень. Он блокирует сундук.
Пока слизень не нейтрализован, у сундука доступен только осмотр. Другое действие возвращает сообщение о блокировке и не меняет состояние.
Игрок знает искру. На слизне доступны осмотр и Cast: Spark. Искра нейтрализует слизня, после чего сундук открывается для остальных действий.
Нейтрализованный слизень остаётся в комнате. Повторно нейтрализовать его нельзя.
