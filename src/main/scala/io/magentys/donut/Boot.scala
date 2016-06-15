package io.magentys.donut

import java.io.File

import io.magentys.donut.gherkin.Generator
import io.magentys.donut.log.Log
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import scopt.OptionParser

object Boot extends App with Log {

  case class DonutArguments(sourceDir: String ="",
                            outputDir: String = "donut",
                            prefix: String = "",
                            datetime: String = DateTimeFormat.forPattern("yyyy-MM-dd-HHmm").print(DateTime.now),
                            template:  String = "default",
                            countSkippedAsFailure: Boolean = false,
                            countPendingAsFailure: Boolean = false,
                            countUndefinedAsFailure: Boolean = false,
                            countMissingAsFailure: Boolean = false,
                            projectName: String = "",
                            projectVersion : String = "")

  val optionParser = new OptionParser[DonutArguments]("MagenTys Donut reports") {

    head("\nDonut help")

    opt[String]('s', "sourcedir") action { (arg, config) =>
      config.copy(sourceDir = arg)
    } text "Use --sourcedir /my/path/cucumber-reports -> Required"

    opt[String]('o', "outputdir") action { (arg, config) =>
      config.copy(outputDir = arg)
    } text "Use --outputdir /my/path/output/donut"

    opt[String]('p', "prefix") action { (arg, config) =>
      config.copy(prefix = arg)
    } text "Use --prefix fileNamePrefix"

    opt[String]('d', "datetime") action { (arg, config) =>
      config.copy(datetime = arg)
    } text "Use --datetime yyyy-MM-dd-HHmm"

    opt[String]('t', "template") action { (arg, config) =>
      config.copy(template = arg)
    } text "Use --template default/light"

    opt[Boolean]("skippedFails") action { (arg, config) =>
      config.copy(countSkippedAsFailure = arg)
    } text "Use --skippedFails true/false"

    opt[Boolean]("pendingFails") action { (arg, config) =>
      config.copy(countPendingAsFailure = arg)
    } text "Use --pendingFails true/false"

    opt[Boolean]("undefinedFails") action { (arg, config) =>
      config.copy(countUndefinedAsFailure = arg)
    } text "Use --undefinedFails true/false"

    opt[Boolean]("missingFails") action { (arg, config) =>
      config.copy(countMissingAsFailure = arg)
    } text "Use --missingFails true/false"

    opt[String]("projectName") action { (arg, config) =>
      config.copy(projectName = arg)
    } text "Use --projectName myProject"

    opt[String]("projectVersion") action { (arg, config) =>
      config.copy(projectVersion = arg)
    } text "Use --projectVersion 1.0"


    checkConfig { c =>
      if (c.sourceDir == "") failure("Missing source dir.") else success
      if (!new File(c.sourceDir).exists()) failure("Source dir does not exist") else success
    }
  }

  optionParser parse(args, DonutArguments()) match {
    case Some(appargs) => {
      Generator(appargs.sourceDir,
                              appargs.outputDir,
                              appargs.prefix,
                              appargs.datetime,
                              appargs.template,
                              appargs.countSkippedAsFailure,
                              appargs.countPendingAsFailure,
                              appargs.countUndefinedAsFailure,
                              appargs.countMissingAsFailure,
                              appargs.projectName,
                              appargs.projectVersion)}
    case None =>
      println("\nERROR: No valid arguments specified.")
      System.exit(-1)
  }

}







