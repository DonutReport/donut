![](http://magentys.github.io/donut/img/Donut-05.png)

[![Build Status](https://travis-ci.org/MagenTys/donut.svg?branch=master)](https://travis-ci.org/MagenTys/donut)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.magentys/donut/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.magentys/donut)
[![Javadoc](https://javadoc-emblem.rhcloud.com/doc/io.magentys/donut/badge.svg)](http://www.javadoc.io/doc/io.magentys/donut)

Live Demo => here (TODO)

## quickstart
You can either use Donut directly or check out the available plugins: 
* [Maven plugin](https://github.com/MagenTys/donut-maven-plugin)
* [Jenkins plugin] (https://github.com/MagenTys/donut-jenkins-plugin)

### download
```
wget http://repo1.maven.org/maven2/io/magentys/donut/0.0.1/donut-0.0.1-one-jar.jar
```
or download the latest release from: [here](http://repo1.maven.org/maven2/io/magentys/donut/0.0.1/donut-0.0.1-one-jar.jar)

### run from command line

```
java -jar donut-<Version>.jar -s /source/dir 
```

### options

`-s` or `--sourcedir` is a mandatory parameter, and it should be the directory that hold the generated JSON files to be visualised. 

Other parameters can also be specified as bellow:

```
Donut help
Usage: MagenTys Donut reports [options]

  -s <value> | --sourcedir <value>
        Use --sourcedir /my/path/cucumber-reports -> Required
  -o <value> | --outputdir <value>
        Use --outputdir /my/path/output/donut
  -p <value> | --prefix <value>
        Use --prefix fileNamePrefix
  -d <value> | --datetime <value>
        Use --datetime yyyy-MM-dd-HHmm
  -t <value> | --template <value>
        Use --template default/light
  --skippedFails <value>
        Use --skippedFails true/false
  --pendingFails <value>
        Use --pendingFails true/false
  --undefinedFails <value>
        Use --undefinedFails true/false
  --missingFails <value>
        Use --missingFails true/false
  --projectName <value>
        Use --projectName myProject
  --projectVersion <value>
        Use --projectVersion 1.0
```

default values:
* **outputDir** : by default a `donut` folder will be generated
* **prefix** : the generated file is `donut-report.html`, however you can specify prefix i.e. `myproject-`
* **datetime** : refers to the start time of your execution. If not specified by the user reports will use `now`
* **template** : donut supports 2 themes, `default` and `light`. `default` is the default value

## use as a dependency

* Maven
```
<dependency>
  <groupId>io.magentys</groupId>
  <artifactId>donut</artifactId>
  <version>0.0.1</version>
</dependency>
```

* SBT 
```
libraryDependencies += "io.magentys" % "donut" % "0.0.1"
```

* Gradle
```
compile 'io.magentys:donut:0.0.1'
```

Example usage of the `Generator`

```
ReportConsole report = 
       Generator.apply(sourceDirectory, outputDirectory, filePrefix, timestamp, template, countSkippedAsFailure,         
       countPendingAsFailure, countUndefinedAsFailure, countMissingAsFailure, projectName, projectVersion);
```

This will create an `html` report at the outputDirectory and will return a `ReportConsole` output object: 

```
allFeatures: List[Feature]
allTags: List[ReportTag]
totalFeatures: Int
numberOfPassedFeatures: Int
numberOfFailedFeatures: Int
totalScenarios: Int
numberOfPassedScenarios: Int
numberOfFailedScenarios: Int
totalSteps: Int
numberOfPassedSteps: Int
numberOfFailedSteps: Int
numberOfSkippedSteps: Int
numberOfPendingSteps: Int
numberOfUndefinedSteps: Int
duration: String
buildFailed: Boolean
```

## build from source

### prerequisites

* install java 8+
* install scala 2.11+
* install SBT ([www.scala-sbt.org](www.scala-sbt.org))

### run from sbt

`sbt "run-main io.magentys.donut.Boot -s /my/jsons/dir" `



### License

This project is under an MIT license

Powered by: [MagenTys](http://magentys.io)
