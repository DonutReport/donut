package io.magentys.donut.gherkin.model

case class BeforeHook(hookName: String, status: Status = Status(false, ""), duration: Duration = Duration(0L, ""), error_message: String = "")

case class AfterHook(hookName: String, status: Status = Status(false, ""), duration: Duration = Duration(0L, ""), error_message: String = "")

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

case class Scenario(description: String,
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
                    before: List[BeforeHook] = List.empty,
                    after: List[AfterHook] = List.empty)

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
                   index:String = "0") {

  val scenariosExcludeBackground = {
    scenarios.filterNot(e => e.keyword == "Background")
  }
}
