# build-macros
Build macros for ant.

How you can import these and use them in your own project
while also being able to merge in future changes:

  1. Decide where they should go, e.g., "src/build"
  2. `git subtree add -P src/build git@github.com:archiecobbs/build-macros master`

Then proceed normally.

If/when you want to import the latest changes from this project:

  1. `git subtree pull -P src/build --squash git@github.com:archiecobbs/build-macros master`

