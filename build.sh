#!/bin/bash
# bundle must be built in separate maven reactor
cd org.melophonic.audio.abx.core
mvn clean install
# build the main app
cd ../org.melophonic.audio.abx.app.releng
mvn clean install
