# Task Report Template

После выполнения задачи агент должен предоставить отчёт в следующем формате:

```text
Task:
Status:

Files created:
Files modified:

Commands executed:

Build result:
Run result:

Errors:
Warnings:

Assumptions:
Questions:

Next recommended step:
```

Пояснения:

- `Task`: идентификатор задачи, например `TASK-001`.
- `Status`: `done`, `partial`, `blocked`.
- `Files created`: список созданных файлов.
- `Files modified`: список изменённых файлов.
- `Commands executed`: команды, которые реально запускались.
- `Build result`: успех или ошибка.
- `Run result`: если запуск проверялся; иначе написать `not tested`.
- `Errors`: только релевантные ошибки, не весь лог.
- `Warnings`: важные предупреждения.
- `Assumptions`: если агент принимал решения в условиях неопределённости.
- `Questions`: что требует уточнения.
- `Next recommended step`: рекомендуемая следующая задача.