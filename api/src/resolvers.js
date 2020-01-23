const chinese = require("../content/chinese/cedict.json").reduce(
  (acc, word) => {
    if (word.simplified in acc) {
      acc[word.simplified].push(word);
    } else {
      acc[word.simplified] = [word];
    }
    return acc;
  },
  {}
);

module.exports = {
  Query: {
    wordsInText: async (_parent, { text }, { dataSources }) => {
      const unaddedWords = await dataSources.googleAPI.getWordsInText({ text: text });

      // Register the words in the database as users request them
      const words = unaddedWords.map( async word => {
        const existingWord = await dataSources.database.getWord({ text: word.text })

        if (existingWord.length > 1) {
          word["id"] = existingWord[0].id
        } else {
          const id = await dataSources.database.addWord(word);
          word["id"] = id[0];
        }

        return word;
      })

      return words;
    },
    health: (_parent, _args, { dataSources }) => {
      return "OK";
    }
  },
  Word: {
    __resolveType: (word, _context, _info) => {
      if (word.language == "CHINESE") return "ChineseWord";
      else return "GenericWord";
    }
  },
  ChineseWord: {
    definitions: (word, _args, _context) => {
      if (word.text in chinese) {
        return chinese[word.text].flatMap(x => x.definitions);
      }
    },
    hsk: (word, _args, _context) => {
      if (word.text in chinese) {
        const levels = chinese[word.text]
          .filter(x => "HSK" in x)
          .map(x => x["HSK"]);

        if (levels != []) {
          return Math.min(levels);
        }
      }
    },
    pinyin: (word, _args, _context) => {
      if (word.text in chinese) {
        return chinese[word.text].map(x => x.pinyin);
      }
    }
  }
};
