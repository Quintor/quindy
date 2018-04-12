# StudyBits

Contact us on [Gitter](https://gitter.im/StudyBits/Lobby)

The `indy-wrapper` module contains a high-level wrapper around `indy-sdk`. 
[Documentation](indy-wrapper/README.md) 

The `university` module contains the university trust-anchor rest-api. [Documentation](university/README.md)

The `student` module contains the student agent rest-api.

Running tests: `docker build -t studybits:latest . && docker-compose up --build --force-recreate --exit-code-from tests`

Running the backend: `docker build -t studybits:latest . && docker-compose up --build --force-recreate pool backend-university backend-student`

Running frontend and backend `docker build -t studybits:latest . && docker-compose up --build --force-recreate pool backend-university backend-student frontend`
For this, the frontend repo needs to be checked out in the same directory as the backend (i.e. StudyBits and StudyBits frontend are in the same directory).



For all modules holds that in order to get logging from the lower level indy, set `RUST_LOG=info` in your environment
