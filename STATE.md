# Project State

Last updated: after TASK-009.

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

## In Progress

None.

## Next Task

- Ожидается следующая задача.
  По дорожной карте дальше — артефакт и полный этаж.

## Known Blockers

None.

## Notes

Духи подключены к той же системе действий, что осмотр, переходы и заклинания.
Игрок с начала игры знает духа seeker. В действиях комнаты есть Summon: Seeker, пока этот дух в ней не призван.
Призыв искателя раскрывает скрытые объекты комнаты. Они появляются в комнате и доступны для осмотра.
Повторный призыв в той же комнате недоступен. В другой комнате духа можно призвать снова.
