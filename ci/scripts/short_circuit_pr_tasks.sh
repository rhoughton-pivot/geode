#!/usr/bin/env bash

exit_zero_if_source_from_pr() {
  set -x
  base_dir=$(git rev-parse --show-toplevel)
  github_pr_dir="${base_dir}/.git/resource"
  if [ -d "${github_pr_dir}" ]; then
    files=$(git diff --name-only $(cat "${github_pr_dir}/base_sha") $(cat"${github_pr_dir}/head_sha"))
    for f in $files; do
      echo "${f}"
    done
  else
    echo "repo is not from a PR"
  fi
  set +x
}
