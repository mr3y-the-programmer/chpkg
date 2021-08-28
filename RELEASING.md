1. checkout `origin/main`
2. Update `CHANGELOG.md` file
3. Update version [here](https://github.com/mr3y-the-programmer/chpkg/blob/main/src/main/kotlin/chpkg.kt#L35) & [here](https://github.com/mr3y-the-programmer/chpkg/blob/main/build.gradle.kts#L10)
4. Commit the changes and create a tag for the next release
5. Push the changes & the new release tag:
      ```
      git push origin main && git push --tags
      ```
6. go to releases section on github and create a new release, also make sure to upload the necessary artifacts