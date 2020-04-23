pipeline {
  agent any
  tools {
    jdk 'jdk8'
  }
  stages {
    stage('Build') {
      steps {
        sh './gradlew build'
      }
    }

    stage('Test') {
      steps {
        sh './gradlew check'
      }
    }
  }

  post {
    always {
      archiveArtifacts artifacts: 'build/distributions/*.zip', fingerprint: true
      junit 'build/test-results/**/*.xml'
    }
  }
}