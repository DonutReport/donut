package report.donut.gherkin.model

case class BeforeHook(output: List[String], status: Status = Status(false, ""), duration: Duration = Duration(0L, ""), error_message: String = "")

case class AfterHook(output: List[String], status: Status = Status(false, ""), duration: Duration = Duration(0L, ""), error_message: String = "")

case class Row(cells: List[String])

case class Embedding(mime_type: String = "", data: String = "", id: Int = 0)

case class Screenshots(screenshotsIds: String, screenshotsSize: Int, screenshotsStyle: String)

case class Step(name: String,
                keyword: String,
                rows: List[Row],
                output: List[String],
                status: Status = Status(false, ""),
                duration: Duration = Duration(0L, ""),
                start_time: Long,
                end_time: Long,
                error_message: String = "")

case class Scenario(description: Option[String],
                    name: String,
                    keyword: String,
                    tags: List[String],
                    steps: List[Step],
                    featureName: String = "",
                    featureIndex: String = "",
                    status: Status = Status(false, ""),
                    duration: Duration = Duration(0L, ""),
                    background: Option[Scenario],
                    screenshotsSize: Int = 0,
                    screenshotIDs: String = "",
                    screenshotStyle: String = "display:none",
                    `type`: Option[String],
                    examples: List[Examples],
                    before: List[BeforeHook] = List.empty,
                    after: List[AfterHook] = List.empty
                   )

case class Examples(name: String,
                    keyword: String,
                    description: Option[String],
                    rows: List[Row]
                   )

case class Feature(keyword: String,
                   name: String,
                   description: String,
                   uri: String,
                   scenarios: List[Scenario],
                   tags: List[String],
                   status: Status = Status(false, ""),
                   duration: Duration = Duration(0L, ""),
                   scenarioMetrics: Metrics = Metrics(0, 0, 0),
                   stepMetrics: Metrics = Metrics(0, 0, 0, 0, 0),
                   htmlFeatureTags: List[String] = List.empty,
                   htmlElements: String = "",
                   engine: String,
                   index: String = "0") {

  val scenariosExcludeBackground: List[Scenario] = {
    scenarios.filterNot(e => e.keyword == Feature.BackgroundKeyword)
  }

  val scenariosExcludeBackgroundAndUnitTests: List[Scenario] = {
    scenarios.filterNot(e => e.keyword == Feature.BackgroundKeyword).filterNot(e => e.`type`.getOrElse("") == Feature.UnitTestType)
  }

  val unitTests: List[Scenario] = {
    scenarios.filter(s => s.`type`.getOrElse("") == Feature.UnitTestType)
  }
}

object Feature {
  val UnitTestType = "unit-test" // Expected type for a unit test
  val DummyFeatureName = "Without feature" // Used to decide if a unit test is orphaned
  val BackgroundKeyword = "Background"
}
