#!/usr/bin/env bash

is_source_from_pr_testable() {
  base_dir=$(git rev-parse --show-toplevel)
  github_pr_dir="${base_dir}/.git/resource"
  exclude_dirs="${@:-ci}"
  for d in $(echo ${exclude_dirs}); do
    local exclude_pathspec="${exclude_pathspec} :(exclude,glob)${d}/**"
  done
  local return_code=0
  if [ -d "${github_pr_dir}" ]; then
#    shopt -s extglob
    pushd ${base_dir} &> /dev/null
      local files=$(git diff --name-only $(cat "${github_pr_dir}/base_sha") $(cat "${github_pr_dir}/head_sha") -- . $(echo ${exclude_pathspec}))
    popd &> /dev/null
    if [[ -z "${files}" ]]; then
      echo "Code changes are from CI only"
      return_code=1
    else
      echo "real code change here!"
    fi
  else
    echo "repo is not from a PR"
  fi
  return ${return_code}
}
