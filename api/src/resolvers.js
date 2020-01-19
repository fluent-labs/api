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
    wordsInText: (_parent, { text }, { dataSources }) => {
      return dataSources.googleAPI.getWordsInText({ text: text });
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
