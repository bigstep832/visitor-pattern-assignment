# Проверка реализации

Дата: 08.11.2026 (Asia/Qyzylorda). Среда: Windows, Microsoft OpenJDK 17.0.20.1,
Apache Maven 3.9.11, JUnit Jupiter 5.10.2. Компиляция: `--release 17`.

## Выполненные команды

```sh
mvn test
mvn package
java -jar target/visitor-pattern-1.0.0.jar
java -jar target/visitor-pattern-1.0.0.jar .png 1000
```

`mvn test` выполнен из каталога с `pom.xml`, после настройки `JAVA_HOME` и `PATH`.
На машине разработки использован отдельный Maven cache через `maven.repo.local`;
это не требуется для обычного запуска на другой машине. Первая попытка скачивания
зависимостей в сетевой песочнице завершилась ошибкой разрешения Maven Central;
после разрешённого доступа зависимости загрузились и сборки завершились успешно.

Результат тестов:

```text
DemoTest                 2
DispatchTest             1
SearchVisitorTest        7
StatisticsVisitorTest    5
TotalSizeVisitorTest     4
TreeIntegrityTest        6
Tests run: 25, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

`mvn package`: BUILD SUCCESS, создан `target/visitor-pattern-1.0.0.jar`.
Стандартное демо: 3568 байт, 6 файлов, 6 каталогов, максимальная глубина 3;
два совпадения `.txt` с размером >= 300. Изменённое демо `.png 1000`:
одно совпадение `assets/logo.png`. Выводы сохранены рядом с этим документом.

Проверена история добавления статистики: commit `cefe72a` добавляет только
`Statistics.java`, `StatisticsVisitor.java`, `StatisticsVisitorTest.java`.
Изменений элементов или `Visitor` в этом commit нет.
В production-коде не найдены `instanceof`, `getClass`, reflection или casts
между `FileNode` и `DirectoryNode` для выбора поведения.

## Границы

Приложение моделирует дерево в памяти; оно не читает реальные файлы и не хранит
данные на диске. Поддержка нескольких потоков и изменения дерева во время обхода
не заявляется. GitHub-публикация и загрузка в Moodle - отдельные действия;
успех локальных тестов не подтверждает загрузку в Moodle или прохождение защиты.
