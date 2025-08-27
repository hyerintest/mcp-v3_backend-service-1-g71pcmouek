#!/bin/bash
echo BUILD START...
COMMIT_ID=`git rev-parse --short HEAD`
echo Building with mvn...
docker login -u $ACCESS_KEY -p $SECRET_KEY $IMAGE_REPO_NAME
EXIT_CODE=$?
if [ $EXIT_CODE -ne 0 ] ; then
  echo 'docker login failed'; exit $EXIT_CODE
fi
mvn clean package -P $SPRING_PROFILES_ACTIVE -Dspring-profile=$SPRING_PROFILES_ACTIVE -Ddocker-registry=$IMAGE_REPO_NAME -Ddocker-name=$ARGO_APPLICATION -Ddocker-tag=$COMMIT_ID
EXIT_CODE=$?
if [ $EXIT_CODE -ne 0 ] ; then
  echo 'mvn clean package failed'; exit $EXIT_CODE
fi
echo BUILD END...
