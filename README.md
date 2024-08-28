# Monet [![CI](https://github.com/filiprd/monet/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/filiprd/monet/actions?query=branch%3Amain+) [![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) <a href="https://typelevel.org/cats/"><img src="https://typelevel.org/cats/img/cats-badge.svg" height="40px" align="right" alt="Cats friendly" /></a>

Monet is a project which came to life with the educational purpose, and was inspired by the excellent [Practical FP in
Scala](https://leanpub.com/pfp-scala) book by Gabriel Volpe. Monet's goal is to implement an API for a backend of an online 
paintings retail platform, in Scala 3, with the functional programming paradigm using the Typelevel stack.

Latest update: Monet was initially completed in early 2023, and it has been updated and openly published in the 
summer of 2024.

# Specs

Monet specification includes:
- [Functional requirements document](specs/functional-requirements.md), which specifies business requirements
- [Design document](specs/design.md), which specifies domain model and API routes

# Tech Stack

Monet uses the following libraries:
- [cats](https://typelevel.org/cats/), for functional programming abstractions
- [cats-effect](https://typelevel.org/cats-effect/), for concurrency and IO monad
- [cats-retry](https://cb372.github.io/cats-retry/), for retrying failing effects
- [circe](https://circe.github.io/circe/), for encoding/decoding JSON
- [fs2](https://fs2.io/), for purely functional streams
- [http4s](https://http4s.org/), for purely functional HTTP servers/clients
- [http4s-jwt-auth](https://github.com/profunktor/http4s-jwt-auth), for JWT authentication
- [kittens](https://github.com/typelevel/kittens), for cats' typeclass deriving
- [log4cats](https://typelevel.org/log4cats/), for purely functional logging
- [monix-newtypes](https://newtypes.monix.io/), for defining newtypes
- [monocle](https://www.optics.dev/Monocle/), for access and transformation of immutable data
- [pureconfig](https://pureconfig.github.io/), for loading and handling configurations
- [redis4cats](https://github.com/profunktor/redis4cats), for a Redis client compatible with cats-effect/fs2
- [skunk](https://typelevel.org/skunk/), for purely functional PostgreSQL client
- [weaver](https://github.com/disneystreaming/weaver-test), for testing compatible with cats-effect/fs2

# Running Monet

If you are using Docker:
1. Create and publish Monet Docker image:
```bash
sbt docker:publishLocal
```

2. Run Monet with accompanying services (PostgreSQL and Redis):
```bash
sudo docker network create monet-network
docker-compose -f app/docker-compose.yml up -d
```

If you do not want to use Docker:
1. Ensure both PostgreSQL and Redis instances are running locally
2. Ensure `$MONET_ENV` environment variable is set to either `prod` or `test`
3. Make sure the [configuration](core/application.conf) file is specified correctly (note that in the linked example 
   PostgreSQL and Redis hosts are Docker-specific so you will need to modify them, for example to `localhost`).
4. Inside `sbt`, run `reStart`. To stop Monet, run `reStop`.

# Tests

To run unit tests execute:  
```bash
sbt test
```

To run integration tests, execute:
```bash
docker-compose -f tests/docker-compose.yml up -d
sbt it:test
docker-compose -f tests/docker-compose.yml down
```

# Future improvements

- Add a new admin account type to enable different management tasks (e.g., adding painting techniques/categories)
- Optimize painting update http route by writing only data that is being updated
- Use BouncyCastle instead of javax.crypto, and use GCM instead of CBC mode
- Add caching layer
- Introduce [Iron](https://github.com/Iltotore/iron) for (both runtime and compile time) refined types (also for 
  newtypes)

# Contributions
If you have suggestions for improvements, or think that I've done something wrong, feel free to open an [issue](https://github.com/filiprd/monet/issues).  
Pull requests are also more than welcome.

# License
Licensed under the Apache License, Version 2.0 (the "License"); you may not use this project except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.