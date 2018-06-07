# Quindy

Quindy is a high-level API that allows interaction with Hyperledger Indy nodes from Java. It is currently in a pre-release
state, breaking changes will be happening a lot.

Quindy provides an object-oriented API, at the level agents would want to have interaction. If your application
needs low level control over the communication, or a procedural API is more suitable, we refer to the
[indy-sdk](https://github.com/hyperledger/indy-sdk), for Java specifically to the 
[Java wrapper](https://github.com/hyperledger/indy-sdk/tree/master/wrappers/java) that indy-sdk provides

## Running in docker
In order to test, run: `docker-compose up --build --force-recreate --exit-code-from wrapper pool wrapper`

## Running locally
In order to run locally, install libindy with the version matching the indy-sdk in the `pom.xml` 
through instructions [here](https://github.com/hyperledger/indy-sdk) (use master branch to install dev builds)
or build from source and set `LD_LIBRARY_PATH` to a folder with `libindy.so`.

## Debugging
In order to get logging from the lower level sdk, set `RUST_LOG=info` in your environment