
# Make Symbolic links
	Create symbol links in your battlecode scaffold (battlecode24-scaffold) that point into this repo.

	Make sure the links are named 'tx', or the packaging will be messed up.

- (scaffold)/src/"tx" -> (repo)/tx
- (scaffold)/test/"tx" -> (repo)/test/tx






## Why?
Because you need to keep the scaffold GIT repo in-tact so that we can pull battlecode client updates with Gradle.
We also need to version control our own code.


Both repos need to remain in tact, but separated.

Also, the IDE won't be able to commit Project changes to git.  You should only be GIT pulling from the scaffold.

To commit changes, you need to navigate to the directory where you GIT cloned this repo and run GIT commands normally from there.

- git pull
- git add -A
- git commit -m "Some message"
- git push
- 
