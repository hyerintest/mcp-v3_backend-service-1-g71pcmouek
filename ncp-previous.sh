#!/bin/bash
echo PREVIOUS START...
echo Kubectl download...
curl -o kubectl https://amazon-eks.s3-us-west-2.amazonaws.com/1.21.2/2021-07-05/bin/linux/amd64/kubectl
chmod +x ./kubectl
mv ./kubectl /usr/local/bin/kubectl
mkdir ~/.kube
echo Helm download...
curl https://raw.githubusercontent.com/helm/helm/master/scripts/get-helm-3 > get_helm.sh
chmod 700 get_helm.sh
./get_helm.sh
echo ArgoCD download...
curl -H s3://turaco-cli  https://turaco-curl.s3.ap-northeast-2.amazonaws.com/argocd/argocd -o argocd
chmod +x argocd
mv argocd /usr/local/bin/argocd

# Check if ./settings.xml exists
if [ -f ./settings.xml ]; then
    mkdir -p /root/.m2
    echo mkdir settings.xml
    mv ./settings.xml /root/.m2/settings.xml
else
    echo "settings.xml not found, skipping..."
fi

echo PREVIOUS END...
