#!/bin/bash
#
# Copyright (C) 2016 The Material Motion Authors. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

usage() {
  echo "Usage:"
  echo "$0 [LibraryName] [PackageName] [GithubGroup] [GithubRepo]"
  echo
  echo "Creates a directory structure for an Android library."
  echo
  echo "Example usage: $0 MyLibrary com.example.library material-motion analytics"
}

if [ "$#" -lt 4 ]; then
  usage
  exit -1
fi

LIBRARY_NAME="$(echo $1 | tr -d ' ')"
PACKAGE_NAME="$(echo $2 | tr -d ' ')"
PACKAGE_SAMPLE_NAME="$PACKAGE_NAME.sample"
PACKAGE_PATH="$(echo $PACKAGE_NAME | tr . /)"
PACKAGE_SAMPLE_PATH="$PACKAGE_PATH/sample"
GITHUB_GROUP="$(echo $3 | tr -d ' ')"
GITHUB_REPO="$(echo $4 | tr -d ' ')"

CONVENTION_PACKAGE_NAME="com.google.android.material.motion.convention"
CONVENTION_PACKAGE_SAMPLE_NAME="$CONVENTION_PACKAGE_NAME.sample"
CONVENTION_PACKAGE_PATH="$(echo $CONVENTION_PACKAGE_NAME | tr . /)"
CONVENTION_PACKAGE_SAMPLE_PATH="$CONVENTION_PACKAGE_PATH/sample"
CONVENTION_GITHUB_GROUP="github-group"
CONVENTION_GITHUB_REPO="github-repo"

ROOT="$(dirname $0)"

# Set up new repo

rm -rf .git/
git init &>/dev/null
git remote add origin "git@github.com:$GITHUB_GROUP/$GITHUB_REPO.git"
echo "Reset git repository to git@github.com:$GITHUB_GROUP/$GITHUB_REPO.git"

# Initial commit

git add -A
git commit -m "Initial import from conventions-android" &>/dev/null
echo "Created initial commit for import"

# Replace all files

replace() {
  mkdir -p  "$NEW_PATH"

  if [ "$OLD_FILE" != "$NEW_FILE" ]; then
    mv "$OLD_FILE" "$NEW_FILE"
  fi

  sed -i  "s|Convention|$LIBRARY_NAME|g"                "$NEW_FILE"
  sed -i  "s|$CONVENTION_PACKAGE_NAME|$PACKAGE_NAME|g"  "$NEW_FILE"
  sed -i  "s|$CONVENTION_PACKAGE_PATH|$PACKAGE_PATH|g"  "$NEW_FILE"
  sed -i  "s|$CONVENTION_GITHUB_GROUP|$GITHUB_GROUP|g"  "$NEW_FILE"
  sed -i  "s|$CONVENTION_GITHUB_REPO|$GITHUB_REPO|g"    "$NEW_FILE"
}

# Library source

OLD_PATH="$ROOT/library/src/main/java/$CONVENTION_PACKAGE_PATH"
NEW_PATH="$ROOT/library/src/main/java/$PACKAGE_PATH"
OLD_FILE="$OLD_PATH/Convention.java"
NEW_FILE="$NEW_PATH/$LIBRARY_NAME.java"
replace

#Library test

OLD_PATH="$ROOT/library/src/androidTest/java/$CONVENTION_PACKAGE_PATH"
NEW_PATH="$ROOT/library/src/androidTest/java/$PACKAGE_PATH"
OLD_FILE="$OLD_PATH/ApplicationTest.java"
NEW_FILE="$NEW_PATH/ApplicationTest.java"
replace

# Library manifest

OLD_PATH="$ROOT/library/src/main"
NEW_PATH="$ROOT/library/src/main"
OLD_FILE="$OLD_PATH/AndroidManifest.xml"
NEW_FILE="$NEW_PATH/AndroidManifest.xml"
replace

# Library build.gradle

OLD_PATH="$ROOT/library"
NEW_PATH="$ROOT/library"
OLD_FILE="$OLD_PATH/build.gradle"
NEW_FILE="$NEW_PATH/build.gradle"
replace

# Sample source

OLD_PATH="$ROOT/sample/src/main/java/$CONVENTION_PACKAGE_SAMPLE_PATH"
NEW_PATH="$ROOT/sample/src/main/java/$PACKAGE_SAMPLE_PATH"
OLD_FILE="$OLD_PATH/MainActivity.java"
NEW_FILE="$NEW_PATH/MainActivity.java"
replace

# Sample manifest

OLD_PATH="$ROOT/sample/src/main"
NEW_PATH="$ROOT/sample/src/main"
OLD_FILE="$OLD_PATH/AndroidManifest.xml"
NEW_FILE="$NEW_PATH/AndroidManifest.xml"
replace

# Sample build.gradle

OLD_PATH="$ROOT/sample"
NEW_PATH="$ROOT/sample"
OLD_FILE="$OLD_PATH/build.gradle"
NEW_FILE="$NEW_PATH/build.gradle"
replace

# Sample strings.xml

OLD_PATH="$ROOT/sample/src/main/res/values"
NEW_PATH="$ROOT/sample/src/main/res/values"
OLD_FILE="$OLD_PATH/strings.xml"
NEW_FILE="$NEW_PATH/strings.xml"
replace

# README

OLD_PATH="$ROOT"
NEW_PATH="$ROOT"
OLD_FILE="$OLD_PATH/LIBRARY_README.md"
NEW_FILE="$NEW_PATH/README.md"
replace

# Delete old empty dirs

find $ROOT -type d -empty -delete

# Delete the bootstrap script
rm -- "$0"

# Second commit

git add -A
git commit -m "Ran script $0 $*" &>/dev/null
echo "Created second commit for replacement"

echo
echo "Bootstrap script done. You now have two commits in a new git history. Optionally modify the README.md, then run"
echo " $ git push -u origin master"
