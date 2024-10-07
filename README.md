# User Backend Application
## How to Start This Project
### Prerequisites
Make sure your computer has the following:

* Java 17 or later
* Maven
* IntelliJ IDEA
* Docker
* Git

### Instructions

* Clone the repository
    ``` 
    git clone https://github.com/Elie-MK/User.git 
    ```
* Open the project in IntelliJ IDEA 
   * Launch IntelliJ IDEA
   * click on "Open" and select the cloned project directory
* Ensure Docker is running
   * Make sure Docker is running on your machine, especially when you run the unit tests.
* Run the project
   * Navigate to the main application file
    ````
    users\src\main\java\com\rakbank\users\UsersApplication.java
    ````
    * Right-click on the file and select "Run 'UsersApplication.main()' "
* Check port availability 
    * The application will run on port 8080. Ensure that this port is not already taken by another application.
* Access Swagger UI
   * You can access the swagger UI for API documentation at : 
   ```
   http://localhost:8080/swagger-ui/index.html#/
   ```
* Build the JAR File
   * Open your terminal or command prompt.
   * Navigate to your project directory where the pom.xml file is located.
   * Run the following command to build the JAR file:
      ```
      mvn clean package
      ```

