const { RESTDataSource } = require("apollo-datasource-rest");
const AWS = require("aws-sdk");
AWS.config.update({ region: "us-west-2" });

const comprehend = new AWS.Comprehend();

class AWSComprehendAPI extends RESTDataSource {
  constructor() {
    super();
  }

  getWordsInText({ text }) {
    comprehend.detectSyntax({ LanguageCode: "en", Text: text }, (err, data) => {
      if (err) {
        console.log(err);
        return [];
      } else {
        console.log(data);
        return Array.isArray(data) ? data : [];
      }
    });
  }
}

module.exports = AWSComprehendAPI;
