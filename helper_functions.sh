#!/bin/bash

 # checks for any files flagged w/ --skip-worktree alias
check="git ls-files -v|grep '^S'"
# add --skip-worktree flag to file
skip() {  git update-index --skip-worktree "$@";  git status; }
# remove --skip-worktree flag from file
unskip() {  git update-index --no-skip-worktree "$@";  git status; }

projects=("ru.bmstu.rk9.rao.lib/.project" "ru.bmstu.rk9.rao.tests/.project" "ru.bmstu.rk9.rao.ui/.project"  "ru.bmstu.rk9.rao/.project")

sp() {
    skip ${projects[@]}
}

up() {
    unskip ${projects[@]}
}

ucp() {
    unskip ${projects[@]};
    git checkout -- ${projects[@]};
}
