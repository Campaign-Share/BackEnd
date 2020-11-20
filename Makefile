
.PHONY: build
build: build
	./gradlew clean build

.PHONY: image
image:
	docker build . -t campaign-share:${VERSION}

.PHONY: upload
upload:
	docker tag campaign-share:${VERSION} jinhong0719/campaign-share:${VERSION}.RELEASE
	docker push jinhong0719/campaign-share:${VERSION}.RELEASE

.PHONY: pull
pull:
	docker pull jinhong0719/campaign-share:${VERSION}.RELEASE

.PHONY: run
run:
	docker-compose -f ./docker-compose.yml up -d
