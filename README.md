# Azure Functions in Java :
This is an azure function to gather your GPB operations (statements) from your ebanking account


## Prerequisites

- Gradle 4.10+
- Latest [Function Core Tools](https://aka.ms/azfunc-install)
- Azure CLI. This plugin use Azure CLI for authentication, please make sure you have Azure CLI installed and logged in.

## Setup

- ```cmd
    az login
    az account set -s <your subscription id>
    ```

- Update `host.json` with the right extension bundle version. `V3 - [1.*, 2.0.0) and V4 - [2.*, 3.0.0)`


## Running locally
```cmd
./gradlew clean azureFunctionsRun
```
```cmd
curl   http://localhost:7071/api/HttpTriggerJavaVersion
curl -d "{ account:'' , pwd:''}" --request POST http://localhost:7071/api/GbpStatements
```



## Deploy  on Azure

```cmd
./gradlew clean azureFunctionsDeploy
```
```cmd
curl   {your host}/api/HttpTriggerJavaVersion
curl -d "{ account:'' , pwd:''}" --request POST {your host}/api/GbpStatements
```

> NOTE: please replace '/' with '\\' when you are running on windows.


