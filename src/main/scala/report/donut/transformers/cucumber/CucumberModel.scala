package report.donut.transformers.cucumber

case class Tag(name: String)

case class Result(duration: Long = 0L, status: String = "", error_message: String = "")

case class Argument(offset: Int, `val`: String = "")

case class Match(location: String = "", arguments: Option[List[Argument]])

case class BeforeHook(result: Result, output: List[String], `match`: Match)

case class AfterHook(result: Result, output: List[String], `match`: Match)

case class Row(cells: List[String])

case class Embedding(mime_type: String = "", data: String = "", id: Int = 0)

// cucumber 1 backwards compatibility
case class Examples(id: String,
                    name: String,
                    keyword: String,
                    line: Int,
                    description: Option[String],
                    rows: List[Row])

case class Step(name: String,
                keyword: String,
                line: Int,
                result: Result,
                `match`: Match,
                rows: List[Row],
                matchedColumns: List[Int],
                output: List[String],
                embeddings: List[Embedding])

case class Element(id: String = "",
                   description: Option[String],
                   name: String,
                   keyword: String,
                   line: Int,
                   `type`: Option[String],
                   before: List[BeforeHook],
                   after: List[AfterHook],
                   tags: List[Tag],
                   steps: List[Step],
                   examples: List[Examples])

case class Feature(keyword: String,
                   name: String,
                   description: Option[String],
                   line: Int,
                   id: String,
                   uri: String,
                   elements: List[Element],
                   tags: List[Tag]) {

  val scenariosExcludeBackground = {
    elements.filterNot(e => e.keyword == "Background")
  }
}