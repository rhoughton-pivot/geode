#!/usr/bin/env bash
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


sanitizeName() {
  echo ${1} | tr "._/" "-" | tr '[:upper:]' '[:lower:]'
}

getSanitizedBranch () {
  echo $(sanitizeName ${1}) | cut -c 1-20
}

getSanitizedFork () {
  echo $(sanitizeName ${1}) | cut -c 1-16
}

shortenJobName () {
  echo $(sanitizeName ${1}) | sed -e 's/windows/win/' -e 's/distributed/dst/' -e 's/acceptance/acc/' -e 's/openjdk/oj/' | cut -c 1-18
}

parseMetaProperties() {
  META_PROPERTIES=${SCRIPTDIR}/../meta/meta.properties
  LOCAL_META_PROPERTIES=${SCRIPTDIR}/../meta/meta.properties.local

  ## Load default properties
  source ${META_PROPERTIES}
  echo "**************************************************"
  echo "Default Environment variables for this deployment:"
  cat ${META_PROPERTIES} | grep -v "^#"
  source ${META_PROPERTIES}
  GEODE_BRANCH=$(git rev-parse --abbrev-ref HEAD)
  echo GEODE_BRANCH=${GEODE_BRANCH}
  echo "**************************************************"

  ## Load local overrides properties file
  if [[ -f ${LOCAL_META_PROPERTIES} ]]; then
    echo "Local Environment overrides for this deployment:"
    cat ${LOCAL_META_PROPERTIES} | grep -v "^#"
    source ${LOCAL_META_PROPERTIES}
    echo "**************************************************"
  else
    git remote -v | awk '/fetch/{sub("/[^/]*$","");sub(".*[/:]","");if($0!="apache")print}' | while read fork; do
      echo "to deploy a pipeline for $fork, press x then"
      echo "echo GEODE_FORK=$fork > ${LOCAL_META_PROPERTIES}"
    done
    echo "**************************************************"
  fi

  read -n 1 -s -r -p "Press any key to continue or x to abort" DEPLOY
  echo
  if [[ "${DEPLOY}" == "x" ]]; then
    echo "x pressed, aborting deploy."
    exit 0
  fi
}

checkRequiredPythonModules() {
  for cmd in Jinja2 PyYAML; do
    if ! [[ $(pip3 list |grep ${cmd}) ]]; then
      echo "${cmd} must be installed for pipeline deployment to work."
      echo " 'pip3 install ${cmd}'"
      echo ""
      exit 1
    fi
  done
}
