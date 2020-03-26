# MeshTalk Message Gateway

MeshTalk message gateway.

## Running

1. Change the passwords:
    - `src/main/resources/server.properties`
    - `docker-compose.yml`
2. Create the data directory:
    - `$ mkdir data`
3. Build the application:
    - `$ docker-compose build`
4. Start the application: 
    - `$ docker-compose up`

The application will run on port `8082`, you can change this in the `docker-compose.yml` file.

If you need/want an example configuration for NGINX [look no further](nginx.site.conf).

## Upgrading

1. SSH to your server, cd into the repo directory
2. Since the passwords were changed (and committed), pull with rebase:
    - `$ git pull --rebase`
3. Stop and remove the containers:
    - `$ docker-compose stop`
    - `$ docker-compose rm -f`
4. Change the owner of the data owner back to the user building the container:
    - `$ chown -R user:user data`
    - This can be skipped if you use `root` to build/start the container
5. Build the updated container and start it:
    - `$ docker-compose build`
    - `$ docker-compose up -d`
6. Check if the application runs correctly

## Developing

1. Don't change the db password (without updating the command below)!
2. Build the project using any (Java) IDE
3. Run the database:
    - `$ docker run -it -v "$(pwd)/db:/docker-entrypoint-initdb.d" -p 5432:5432 -e POSTGRES_PASSWORD=changemebabychangeme postgres:alpine`
4. Start the application from the IDE

The application will run on port `8080` by default, this can be changed in `src/main/resources/server.properties`. Note that changing this will require you to update the `docker-compose.yml`.
 