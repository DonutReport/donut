package io.magentys.donut.template

import java.io.{File, PrintWriter}

import com.gilt.handlebars.scala.Handlebars
import com.gilt.handlebars.scala.binding.dynamic._
import io.magentys.donut.gherkin.model.Report
import io.magentys.donut.log.Log

object TemplateEngine {
  def apply(report: Report, path: String): Renderer = {
    val inputStream = getClass.getResourceAsStream(path)
    val template = scala.io.Source.fromInputStream(inputStream).mkString
    val t: Handlebars[Any] = Handlebars(template)
    Renderer(t(report)
      .replace("&quot;", "\"")
      .replace("&amp;", "&")
      .replace("&gt;", ">")
      .replace("&lt;", "<"))
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

    val prefix = if (filePrefix!="") filePrefix + "-" else ""

    val out = new PrintWriter(outputPath + File.separator + prefix + "donut-report.html")
    out.write(boundTemplate.toString)
    out.close()
    log.info(s"Donuts created at: $path/${prefix}donut-report.html")
  }
}



