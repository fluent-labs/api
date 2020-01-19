const { gql } = require("apollo-server");

const typeDefs = gql`
  type Query {
    wordsInText(text: String!): [Word!]!
    wordInformation(words: [String!]!): [Word!]!
    me: User
    health: String
  }

  type Word {
    language: Language!
    text: String!
    partOfSpeech: String
    lemma: String
    definitions: [String!]
  }

  enum Language {
    CHINESE
    ENGLISH
    SPANISH
  }

  type Vocabulary {
    id: ID!
    user: User
    word: Word!
    added: String
  }

  type User {
    id: ID!
    email: String!
    vocabulary: [Vocabulary!]!
  }
`;

module.exports = typeDefs;
