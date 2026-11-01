# Project State

Last updated: after TASK-008.

## Completed

- TASK-001-bootstrap-project.md
- TASK-002-restructure-modules.md
- TASK-003-data-loading.md
- TASK-004-screen-and-room-view.md
- TASK-005-domain-state-and-actions.md
- TASK-006-command-panel-and-navigation.md
- TASK-007-object-interaction.md
- TASK-008-spells-and-spell-actions.md

## In Progress

None.

## Next Task

- Ожидается следующая задача.
  По дорожной карте дальше — дух, артефакт и полный этаж.

## Known Blockers

None.

## Notes

Заклинания подключены к той же системе действий, что осмотр и переходы.
Игрок начинает со заклинанием spark. Если объект его поддерживает, в списке появляется Cast: Spark.
Искра ставит объекту состояние lit: меняются подпись, цвет заглушки и текст осмотра.
Отдельная система заклинаний не понадобилась.
