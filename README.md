# StudyBits

Contact us on [Gitter](https://gitter.im/StudyBits/Lobby)

The `wrapper` module contains a high-level wrapper around `indy-sdk`. [Documentation](indy-wrapper/README.md) 

The `studybits-university` module contains the university trust-anchor rest-api. [Documentation](studybits-university/README.md)

The `studybits-student` module contains the student agent rest-api.

Running tests: `docker build -t studybits:latest . && docker-compose up --build --force-recreate --exit-code-from tests`


For all modules holds that in order to get logging from the lower level indy, set `RUST_LOG=info` in your environment