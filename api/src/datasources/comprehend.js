const { RESTDataSource } = require("apollo-datasource-rest");

class AWSComprehendAPI extends RESTDataSource {
  constructor() {
    super();
  }
}

module.exports = AWSComprehendAPI;
