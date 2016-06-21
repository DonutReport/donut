package io.magentys.donut.gherkin.processors

import io.magentys.donut.gherkin.model._

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
    if(failedElements.length > 0) {
      val indexes = failedElements.zipWithIndex
      val failedScenariosHtml = indexes.map { case (e, i) => HTMLProcessor.scenarios(e, i.toString.trim, "failure") }.mkString
      val failedScenariosIds = HTMLProcessor.scenariosAllIds("failure", indexes.map { case (e, i) => "ul-" + "failure-" + i.toString.trim }.mkString(","))
      failedScenariosHtml + failedScenariosIds
    } else {
      "No failures"
    }
  }
}

object HTMLProjectMetadata {
  def apply(projectMetadata: ProjectMetadata) = {
    val pname = if (projectMetadata.projectName != "") s"""Project: <span class="white">${projectMetadata.projectName}</span>""" else projectMetadata.projectName
    val pversion = if (projectMetadata.projectVersion != "") s"""| Build: <span class="white">#${projectMetadata.projectVersion}</span>""" else projectMetadata.projectVersion
    new ProjectMetadata(pname, pversion)
  }
}

private[processors] object HTMLProcessor {

  def apply(elements: List[Scenario], parentIndex: String, parentType: String): String =
    elements.zipWithIndex.map { case (e, i) => scenarios(e, parentIndex + i.toString.trim, parentType) }.mkString

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
       |          <b>$icon ${element.keyword} </b>${element.name}
       |          <a href="#" class="btn btn-default btn-xs pull-right toggle-button" onclick=toggleScenario('ul-$parentType-$index')>
       |            <span class="glyphicon glyphicon-menu-down"></span>
       |          </a>
       |          <span class="durationBadge pull-right">${element.duration.durationStr} </span>
       |        </p>
       |        <div $style id="ul-$parentType-$index">
       |          <ul class="list-group">
       |            ${stepList(element.steps)}
       |          </ul>
       |          $output
       |        </div>
       |        $screenshots
       |      </div>
       |   </div>
       | </div>
     """.stripMargin

  }

  def backgroundForScenario(elementOpt: Option[Scenario], index: String, parentType: String) = {

    elementOpt match {
      case Some(element) => {
        val icon = statusIcon(element.status.statusStr)
        val style = if (element.status.statusStr == "passed") """style="display:none;"""" else ""
        val output = element.steps.flatMap(s => s.output).map(o => s"""<div class="step-custom-output">$o</div>""").mkString
        val screenshots = scenariosScreenshots(index, element.screenshotStyle, element.screenshotIDs, element.screenshotsSize, parentType)
        s"""
           |        <p class="scenario">
           |          <b>$icon ${element.keyword} </b>${element.name}
           |          <a href="#" class="btn btn-default btn-xs pull-right toggle-button" onclick=toggleScenario('ul-$parentType-$index')>
           |            <span class="glyphicon glyphicon-menu-down"></span>
           |          </a>
           |          <span class="durationBadge pull-right">${element.duration.durationStr} </span>
           |        </p>
           |        <div $style id="ul-$parentType-$index">
           |          <ul class="list-group">
           |            ${stepList(element.steps)}
           |          </ul>
           |          $output
           |        </div>
           |        $screenshots
     """.stripMargin
      }
      case None => ""
    }

  }

  def scenariosScreenshots(index: String, style: String, screenshotsIds: String, screenshotsSize: Int, parentType: String) = {
    s"""
       |<a href="#" id="openScreenshotsFeatures-$index" onclick="toggleScreenshot('$index', 'screenshot-$parentType', '$screenshotsIds')" style="$style">screenshots (${screenshotsSize})</a>
       |   <div id="screenshot-$parentType-$index" class="row" style="display: none;"></div>
    """.stripMargin
  }

  def stepError(step: Step) = {

    if (step.error_message != "")
      s"""
        |<div style="white-space: pre-wrap;margin-left:15px;">
        | <code> ${step.error_message} </code>
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
         |  ${statusIcon(step.status.statusStr)} <b> ${step.keyword} </b>  <span class="wrapped-text" style="white-space: pre-wrap;">${step.name}</span>
         |  ${error}
         |  ${stepTable(step.rows)}
         |</li>
     """.stripMargin
    }).mkString("")
  }

  def stepTable(rows: List[Row]): String = {
    if (rows.size > 0)
      "<table class=\"step-table\">" +
        rows.map(row => {
          val cell = if (row.cells.size > 0) row.cells.map(c => s"""<td class="step-table-cell">""" + c.mkString + """</td>""").mkString else ""
          "<tr>" + cell + "</tr>"
        }).mkString +
        "</table>"
    else ""
  }

  def getFeatureLink(parentType: String, parentIndex: String, featureId: String) = {
    if (parentType != "feature")
      s"""
         |<b>Feature:</b>
         |<a data-dismiss="modal" data-toggle="modal" data-target="#modal-$parentIndex" href="#modal-$parentIndex"> $featureId </a><br><br>
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