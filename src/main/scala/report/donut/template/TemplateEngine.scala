package report.donut.template

import java.io.{File, PrintWriter}

import com.gilt.handlebars.scala.Handlebars
import com.gilt.handlebars.scala.binding.dynamic._
import report.donut.gherkin.model.Report
import report.donut.log.Log
import report.donut._
import scala.util.Try

object TemplateEngine {
  def apply(report: Report, templatePath: String): Renderer = {
    val inputStream = getClass.getResourceAsStream(templatePath)
    val template = scala.io.Source.fromInputStream(inputStream).mkString
    val hbs: Handlebars[Any] = Handlebars(template)
    val rep = hbs(report)
    Renderer(rep)
  }
}

case class Renderer(boundTemplate: String) extends Log {
  def renderToHTML(outputPath: String, filePrefix: String): Either[String, Unit] = Try {
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
  }.toEither(_.getMessage)
}

object SpecialCharHandler {

  def escape(htmlReport: String) = {
    htmlReport
      .replace(">", "&gt;")
      .replace("<", "&lt;")
  }

}

