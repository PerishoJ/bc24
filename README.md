
# Make Symbolic links
	Create symbol links in your battlecode scaffold (battlecode24-scaffold) that point into this repo.

	Make sure the links are named 'tx', or the packaging will be messed up.

- (scaffold)/src/"tx" -> (repo)/tx
- (scaffold)/test/"tx" -> (repo)/test/tx






## Why?
Because you need to keep the scaffold GIT repo in-tact so that we can pull battlecode client updates with Gradle.
We also need to version control our own code.
This is how
