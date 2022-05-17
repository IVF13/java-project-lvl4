install:
	./gradlew clean install

run-dist:
	./build/install/app/bin/app

check-updates:
	./gradlew dependencyUpdates

lint:
	./gradlew checkstyleMain
	
build:
	./gradlew clean build

report:
	./gradlew jacocoTestReport	

test:
	./gradlew test

.PHONY: build
