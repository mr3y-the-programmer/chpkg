# Chpkg
A small command line tool to update your android project's package name easily, unlike Android Studio it takes care of
updating your directories & files properly.

## Demo:
```
chpkg --from "old_pkg_name" --to "new_pkg_name"
```

- **Limitations**:
    
    this tool can't change `APPLICATION_ID` for you, since it can be declared in many places
: `build.gradle`, `buildSrc` to name a few.

## Installation:

- Download binaries from releases: // TODO

- Build from Source:
    
    1- clone the project
    
    2- run: ```./gradlew installDist``` 

    3- Add the produced `bin` folder to `PATH` environment variable

## License:
```
Copyright [2021] [MR3YY]

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```