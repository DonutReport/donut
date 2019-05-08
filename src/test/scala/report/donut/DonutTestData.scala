package report.donut

import java.io.File

import report.donut.gherkin.model.{Embedding, Feature, StatusConfiguration}
import report.donut.transformers.cucumber.{CucumberTransformer, Feature => CucumberFeature}

import scala.collection.mutable.ListBuffer
import scala.util.Try

object DonutTestData {

  val statusConfiguration = StatusConfiguration()

  val features_sample_2: Either[String, List[Feature]] = {
    val sourcePath = List("cucumber:src", "test", "resources", "samples-2").mkString("", File.separator, File.separator)
    val loader = ResultLoader(sourcePath)
    val donutFeatures = CucumberTransformer.transform(loader.load().right.get, DonutTestData.statusConfiguration).right.get
    Try(donutFeatures.toList).toEither(_.getMessage)

  }

  val features_sample_3: Either[String, List[Feature]] = {
    val sourcePath = List("cucumber:src", "test", "resources", "samples-3").mkString("", File.separator, File.separator)
    val loader = ResultLoader(sourcePath)
    val donutFeatures = CucumberTransformer.transform(loader.load().right.get, DonutTestData.statusConfiguration).right.get
    Try(donutFeatures.toList).toEither(_.getMessage)
  }

  val featuresWithOnlyUnits: Either[String, List[Feature]] = {
    val sourcePath = List("cucumber:src", "test", "resources", "cuke-and-unit", "unit").mkString("", File.separator, File.separator)
    val loader = ResultLoader(sourcePath)
    val donutFeatures = CucumberTransformer.transform(loader.load().right.get, DonutTestData.statusConfiguration).right.get
    Try(donutFeatures.toList).toEither(_.getMessage)
  }

  val featuresWithCukeAndOrphanedUnits: Either[String, List[Feature]] = {
    val sourcePaths = List(List("cucumber:src", "test", "resources", "samples-6", "bdd").mkString("", File.separator, File.separator),
      List("cucumber:src", "test", "resources", "samples-6", "unit").mkString("", File.separator, File.separator))
    val features = new ListBuffer[CucumberFeature]
    for (sourcePath <- sourcePaths) {
      val loader = ResultLoader(sourcePath)
      features ++= loader.load().right.get
    }
    val donutFeatures = CucumberTransformer.transform(features.toList, DonutTestData.statusConfiguration).right.get
    Try(donutFeatures.toList).toEither(_.getMessage)
  }

  val embedding = Embedding("image/png",
    "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AACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAGjYxwkAACAASURBVACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkEhOAAAAieQEAACQSE4AAACJ5AQAAJBITgAAAInkBAAAkKj06KOPHu4xAAAAL0tZnueHewwAAMDL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",
    578984356)

}
