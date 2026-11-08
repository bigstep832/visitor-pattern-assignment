"""Build the Russian assignment report from real Surefire results and CLI output.

Run after `mvn test` from any directory. Only reportlab is required.
"""

from __future__ import annotations

import argparse
from pathlib import Path
import xml.etree.ElementTree as ET

from reportlab.lib import colors
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.pdfgen import canvas
from reportlab.platypus import Paragraph, Table, TableStyle


REPOSITORY = "https://github.com/bigstep832/visitor-pattern-assignment"
INK = colors.HexColor("#163044")
TEAL = colors.HexColor("#097C83")
MUTED = colors.HexColor("#526572")
PALE = colors.HexColor("#EDF5F6")
LINE = colors.HexColor("#CCDADD")
WIDTH, HEIGHT = A4
MARGIN = 42
BODY_WIDTH = WIDTH - 2 * MARGIN


def register_fonts(font_dir: Path | None) -> None:
    candidates = [font_dir] if font_dir else [
        Path("C:/Windows/Fonts"), Path("/usr/share/fonts/truetype/dejavu")
    ]
    for folder in candidates:
        for regular, bold in [("arial.ttf", "arialbd.ttf"), ("DejaVuSans.ttf", "DejaVuSans-Bold.ttf")]:
            if folder and (folder / regular).exists() and (folder / bold).exists():
                pdfmetrics.registerFont(TTFont("Body", str(folder / regular)))
                pdfmetrics.registerFont(TTFont("BodyBold", str(folder / bold)))
                pdfmetrics.registerFontFamily("Body", normal="Body", bold="BodyBold")
                return
    raise SystemExit("Arial or DejaVu Sans fonts not found; pass --font-dir")


def test_results(source: Path) -> list[tuple[str, int, int, int, int]]:
    files = sorted((source / "target/surefire-reports").glob("TEST-*.xml"))
    if not files:
        raise SystemExit("No Surefire XML reports found. Run mvn test first.")
    results = []
    for file in files:
        suite = ET.parse(file).getroot()
        results.append((suite.attrib["name"].rsplit(".", 1)[-1], *[
            int(suite.attrib.get(key, "0")) for key in ("tests", "failures", "errors", "skipped")
        ]))
    return results


class Report:
    def __init__(self, output: Path):
        output.parent.mkdir(parents=True, exist_ok=True)
        self.canvas = canvas.Canvas(str(output), pagesize=A4, invariant=1)
        self.canvas.setTitle("Assignment 06 - Visitor Pattern")
        self.canvas.setAuthor("Visitor Pattern project; prepared with ChatGPT/Codex assistance")
        self.body = ParagraphStyle("body", fontName="Body", fontSize=10, leading=14,
                                   textColor=INK, spaceAfter=8)
        self.small = ParagraphStyle("small", parent=self.body, fontSize=8.7, leading=12)
        self.page = 0
        self.y = 0

    def new_page(self, title: str, subtitle: str) -> None:
        if self.page:
            self.canvas.showPage()
        self.page += 1
        c = self.canvas
        c.setFillColor(TEAL)
        c.rect(MARGIN, HEIGHT - 42, 30, 4, stroke=0, fill=1)
        c.setFont("BodyBold", 9)
        c.drawString(MARGIN + 40, HEIGHT - 42, "AITU / SOFTWARE DESIGN PATTERNS / ASSIGNMENT 06")
        c.setFillColor(INK)
        c.setFont("BodyBold", 24)
        c.drawString(MARGIN, HEIGHT - 86, title)
        c.setFillColor(MUTED)
        c.setFont("Body", 10)
        c.drawString(MARGIN, HEIGHT - 106, subtitle)
        c.setStrokeColor(LINE)
        c.line(MARGIN, 42, WIDTH - MARGIN, 42)
        c.setFont("Body", 8)
        c.drawString(MARGIN, 27, "Java 17 / Maven / JUnit 5")
        c.drawRightString(WIDTH - MARGIN, 27, f"{self.page} / 4" if self.page < 4 else "UML / приложение")
        self.y = HEIGHT - 132

    def paragraph(self, text: str, small: bool = False, gap: float = 7) -> None:
        paragraph = Paragraph(text, self.small if small else self.body)
        _, height = paragraph.wrap(BODY_WIDTH, HEIGHT)
        if self.y - height < 57:
            raise ValueError(f"Content overflows page {self.page}: {text[:70]}")
        paragraph.drawOn(self.canvas, MARGIN, self.y - height)
        self.y -= height + gap

    def heading(self, text: str) -> None:
        self.y -= 6
        self.canvas.setFillColor(TEAL)
        self.canvas.setFont("BodyBold", 12)
        self.canvas.drawString(MARGIN, self.y, text)
        self.y -= 20

    def code(self, text: str, font_size: float = 9.3) -> None:
        lines = text.strip().splitlines()
        height = len(lines) * 13 + 20
        if self.y - height < 57:
            raise ValueError(f"Code block overflows page {self.page}")
        c = self.canvas
        c.setFillColor(PALE)
        c.roundRect(MARGIN, self.y - height, BODY_WIDTH, height, 6, stroke=0, fill=1)
        c.setFillColor(INK)
        c.setFont("Courier", font_size)
        for index, line in enumerate(lines):
            c.drawString(MARGIN + 12, self.y - 17 - index * 13, line)
        self.y -= height + 10

    def tests_table(self, results: list[tuple[str, int, int, int, int]]) -> None:
        rows = [["Класс тестов", "Тестов", "Failures", "Errors", "Skipped"]]
        rows.extend([[name, str(n), str(f), str(e), str(s)] for name, n, f, e, s in results])
        table = Table(rows, colWidths=[239, 62, 70, 65, BODY_WIDTH - 436], rowHeights=25)
        table.setStyle(TableStyle([
            ("FONTNAME", (0, 0), (-1, -1), "Body"),
            ("FONTNAME", (0, 0), (-1, 0), "BodyBold"),
            ("FONTSIZE", (0, 0), (-1, -1), 9),
            ("TEXTCOLOR", (0, 0), (-1, -1), INK),
            ("BACKGROUND", (0, 0), (-1, 0), PALE),
            ("ROWBACKGROUNDS", (0, 1), (-1, -1), [colors.white, colors.HexColor("#F7FAFA")]),
            ("LINEBELOW", (0, 0), (-1, 0), 0.8, TEAL),
            ("LINEBELOW", (0, -1), (-1, -1), 0.6, LINE),
            ("ALIGN", (1, 0), (-1, -1), "CENTER"),
            ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
            ("LEFTPADDING", (0, 0), (-1, -1), 8),
        ]))
        _, height = table.wrap(BODY_WIDTH, HEIGHT)
        table.drawOn(self.canvas, MARGIN, self.y - height)
        self.y -= height + 14

    def box(self, x: float, y: float, width: float, height: float, title: str,
            lines: list[str], stereotype: str = "") -> None:
        c = self.canvas
        c.setFillColor(colors.white)
        c.setStrokeColor(INK)
        c.setLineWidth(0.8)
        c.roundRect(x, y, width, height, 3, stroke=1, fill=1)
        header_height = 37 if stereotype else 25
        c.setFillColor(PALE)
        c.rect(x + 0.5, y + height - header_height, width - 1, header_height - 1, stroke=0, fill=1)
        if stereotype:
            c.setFillColor(MUTED)
            c.setFont("Body", 7.5)
            c.drawCentredString(x + width / 2, y + height - 12, stereotype)
        c.setFillColor(INK)
        c.setFont("BodyBold", 9.5)
        c.drawCentredString(x + width / 2, y + height - header_height + 9, title)
        c.line(x, y + height - header_height, x + width, y + height - header_height)
        c.setFont("Body", 8)
        for index, line in enumerate(lines):
            c.drawString(x + 7, y + height - header_height - 15 - index * 12, line)

    def polyline(self, points: list[tuple[float, float]], dashed: bool = False,
                 triangle: bool = False, arrow: bool = False) -> None:
        c = self.canvas
        c.setStrokeColor(INK)
        c.setLineWidth(0.85)
        c.setDash(3, 2) if dashed else c.setDash()
        path = c.beginPath()
        path.moveTo(*points[0])
        for point in points[1:]:
            path.lineTo(*point)
        c.drawPath(path)
        c.setDash()
        x, y = points[-1]
        px, py = points[-2]
        dx, dy = x - px, y - py
        length = (dx * dx + dy * dy) ** 0.5
        ux, uy = dx / length, dy / length
        if triangle or arrow:
            back_x, back_y = x - 8 * ux, y - 8 * uy
            p = c.beginPath()
            p.moveTo(back_x - 4 * uy, back_y + 4 * ux)
            p.lineTo(x, y)
            p.lineTo(back_x + 4 * uy, back_y - 4 * ux)
            if triangle:
                p.close()
                c.setFillColor(colors.white)
            c.drawPath(p, fill=int(triangle), stroke=1)

    def uml(self) -> None:
        self.new_page("Структура классов", "UML-приложение. Имена и связи соответствуют исходному коду.")
        self.box(42, 620, 215, 75, "Element", ["+ name(): String", "+ accept(Visitor): void"], "<<interface, sealed>>")
        self.box(300, 620, 253, 75, "Visitor", ["+ visit(FileNode): void", "+ visit(DirectoryNode): void"], "<<interface>>")
        self.box(42, 500, 215, 88, "Node", ["- name: String", "- parent: DirectoryNode", "~ attachTo(DirectoryNode): void"], "<<abstract, sealed>>")
        self.box(42, 359, 108, 99, "FileNode", ["- size: long", "+ size(): long", "+ accept(Visitor)"], "<<final>>")
        self.box(166, 344, 116, 114, "DirectoryNode", ["- children: Map", "+ add(Node)", "+ children(): List", "+ accept(Visitor)"], "<<final>>")
        self.box(300, 413, 253, 175, "TreeVisitor<R>", ["- pending: Deque<Frame>", "+ evaluate(Element): R", "+ visit(FileNode): void", "+ visit(DirectoryNode): void", "# reset(): void", "# onFile(FileNode, VisitContext)", "# onDirectory(DirectoryNode, VisitContext)", "# result(): R"], "<<abstract>>")
        self.box(42, 196, 153, 105, "TotalSizeVisitor", ["- total: long", "# onFile(...)", "# result(): Long"], "<<final>>")
        self.box(213, 196, 161, 105, "SearchVisitor", ["- filenameCriterion: Predicate", "- minimumSize: long", "# result(): List<String>"], "<<final>>")
        self.box(392, 196, 161, 105, "StatisticsVisitor", ["- fileCount, directoryCount", "- maximumDepth: int", "# result(): Statistics"], "<<final>>")
        self.box(42, 83, 216, 75, "VisitContext", ["+ path: String", "+ depth: int"], "<<record>>")
        self.box(337, 83, 216, 75, "Statistics", ["+ fileCount, directoryCount: long", "+ maximumDepth: int"], "<<record>>")
        self.polyline([(149, 588), (149, 620)], dashed=True, triangle=True)
        self.polyline([(426, 588), (426, 620)], dashed=True, triangle=True)
        self.polyline([(96, 458), (96, 478), (149, 478), (149, 500)], triangle=True)
        self.polyline([(224, 458), (224, 478), (149, 478), (149, 500)], triangle=True)
        self.polyline([(257, 661), (300, 661)], dashed=True, arrow=True)
        c = self.canvas
        c.setFont("Body", 7)
        c.setFillColor(MUTED)
        c.drawCentredString(278, 674, "accept")
        # Composition is drawn outside the inheritance links.
        self.polyline([(282, 394), (291, 394), (291, 545), (257, 545)])
        diamond = c.beginPath()
        for index, point in enumerate([(282, 394), (287, 398), (292, 394), (287, 390)]):
            diamond.moveTo(*point) if index == 0 else diamond.lineTo(*point)
        diamond.close()
        c.setFillColor(INK)
        c.drawPath(diamond, fill=1, stroke=1)
        c.setFont("Body", 7)
        c.drawString(264, 553, "0..*")
        c.drawString(267, 382, "0..1")
        for center in [118, 293, 472]:
            self.polyline([(center, 301), (center, 325), (426, 325), (426, 413)], triangle=True)
        self.polyline([(315, 413), (315, 337), (33, 337), (33, 174), (150, 174), (150, 158)], dashed=True, arrow=True)
        self.polyline([(472, 196), (472, 158)], dashed=True, arrow=True)
        c.setFont("Body", 7)
        c.setFillColor(MUTED)
        c.drawString(165, 176, "контекст кадра")
        c.drawString(480, 174, "результат")
        c.setFont("Body", 7.8)
        c.drawString(MARGIN, 59, "Пустой треугольник: наследование / реализация; ромб: владение; пунктир: зависимость.")


def build(source: Path, output: Path, font_dir: Path | None) -> None:
    register_fonts(font_dir)
    results = test_results(source)
    totals = [sum(row[index] for row in results) for index in range(1, 5)]
    demo = (source / "docs/demo-output.txt").read_text(encoding="utf-8-sig").strip()
    changed_demo = (source / "docs/demo-png-output.txt").read_text(encoding="utf-8-sig").strip()
    r = Report(output)
    r.new_page("Visitor Pattern", "Файловая система в памяти / проект и решения")
    r.paragraph("<b>Цель.</b> Отделить структуру файловой системы от независимых операций: суммирования размеров, поиска и статистики. Проект использует Java 17, Maven и собственные синтетические данные; чтение настоящего диска не требуется.")
    r.heading("01 / Модель и double dispatch")
    r.paragraph("<b>Element</b> задаёт общий контракт; <b>FileNode</b> хранит имя и неотрицательный размер, <b>DirectoryNode</b> владеет именованными детьми. Общий sealed-класс <b>Node</b> хранит имя и внутреннюю ссылку на родителя. В <b>Visitor</b> заранее определены обе перегрузки <b>visit</b>.")
    r.paragraph("Первый шаг: динамический тип элемента выбирает его <b>accept(Visitor)</b>. Второй шаг: внутри <b>FileNode.accept</b> тип <b>this</b> известен компилятору, поэтому выбирается <b>visit(FileNode)</b>; для каталога - <b>visit(DirectoryNode)</b>. Общий обработчик <b>TreeVisitor</b> вызывает специализированный <b>onFile</b> или <b>onDirectory</b>. Проверки типов не нужны.")
    r.heading("02 / Один владелец обхода")
    r.paragraph("Обходом владеет <b>TreeVisitor&lt;R&gt;</b>: <b>evaluate(root)</b> использует явный стек и DFS. Каталог добавляет детей в обратном порядке, поэтому обработка сохраняет порядок вставки. Элементы только выполняют dispatch. Каждый достижимый узел обрабатывается один раз; рекурсивных вызовов нет.")
    r.paragraph("<b>TotalSizeVisitor</b> прибавляет только размеры файлов. <b>SearchVisitor</b> объединяет predicate имени и включительный порог <b>size &gt;= minimumSize</b>, затем сортирует пути. <b>StatisticsVisitor</b> считает файлы, каталоги (включая корень) и максимальную глубину.")
    r.heading("03 / Пути, глубина и целостность")
    r.paragraph("Выбранный корень имеет глубину <b>0</b> и путь <b>.</b>; дети получают глубину родителя + 1. Имя корневого каталога в пути потомков не включается. Для корня-файла результат пути - <b>.</b>. Разделитель всегда <b>/</b>; порядок поиска - лексикографический <b>String.compareTo</b>. Пустые каталоги влияют на глубину.")
    r.paragraph("<b>DirectoryNode.add</b> до изменения проверяет повтор имён, владение другим родителем и цикл через цепочку предков. Отрицательные размеры и неоднозначные имена отвергаются; <b>children()</b> возвращает неизменяемый snapshot. Переполнение суммы явно вызывает <b>ArithmeticException</b>.")
    r.paragraph(f'<b>GitHub:</b> <link href="{REPOSITORY}" color="#097C83">{REPOSITORY}</link>', small=True)

    r.new_page("Проверка поведения", "Воспроизводимый запуск / результаты программы и JUnit")
    r.paragraph("Команды выполняются из корня проекта с <b>pom.xml</b>. Проверено в CLI на Microsoft OpenJDK 17.0.20.1 и Maven 3.9.11. Установленная IDE не участвует в выполнении тестов.")
    r.code("mvn test\nmvn package\njava -jar target/visitor-pattern-1.0.0.jar\njava -jar target/visitor-pattern-1.0.0.jar .png 1000", 8.8)
    r.paragraph("Дерево <b>campus</b> содержит README (120), три текстовых файла (800, 200 и 400), изображение (2048), пустой файл и вложенные пустые каталоги. Размеры заданы в байтах. Полный состав находится в <b>Demo.sampleTree</b>.")
    r.code(demo)
    r.paragraph("Изменённая конфигурация <b>.png 1000</b> дала единственное совпадение <b>assets/logo.png</b>; общая сумма, количества и глубина остались прежними. Выводы обоих запусков сохранены в <b>docs/demo-output.txt</b> и <b>docs/demo-png-output.txt</b>.", small=True)
    if "assets/logo.png" not in changed_demo:
        raise ValueError("Changed demo output does not contain its documented match")
    r.heading("Автоматизированная проверка")
    r.tests_table(results)
    r.paragraph(f"<b>Итого: {totals[0]} тестов; failures: {totals[1]}, errors: {totals[2]}, skipped: {totals[3]}.</b> Значения прочитаны из Surefire XML после фактического запуска, а не проставлены вручную.")
    r.paragraph("Покрыты T1-T8: перегрузки, вложенная сумма, оба условия поиска, порядок и относительные пути, статистика, пустые случаи, повторные запуски и недопустимые изменения. Дополнительно проверены 2000 уровней вложенности, переполнение, ошибка predicate и повторный вход. Ожидаемые значения тестов заданы независимо от реализации.", small=True)

    r.new_page("Обоснование решений", "Clean Code / границы расширения / повторяемость")
    r.heading("Пять конкретных практик Clean Code")
    practices = [
        ("Содержательные имена", "SearchVisitor.minimumSize и Statistics.maximumDepth прямо называют единицы смысла: порог байтов и максимальную глубину; отдельные поясняющие флаги не нужны."),
        ("Single Responsibility", "TotalSizeVisitor.onFile отвечает только за сумму; поиск и статистика выделены в другие классы, а общий обход находится в TreeVisitor."),
        ("Небольшие методы", "StatisticsVisitor.includeDepth содержит одну операцию max и используется для файлов и каталогов, исключая дублирование правила глубины."),
        ("Инкапсуляция", "DirectoryNode.children возвращает List.copyOf, а add является единственной публичной точкой изменения состава; клиент не может обойти проверки коллекцией."),
        ("Нет цепочек проверки типов", "FileNode.accept и DirectoryNode.accept передают this в перегруженный visit; выбор поведения не зависит от instanceof, getClass, reflection или cast."),
    ]
    for index, (title, body) in enumerate(practices, 1):
        r.paragraph(f"<b>{index}. {title}.</b> {body}")
    r.heading("Повторные запуски и ошибки")
    r.paragraph("Посетитель повторно используется последовательно: каждый <b>evaluate</b> вызывает <b>reset</b>, а <b>finally</b> очищает стек и контекст даже при ошибке. Старые результаты поиска и статистики неизменяемы. Вызов <b>TreeVisitor.visit</b> вне <b>evaluate</b> и вложенный <b>evaluate</b> на том же экземпляре отвергаются. Потокобезопасность не заявляется; predicate не меняет дерево и не вызывает посетителей.")
    r.heading("Расширение и компромиссы")
    r.paragraph("Третий посетитель добавлен отдельным commit: новые <b>Statistics</b>, <b>StatisticsVisitor</b> и его тесты. <b>FileNode</b>, <b>DirectoryNode</b>, <b>accept</b>, <b>Visitor</b> и общий обход не менялись. Новая операция требует только нового наследника <b>TreeVisitor&lt;R&gt;</b>.")
    r.paragraph("Новый тип элемента, напротив, требует обновить sealed <b>permits</b>, контракт <b>Visitor</b>, правила обхода и смысл операций для нового типа. Visitor удобен, когда набор типов стабилен, а операций становится больше. Обход выполняет O(n) посещений; поиск добавляет O(k log k) для сортировки. Стоимость построения строк зависит от длины путей.")
    r.heading("Источники и подготовка")
    r.paragraph('Требования: предоставленный Assignment_6_Visitor_Pattern.pdf, AITU, Assignment 06. Справочные материалы: <link href="https://docs.oracle.com/javase/specs/jls/se17/html/jls-15.html#jls-15.12" color="#097C83">Java SE 17, JLS 15.12</link>; <link href="https://docs.junit.org/5.10.2/user-guide/" color="#097C83">JUnit 5.10.2 User Guide</link>. Памятка по трассировке и изменению входа: <b>docs/DEFENSE.md</b>.', small=True)
    r.paragraph("Код, тесты и отчёт подготовлены с помощью ChatGPT/Codex. Это не утверждение о самостоятельном авторстве студента. Перед сдачей следует учесть правила курса о допустимой помощи ИИ и самостоятельно понимать и изменять реализацию.", small=True)
    r.uml()
    r.canvas.save()
    print(f"Created {output} (4 pages: 3 report + UML)")


def main() -> None:
    project = Path(__file__).resolve().parents[1]
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--source-root", type=Path, default=project)
    parser.add_argument("--output", type=Path, default=project / "docs/report.pdf")
    parser.add_argument("--font-dir", type=Path)
    args = parser.parse_args()
    build(args.source_root.resolve(), args.output.resolve(), args.font_dir)


if __name__ == "__main__":
    main()
