#!/bin/bash

# Compile Java files
javac -cp "lib/jackson/*;lib/jsoup/*;lib/selenium-java-4.28.1/*;lib/sqlite/*" -d bin src/main/java/RealTimeNewsScraper.java

# Copy resource files
cp src/main/resources/config_sina_news.properties bin/
