sh
# 1. Remove the large file from the Git index (stops tracking it)
git rm --cached java_pid14992.hprof

# 2. Update your last commit to exclude this file
git commit --amend --no-edit

# 3. Push again (you may need -f because we changed the commit history)
git push -f origin main