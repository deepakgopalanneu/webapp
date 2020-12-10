# Webapp   

## Prerequisites to this webapp

1. Create AWS account and user with access to required resources
2. Clone the following repo and follow steps in readme to build AMI using Hashicorp Packer. 
This AMI will be used to build the EC2 instances on which we host our webapp.

    `git clone git@github.com:gopaland-fall2020/ami.git`
3. Clone the following repo and follow steps in readme to build the supporting infrastructure using terraform.

    `git clone git@github.com:gopaland-fall2020/infrastructure.git`
    
4. Now your infrastructure has been created, let's deploy our code to our Servers

## Steps to Run this App on AWS
- Enable github actions on your repository and save your credentials in Github secrets to utilize the CI/CD pipelines to trigger AWS code deploy to deliver the code to our instances.
- Alternately, Manually run each of the steps given in the pipeline.yml file in your terminal to deploy the code to the servers
## Steps to run the application Locally
1. Clone or download the zip code from 
    `git clone git@github.com:gopaland-fall2020/webapp.git`
2. Modify AWS properties & configs to create Beans using credentials builder(not recommended)
3. Build the project using  `mvn clean install`
4. Run using `mvn spring-boot:run`
5. The web application is accessible via localhost:8080
6. Use any client such as postman to test the application

## Steps to create and import SSL 
( Run this step before creating infrastructure via terraform)

- Generate private RSA key

    `sudo openssl genrsa -out private.key 2048`
- Generate CSR

    `sudo openssl req -new -key private.key -out csr.pem`
- Create a certificate via Namecheap or any other CA using the CSR 

- The CA issuer will provide  you with the certificate and certificate chain 

- Run the following command to import certificate to AWS Certificate Manager
 
    `aws acm import-certificate --certificate fileb://Certificate.pem 
    --certificate-chain fileb://CertificateChain.pem 
    --private-key fileb://PrivateKey.pem`
    
- Add the imported certificate to the loadbalancer's listener

