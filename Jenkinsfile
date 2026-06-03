pipeline {
    agent any //любой свободный агент

    triggers {
        pollSCM('0 H/3 * * *')} //проверяем гитлаб на наличие изменений каждые 3 часа

    options { //глобальные настройки пайплайна
        timestamps() //добавляем время в логах jenkins
        skipStagesAfterUnstable //если одна стадия упала, следующие скипаем
        buildDiscarder(logRotator(numToKeepStr: '2')) //храним последние 2 сборки
        timeout(time: 15, unit: 'MINUTES') //пайплайн прерывается если висит более 15 мин
    }

    tools{
        jdk 'Messenger-JDK'
        maven 'Messenger-Maven'
    }

    stages {
        stage('Build and Test') {
            steps {
                dir('backend') {
                    sh 'mvn clean verify -B'
                }
            }
            post {
                always {
                    junit 'backend/target/surefire-reports/**/*.xml'
                }
            }
        }

        stage('Archive Artifact') {
            steps {
                archiveArtifacts(
                    artifacts: 'backend/target/*.jar',
                    fingerprint: true
                )
            }
        }
    }





}