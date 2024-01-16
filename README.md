
# Make Symbolic links
	Create symbol links in your battlecode scaffold (battlecode24-scaffold) that point into this repo.

	Make sure the links are named 'tx', or the packaging will be messed up.

- (scaffold)/src/"tx" -> (repo)/tx
- (scaffold)/test/"tx" -> (repo)/test/tx






## Why?
- Because the scaffold needs to pull battlecode updates for the client
- We need to version control our code 

Both repos need to remain in tact, but separated.

### Key Details
Commands from the IDE: Anything Battlecode related. Build and Run. __NO CODE COMMITS__
- ./gradlew build
- ./gradlew update 
- ./gradlew zipForSubmission

__To commit changes, you to your git run commands from THIS repo__
