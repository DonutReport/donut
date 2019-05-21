## 1.2.2
* Fix Scenario output not being rendered to HTML template (Issue #55)
* Fix light template to cater for Unit tests
* Gilt handlebars requires inverse for #if conditional

## 1.2.1
* Fix regular expression in ResultLoader to handle absolute Windows path
* Report exception details to the console

## 1.2.0
* Supports changes to JSON results output relating to gherkin 5
* Maintains backwards compatibility with JSON results output relating to earlier gherkin versions with the exception of comments

## 1.1
* Publishing only unit test results is now supported.
* You don't necessarily have to provide the path to cucumber/specflow json files, which was the case earlier
* The donut UI won't show the scenarios donut if only unit tests are being reported.
* Following scenarios have been tested to be working fine:
    * Only cucumber json files
    * Cucumber & unit json files (Unit tests linked to features in cucumber json files)
    * Cucumber & unit json files - orphaned unit tests(i.e. with feature name ‘Without Feature’)
    * Cucumber & unit json files - unit tests linked to different feature(s)    
    * Only unit json files - with feature name other than ‘Without Feature’
    * Only unit json files - with feature name ‘Without Feature’
       - Feature count is shown as 0, as there’s no feature other than ‘Without Feature’, so feature donut is collapsed
       - Also, scenarios donut isn’t there as there are no scenarios
       - And unit tests donut isn’t there as there are no linked unit tests i.e. unit tests tied to any feature other than ‘Without Feature’
       - All the unit tests are treated as orphaned and reported on the report as ‘orphaned’ tests
       - To see unit tests being reported properly, feature name must be specified for unit tests
    
## 1.0
* Unit test metrics separated from scenarios. A separate dashboard panel will be displayed with unit test pie-chart 
   * If there are 0 unit tests(that are part of a feature), unit tests panel won't be displayed
* Orphaned unit test metrics reported separately under the dashboard panel
   * If there are 0 orphaned unit tests, their metrics won't be displayed at all
* Fixed the bugs:
   * Scenario/Unit Test description wasn't being displayed.
   * Windows paths were not working
   * Feature description is in this format "Some(<description>)" instead of displaying just the description
* Feature summary table will report the total numbers (Scenarios + Unit Tests).

## 0.0.5 
* Group scenarios from different json files by feature name
    * If you split your test execution by scenario rather than feature, you can group the scenarios on donut by feature name
* Print scenario type ex: Scenarios or Unit Tests above the scenarios or unit tests
* Read non-cuke json reports(ex: jsons generated from nunit xml using donut-nunit-adapter) and publish a single report
    * Donut can now handle 2 types of reports one cuke (cucumber/specflow) json and 2nd one unit test results json
    * The nunit adapter has only been tested for nunit xml reports. It hasn't been tested for JUnit reports.
* Improved error reporting in case of failures during report generation
* Fixed 'Background not expanding' bug