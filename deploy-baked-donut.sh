echo "Start integration tests for build number: $TRAVIS_BUILD_NUMBER"

TARGET_BRANCH="gh-pages"

# Run to create the baked donut file, probably to another file
function doBakeDonut {
  sbt "run-main io.magentys.donut.Boot -s src/test/resources/samples-2"
}

doBakeDonut

# Save some useful information
REPO=`git config remote.origin.url`
SSH_REPO=${REPO/https:\/\/github.com\//git@github.com:}
SHA=`git rev-parse --verify HEAD`

# Clone the existing gh-pages for this repo into out/
# Create a new empty branch if gh-pages doesn't exist yet (should only happen on first deply)
git clone $REPO out
cd out
git checkout $TARGET_BRANCH || git checkout --orphan $TARGET_BRANCH
cd ..

# Now let's go have some fun with the cloned repo
cd out
git config user.name "Travis CI"
git config user.email "$COMMIT_AUTHOR_EMAIL"

echo "Before copy file in gh-pages"
ls -al

# copy donut file to gh-pages branch
rm -rf donut-report.html
cp ../donut/donut-report.html .
mv donut-report.html baked-donut.html

echo "Checking copied file.."
ls -al

# If there are no changes to the compiled out (e.g. this is a README update) then just bail.
# if [ -z `git diff --exit-code` ]; then
#    echo "No changes to the output on this push; exiting."
#    exit 0
# fi

# Commit the donut file.
# The delta will show diffs between new and old versions.
git add .
git commit -m "Deploy to GitHub Pages the Donut file: ${SHA}"

# Get the deploy key by using Travis's stored variables to decrypt deploy_key.enc
ENCRYPTED_KEY_VAR="encrypted_${ENCRYPTION_LABEL}_key"
ENCRYPTED_IV_VAR="encrypted_${ENCRYPTION_LABEL}_iv"
ENCRYPTED_KEY=${!ENCRYPTED_KEY_VAR}
ENCRYPTED_IV=${!ENCRYPTED_IV_VAR}
openssl aes-256-cbc -K $ENCRYPTED_KEY -iv $ENCRYPTED_IV -in travis_donut_ghpages_rsa.enc -out deploy_key -d
chmod 600 deploy_key
eval `ssh-agent -s`
ssh-add deploy_key

# Now that we're all set up, we can push.
git push $SSH_REPO $TARGET_BRANCH


