package io.magentys.donut.template

import java.io.{File, PrintWriter}

import com.gilt.handlebars.scala.Handlebars
import com.gilt.handlebars.scala.binding.dynamic._
import io.magentys.donut.gherkin.model.Report
import io.magentys.donut.log.Log

object TemplateEngine {
  def apply(report: Report, templatePath: String): Renderer = {
    val inputStream = getClass.getResourceAsStream(templatePath)
    val template = scala.io.Source.fromInputStream(inputStream).mkString
    val hbs: Handlebars[Any] = Handlebars(template)
    val rep = SpecialCharHandler(hbs(report))
    Renderer(rep)
  }
}

case class Renderer(boundTemplate: String) extends Log {
  def renderToHTML(outputPath: String, filePrefix: String) = {
    val path =
      if (outputPath != "") {
        val outputDir = new File(outputPath)
        if (!outputDir.exists) outputDir.mkdirs
        outputDir.getAbsolutePath
      }

    val prefix = if (filePrefix != "") filePrefix + "-" else ""
    val out = new PrintWriter(outputPath + File.separator + prefix + "donut-report.html")
    out.write(boundTemplate.toString)
    out.close()
    log.info(s"Donuts created at: $path/${prefix}donut-report.html")
  }
}

object SpecialCharHandler {

  def apply(htmlReport: String) = {
    val report = unescapeReport(htmlReport)
    escapeErrorMessages(report)
  }

  def unescapeReport(htmlReport: String) = {
    htmlReport
      .replace("&quot;", "\"")
      .replace("&amp;", "&")
      .replace("&gt;", ">")
      .replace("&lt;", "<")
  }

  //We capture the error messages that might contain html code so we can escape it
  def escapeErrorMessages(htmlReport: String) = {
    htmlReport
      .split("<code>")
      .map(htmlSnip => {
        if (!htmlSnip.contains("</code>")) htmlSnip
        else {
          val codeSnip = htmlSnip.split("</code>")
          val escapedErrorMsg = codeSnip.head.replace(">", "&gt;").replace("<", "&lt;")
          escapedErrorMsg + codeSnip.tail.head
        }
      }).mkString
  }
}

