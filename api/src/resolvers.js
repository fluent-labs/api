module.exports = {
  Query: {
    wordsInText: async (_, { text }, { dataSources }) => {
      return await dataSources.comprehendAPI.getWordsInText({ text: text });
    }
  }
};
