pipeline {
    agent any
    tools {
        jdk 'jdk17'
        maven 'maven3.8.1'
    }
    environment {
        COMMIT_ID = ''
        MVN_HOME = tool 'maven3.8.1'
    }
    stages {
        stage('Sonarqube Build') {
            steps {
                git branch: "${BRANCH}", url: "https://${SOURCE_REPO_URL}/${GROUP_NAME}_${SERVICE_NAME}.git", credentialsId: "${CREDENTIAL_ID}"
                script {
                    if (env.IS_SONAR == "true") {
                        echo "SonarQube analysis..."
                        sh "${MVN_HOME}/bin/mvn clean verify sonar:sonar -Dsonar.projectKey=${PROJECT_KEY} -Dsonar.projectName=${PROJECT_KEY} -Dsonar.token=${SONAR_TOKEN} -Ddocker-registry=turaco-registry -Ddockerfile.skip=true"
                        sh "sleep 60"
                        sh "curl -u ${SONAR_ID}:${SONAR_PWD} ${SONAR_HOST_URL}/api/qualitygates/project_status?projectKey=${PROJECT_KEY} > result.json"
                        def qualityGates = readJSON(file: 'result.json').projectStatus.status
                        echo "Quality Gates Status: ${qualityGates}"

                        if (qualityGates == "ERROR") {
                            error("SonarQube Quality Gate failed")
                        }
                    } else {
                        echo "SonarQube analysis skipped (IS_SONAR != true)"
                    }
                }
            }
        }

        stage('Build') {
            steps {
                git branch: "${BRANCH}", url: "https://${SOURCE_REPO_URL}/${GROUP_NAME}_${SERVICE_NAME}.git", credentialsId: "${CREDENTIAL_ID}"
                script {
                    COMMIT_ID = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                    echo "Commit ID: ${COMMIT_ID}"

                    echo "Maven Build..."
                    sh '''
                        if [ -f ./settings.xml ]; then
                            mvn clean package -P ${SPRING_PROFILES_ACTIVE} -s ./settings.xml -Ddocker-registry=turaco-registry -Ddockerfile.skip=true
                        else
                            mvn clean package -P ${SPRING_PROFILES_ACTIVE} -Ddocker-registry=turaco-registry -Ddockerfile.skip=true
                        fi
                    '''
                    echo "Podman Build..."
                    sh "podman login -u ${HARBOR_USER} -p ${HARBOR_PASSWORD} ${IMAGE_REPO_NAME}"
                    sh "podman build -t ${IMAGE_REPO_NAME}:${ARGO_APPLICATION}-latest --build-arg SPRING_PROFILE=${SPRING_PROFILES_ACTIVE} --build-arg JAR_FILE=${SERVICE_NAME}.jar -f ./src/main/docker/Dockerfile ."
                    sh "podman tag ${IMAGE_REPO_NAME}:${ARGO_APPLICATION}-latest ${IMAGE_REPO_NAME}:${ARGO_APPLICATION}-${COMMIT_ID}"
                    sh "podman push ${IMAGE_REPO_NAME}:${ARGO_APPLICATION}-${COMMIT_ID}"
                    sh "podman push ${IMAGE_REPO_NAME}:${ARGO_APPLICATION}-latest"

                }
            }
        }

        stage('Deploy') {
            steps {
                git (branch: "master", url: "https://${SOURCE_REPO_URL}/${GROUP_NAME}_HelmChart.git", credentialsId: "${CREDENTIAL_ID}")
                dir ("${STAGE}/${SERVICE_NAME}") {
                    sh "find ./ -name values.yaml -type f -exec sed -i \'s/^\\(\\s*tag\\s*:\\s*\\).*/\\1\"\'${ARGO_APPLICATION}-${COMMIT_ID}\'\"/\' {} \\;"
                    sh 'git config --global user.email "info@twolinecode.com"'
                    sh 'git config --global user.name "jenkins-runner"'
                    sh 'git add ./values.yaml'
                    sh "git commit --allow-empty -m \"Pushed Helm Chart: ${ARGO_APPLICATION}-${COMMIT_ID}\""
                    withCredentials([gitUsernamePassword(credentialsId: "${CREDENTIAL_ID}", gitToolName: 'git-tool')]) {
                        sh '''
                        while :
                        do
                            git pull --rebase origin master
                            if git push origin master
                            then
                                break
                            fi
                        done
                        '''
                    }
                }
                dir("${STAGE}/Common") {
                    script {
                        echo "Helm & Kubectl apply..."
                        sh '''
                            helm template . > ./common.yaml
                            kubectl --kubeconfig ../$KUBECONFIG apply -f common.yaml
                            kubectl --kubeconfig ../$KUBECONFIG get secret argocd-initial-admin-secret -n tlc-support -o jsonpath='{.data.password}' | base64 -d > argocd-password.txt
                        '''
                        def PASSWORD = readFile("argocd-password.txt").trim()
                        echo "Sync ArgoCD ing..."
                        sh "argocd login ${ARGO_ENDPOINT}:80 --grpc-web-root-path argocd --username admin --password ${PASSWORD} --plaintext --insecure"
                        sh "argocd app get ${ARGO_APPLICATION} --refresh"
                        sh "argocd app sync ${ARGO_APPLICATION}"
                    }
                }
            }
        }
    }
}
