![](http://magentys.github.io/donut/img/Donut-05.png) 

[![Build Status](https://travis-ci.org/MagenTys/donut.svg?branch=master)](https://travis-ci.org/MagenTys/donut)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.magentys/donut/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.magentys/donut)
[![Javadoc](https://javadoc-emblem.rhcloud.com/doc/io.magentys/donut/badge.svg)](http://www.javadoc.io/doc/io.magentys/donut)

## 0.0.5 
* Group scenarios from different json files by feature name
    * If you split your test execution by scenario rather than feature, you can group the scenarios on donut by feature name
* Print scenario type ex: Scenarios or Unit Tests above the scenarios or unit tests
* Read non-cuke json reports(ex: jsons generated from nunit xml using donut-nunit-adapter) and publish a single report
    * Donut can now handle 2 types of reports one cuke (cucumber/specflow) json and 2nd one unit test results json
    * The nunit adapter has only been tested for nunit xml reports. It hasn't been tested for JUnit reports.
* Improved error reporting in case of failures during report generation
* Fixed 'Background not expanding' bug