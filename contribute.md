## pull request workflow

- Fork `donut`
- Check out the `master` branch
- Make a feature branch (use `git checkout -b "cool-new-feature”`)
- Make your cool new feature or bugfix on your branch
- Write a test for your change
- From your branch, make a pull request against `donut/master`
- Work with repo maintainers to get your change reviewed
- Wait for your change to be pulled into `donut/master`
- Merge  `donut/master` into your origin `donut/master`
- Delete your feature branch

Additional help can be found on github documentation:
https://help.github.com/articles/using-pull-requests/

## style 
Pull Requests should come with a description. We adhere a specific format:
```
Problem: 
Explain the context and why you're making that change.  What is the
problem you're trying to solve? In some cases there is not a problem
and this can be thought of being the motivation for your change.

Solution: 
Describe the modifications you've done.
```

If your PR fixes an open issue you should mention it in the PR description 
because github will automatically closes this issue during the merge. 
You need to add the keyword `fixes` i.e. `fixes #5`. 

Check this PR for example: https://github.com/MagenTys/donut/pull/6



