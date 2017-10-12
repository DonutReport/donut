package report.donut.gherkin.processors

import report.donut.gherkin.model._
import report.donut.template.SpecialCharHandler.escape

import scala.collection.mutable.ListBuffer

/*
 *
 * DOM creation for scenarios
 *
 * Here we create the list of scenarios rendered in HTML for each of type of listing: feature, tag, failure
 * In every listing the scenario rendering is the same.
 * Feature and Tag listings are groupings per feature or tag name accordingly,
 * whereas failures it's just a plain list of scenarios for the whole execution (no grouping).
 *
 * Indexing:
 * Indexing is required to give unique identifiers to the DOM elements so javascript can show/hide the relevant divs.
 *
 * The format of the index is:
 * [div type]-[parentIndex if exists]-[scenarioIndex]-[keyword]
 * ie. ul-feature-3-2 > this is the ul list step of a feature type grouping, feature #3, scenario#2
 *
 */

object HTMLFeatureProcessor {
  def apply(featureElements: List[Scenario], parentIndex: String): String =
    HTMLProcessor(featureElements, parentIndex + "-", "feature")
}

object HTMLTagsProcessor {
  def apply(tagElements: List[Scenario], parentIndex: String): String =
    HTMLProcessor(tagElements, parentIndex + "-", "tag")
}

object HTMLFailuresProcessor {
  def apply(failedElements: List[Scenario]): String = {
    if (failedElements.nonEmpty) {
      val indexes = failedElements.zipWithIndex
      val failedScenariosHtml = indexes.map { case (e, i) => HTMLProcessor.scenarios(e, i.toString.trim, "failure") }.mkString
      val failedScenariosIds = HTMLProcessor.scenariosAllIds("failure", indexes.map { case (e, i) => "ul-" + "failure-" + i.toString.trim }.mkString(","))
      failedScenariosHtml + failedScenariosIds
    } else {
      "No failures"
    }
  }
}

private[processors] object HTMLProcessor {

  def apply(elements: List[Scenario], parentIndex: String, parentType: String): String = {
    var map = Map[String, ListBuffer[Scenario]]()
    val bddScenarios = new ListBuffer[Scenario]
    val nonBddScenarios = new ListBuffer[Scenario]
    val builder = new StringBuilder()

    for (e <- elements) {
      map += (e.keyword -> addToList(e, bddScenarios, nonBddScenarios, map))
    }

    for (entry <- map) {
      builder ++=
        buildScenariosHtml(entry._2.toList, parentIndex, parentType)
    }
    builder.toString()
  }

  def addToList(e: Scenario, bddScenarios: ListBuffer[Scenario], nonBddScenarios: ListBuffer[Scenario], map: Map[String, ListBuffer[Scenario]]): ListBuffer[Scenario] = {

    if (map.keys.exists(key => key.equals(e.keyword))) {
      var list = map.get(e.keyword).get
      list += e
    } else {
      if ("Scenario".equals(e.keyword)) {
        bddScenarios += e
      } else {
        nonBddScenarios += e
      }
    }
  }


  def buildScenariosHtml(elements: List[Scenario], parentIndex: String, parentType: String): String = {

    scenarioType(elements.head).mkString +
      elements.zipWithIndex.map { case (e, i) => scenarios(e, e.keyword.replace(" ", "-").toLowerCase + "-" + parentIndex + i.toString.trim, parentType) }.mkString
  }

  private def scenarioType(scenario: Scenario): String = {
    s"""
       |<div class="row">
       |   <div class="panel panel-default">
       |      <div>
       |        <p class="scenario header"">
       |          <b>${scenario.keyword}s</b>
       |        </p>
       |      </div>
       |   </div>
       | </div>
     """.stripMargin
  }

  def scenarios(element: Scenario, index: String, parentType: String): String = {
    val featureName = getFeatureLink(parentType, element.featureIndex, element.featureName)
    val icon = statusIcon(element.status.statusStr)
    val style = if (element.status.statusStr == "passed") """style="display:none;"""" else ""
    val output = element.steps.flatMap(s => s.output).map(o => s"""<div class="step-custom-output">$o</div>""").mkString
    val screenshots = scenariosScreenshots(index, element.screenshotStyle, element.screenshotIDs, element.screenshotsSize, parentType)
    val backgroundHtml = HTMLProcessor.backgroundForScenario(element.background, index + "-background", parentType)

    s"""
       |<div class="row">
       |   <div class="panel panel-default">
       |      <div class="panel-body">
       |        $featureName
       |        <p>${elementTags(element.tags)}</p>
       |        $backgroundHtml
       |        <p class="scenario">
       |          <b>$icon </b>${escape(element.name)}
       |          <a href="#" class="btn btn-default btn-xs pull-right toggle-button" onclick="toggleScenario('ul-$parentType-$index', event)">
       |            <span class="glyphicon glyphicon-menu-down"></span>
       |          </a>
       |          <span class="durationBadge pull-right">${element.duration.durationStr} </span>
       |        </p>
       |        <div $style id="ul-$parentType-$index">
       |          ${elementDescription(element)}
       |          <ul class="list-group">
       |            ${stepList(element.steps)}
       |            ${examplesList(element.examples)}
       |          </ul>
       |          $output
       |        </div>
       |        $screenshots
       |      </div>
       |   </div>
       | </div>
     """.stripMargin

  }

  def elementDescription(element: Scenario) = {
    description(element.description.get)
  }

  def description(description: String) = {
    if(!description.isEmpty)
      s"""<p class="wrapped-text" style="white-space: pre-wrap;">${escape(description)}</p>""".mkString
    else """""".mkString
  }

  def backgroundForScenario(elementOpt: Option[Scenario], index: String, parentType: String) = {

    elementOpt match {
      case Some(element) =>
        val icon = statusIcon(element.status.statusStr)
        val style = if (element.status.statusStr == "passed") """style="display:none;"""" else ""
        val output = element.steps.flatMap(s => s.output).map(o => s"""<div class="step-custom-output">$o</div>""").mkString
        val screenshots = scenariosScreenshots(index, element.screenshotStyle, element.screenshotIDs, element.screenshotsSize, parentType)
        s"""
           |        <p class="scenario">
           |          <b>$icon ${element.keyword} </b>${escape(element.name)}
           |          <a href="#" class="btn btn-default btn-xs pull-right toggle-button" onclick="toggleScenario('ul-$parentType-$index', event)">
           |            <span class="glyphicon glyphicon-menu-down"></span>
           |          </a>
           |          <span class="durationBadge pull-right">${element.duration.durationStr} </span>
           |        </p>
           |        <div $style id="ul-$parentType-$index">
           |          <ul class="list-group">
           |            ${stepList(element.steps)}
           |          </ul>
           |          ${escape(output)}
           |        </div>
           |        $screenshots
     """.stripMargin
      case None => ""
    }

  }

  def scenariosScreenshots(index: String, style: String, screenshotsIds: String, screenshotsSize: Int, parentType: String) = {
    s"""
       |<a href="#" id="openScreenshotsFeatures-$index" onclick="toggleScreenshot('$index', 'screenshot-$parentType', '$screenshotsIds', event)" style="$style">screenshots ($screenshotsSize)</a>
       |   <div id="screenshot-$parentType-$index" class="row" style="display: none;"></div>
    """.stripMargin
  }

  def stepError(step: Step) = {

    if (step.error_message != "")
      s"""
         |<div style="white-space: pre-wrap;margin-left:15px;">
         | ${escape(step.error_message)}
         |</div>
      """.stripMargin
    else
      """"""
  }

  def stepList(steps: List[Step]): String = {
    steps.map(step => {
      val error = stepError(step)

      s"""
         |<li class="list-group-item step ${step.status.statusStr}">
         |  <span class="durationBadge pull-right"> ${step.duration.durationStr} </span>
         |  ${statusIcon(step.status.statusStr)} <b> ${step.keyword} </b>  <span class="wrapped-text" style="white-space: pre-wrap;">${escape(step.name)}</span>
         |  $error
         |  ${dataTable(step.rows)}
         |</li>
     """.stripMargin
    }).mkString("")
  }

  def dataTable(rows: List[Row]): String = {
    if (rows.nonEmpty)
      "<table class=\"step-table\">" +
        rows.map(row => {
          val cell = if (row.cells.nonEmpty) row.cells.map(c => s"""<td class="step-table-cell">""" + escape(c.mkString) + """</td>""").mkString else ""
          "<tr>" + cell + "</tr>"
        }).mkString +
        "</table>"
    else ""
  }

  def examplesList(examples: List[Examples]): String = {
    if (examples.nonEmpty)
      s"""
      |<div class="panel-body">${examples.map(exs => { s"""
      |  <div class="row">
      |    <div class="panel panel-default">
      |      <div class="panel-body">
      |        <p class="examples">
      |          <b>${exs.keyword}: </b>${escape(exs.name)}
      |          <div margin-left:15px;">${description(exs.description.get)}</div>
      |          ${dataTable(exs.rows)}
      |        </p>
      |      </div>
      |    </div>
      |  </div>""".stripMargin}).mkString}
      |</div>
    """.stripMargin
    else ""
  }

  def getFeatureLink(parentType: String, parentIndex: String, featureId: String) = {
    if (parentType != "feature")
      s"""
         |<b>Feature:</b>
         |<a data-dismiss="modal" data-toggle="modal" data-target="#modal-$parentIndex" href="#modal-$parentIndex"> ${escape(featureId)} </a><br><br>
      """.stripMargin
    else ""
  }

  def statusIcon(status: String) = {
    if (status == "passed")
      """<span class="glyphicon glyphicon-ok-sign status-span-pass"></span>"""
    else
      """<span class="glyphicon glyphicon-exclamation-sign status-span-fail"></span>"""
  }

  def elementTags(tags: List[String]) =
    tags.map(tag => {
      s"""
         |<span class="tagBadge">
         |  <a data-dismiss="modal" data-toggle="modal" data-target="#tag-$tag" href="#tag-$tag" class="tagBadgeStyle">
         |    <span class="sub_icon glyphicon glyphicon-tag"></span>$tag
         |  </a>
         |</span>
      """.stripMargin
    }).mkString


  def scenariosAllIds(parentType: String, value: String) = {
    if (parentType == "failure")
      s"""<input hidden id=\"${parentType}Ids" value="$value">"""
    else ""
  }
}