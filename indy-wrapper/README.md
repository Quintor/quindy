# StudyBits

In order to test, run: `LIBINDY_VERSION=1.3.1~406 docker-compose up --build --force-recreate --exit-code-from studybits`

In order to run locally, install libindy through instructions at: https://github.com/hyperledger/indy-sdk (use master branch to install dev builds)
or build from source and set `LD_LIBRARY_PATH` to a folder with `libindy.so`.

In order to get logging from the lower level sdk, set `RUST_LOG=info` in your environment