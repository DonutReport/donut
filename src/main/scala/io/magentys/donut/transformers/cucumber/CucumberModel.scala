package io.magentys.donut.transformers.cucumber

case class Comment(value: String, line: Int)

case class Tag(name: String, line: Int)

case class Result(duration: Long = 0L, status: String = "", error_message: String = "")

case class Argument(offset: Int, `val`: String = "")

case class Match(location: String = "", arguments: Option[List[Argument]])

case class BeforeHook(result: Result, `match`: Match)

case class AfterHook(result: Result, `match`: Match)

case class Row(comments: List[Comment], cells: List[String], line: Int)

case class Embedding(mime_type: String = "", data: String = "", id: Int = 0)

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
                   description: String,
                   name: String,
                   keyword: String,
                   line: Int,
                   `type`: String,
                   before: List[BeforeHook],
                   after: List[AfterHook],
                   tags: List[Tag],
                   steps: List[Step])

case class Feature(keyword: String,
                   name: String,
                   description: String,
                   line: Int,
                   id: String,
                   uri: String,
                   elements: List[Element],
                   comments: List[Comment],
                   tags: List[Tag]) {

  val scenariosExcludeBackground = {
    elements.filterNot(e => e.keyword == "Background")
  }
}