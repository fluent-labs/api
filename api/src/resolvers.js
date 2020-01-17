module.exports = {
  Query: {
    wordsInText: async (_, { text, language }, { dataSources }) => {
      return await dataSources.comprehendAPI.getWordsInText({ text: text, language: language });
    }
  }
};
