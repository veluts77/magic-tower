# Project State

Last updated: after TASK-010.

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

## In Progress

None.

## Next Task

- Ожидается следующая задача.
  По дорожной карте дальше — существа, mastery marks и полный этаж.

## Known Blockers

None.

## Notes

Артефакты подключены к той же системе действий, что осмотр, заклинания и духи.
В тихом зале лежит кольцо тихого пламени. Действие Pick up надевает его в свободный слот, после чего кольцо пропадает из видимых объектов комнаты.
У игрока три слота. В начале игры они пусты.
Комната Flame Veil закрыта огнём. Без защиты переход туда запрещён.
Экипированное кольцо само даёт защиту от огня. Отдельное действие для эффекта не нужно.
Повторно взять уже подобранное кольцо нельзя. Если свободных слотов нет, подбор недоступен.
