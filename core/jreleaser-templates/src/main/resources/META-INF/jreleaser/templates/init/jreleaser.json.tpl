{
  "project": {
    "name": "app",
    "version": "1.0.0-SNAPSHOT",
    "description": "Awesome App",
    "longDescription": "Awesome App",
    "authors": [
      "Duke"
    ],
    "license": "Apache-2.0",
    "links": {
      "homepage": "https://acme.com/app"
    },
    "languages": {
        "java": {
          "groupId": "com.acme",
          "version": "8"
        }
    },
    "inceptionYear": "@year@"
  },

  "release": {
    "github": {
      "owner": "duke"
    }
  },

  "distributions": {
    "app": {
      "artifacts": [
        {
          "path": "path/to/{{distributionName}}-{{projectVersion}}.zip"
        }
      ]
    }
  }
}
