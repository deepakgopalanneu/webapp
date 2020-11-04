# Webapp

## Prerequisites to run locally
1. install Java version 8 or above
    `sudo apt install default-jdk`
2. verify installation
    `javac -version` should return `javac 11.0.7`
3. if Maven is not installed, install it via 
    `sudo apt install maven`

## Steps to run
1. Clone or download the zip code from 
    `git clone git@github.com:gopaland-fall2020/webapp.git`
1. Build the project using  `mvn clean install`
2. Run using `mvn spring-boot:run`
3. The web application is accessible via localhost:8080
4. Use any client such as postman or soap UI to test the application
