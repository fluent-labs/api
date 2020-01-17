module.exports = {
  Query: {
    wordsInText: async (_, { text }, { dataSources }) => {
      return dataSources.googleAPI.getWordsInText({ text: text});
    },
    health: (_, _inputs, { dataSources}) => {
      return "OK";
    }
  }
};
