/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

def call(){

  // sonarqube api token
  cred_id = config.credential_id ?:
            "sonarqube"

  enforce = config.enforce_quality_gate ?:
            true

  stage("SonarQube Analysis"){
    inside_sdp_image "sonar-scanner", {
      withCredentials([usernamePassword(credentialsId: cred_id, passwordVariable: 'token', usernameVariable: 'user')]) {
        withSonarQubeEnv("SonarQube"){
          echo "**************** The ENV VARS -- START ****************"
             sh 'printenv|sort'
          /*
          
          echo "DOLLAR USER :: "$user
          echo "DOLLAR TOKEN :: "$token
          echo "DOLLAR reponame :: "$env.REPO_NAME
          echo "DOLLAR BRANCH_NAME :: "$env.BRANCH_NAME
          */
          echo "**************** The ENV VARS -- END   ****************"
          // unstash "workspace"
          // try{ unstash "test-results" }catch(ex){}
          sh "mkdir -p empty"
          projectKey = "$env.REPO_NAME:$env.BRANCH_NAME".replaceAll("/", "_")
          projectName = "$env.REPO_NAME - $env.BRANCH_NAME"
          def script = """sonar-scanner -X -Dsonar.login=${user} -Dsonar.password=${token} -Dsonar.projectKey="$projectKey" -Dsonar.projectName="$projectName" -Dsonar.projectBaseDir=. """
           
          if (!fileExists("sonar-project.properties"))
            script += "-Dsonar.sources=\"./src\""
          sh "ls -lartR /var/lib/jenkins/workspace/JenkinsDemo_master@tmp"
          sh "cat /var/lib/jenkins/workspace/JenkinsDemo_master@tmp/*/script.sh"
          sh "id"
          sh "cat /var/lib/jenkins/workspace/JenkinsDemo_master@tmp/*/config.json"
          sh "docker ps -a"
          sh "docker ps | grep sonar-scanner  | awk '{print \$1}' | xargs  docker logs "
          //sh "which sonar-scanner"
          sh "ls -lartR /var/lib/jenkins/workspace/JenkinsDemo_master@tmp"
          sh script
            
        }
        timeout(time: 1, unit: 'HOURS') {
          def qg = waitForQualityGate()
          if (qg.status != 'OK' && enforce) {
            error "Pipeline aborted due to quality gate failure: ${qg.status}"
          }
        }
      }
    }
  }
}
