# Download files from S3 as stream zip file

### Usage

Build docker image
* `docker build -t zip-streaming .`

Run docker image

* `docker run -p 8087:8087 --env AWS_ACCESS_KEY_ID=YOUR_ACCESS_KEY --env AWS_SECRET_KEY=YOUR_SECRET_KEY --env S3_BUCKET=YOUR_BUCKET zip-streaming `

For running localy
* `sbt run`

Run in browser, where 'keys' parameter is list of S3 key separated by ";"
* `http://localhost:8087/download?keys=test/images/test1.jpg;test/images/test2.jpg;test/images/test3.jpg `

