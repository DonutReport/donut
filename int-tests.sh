echo "Start integration tests $TRAVIS_JOB_ID $TRAVIS_BRANCH $TRAVIS_BUILD_NUMBER"

ls -la

ls -la $TRAVIS_BUILD_DIR

function doRun {
  sbt "run-main io.magentys.donut.Boot -s src/test/resources/samples-2"
}

cd $TRAVIS_BUILD_DIR
doRun


