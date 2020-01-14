const { gql } = require('apollo-server');

const typeDefs = gql`
  type Query {
    wordsInText: [Word]!
    wordInformation(words: [String]!): [Word]!
    me: User
  }

  type User {
    id: ID!
    email: String!
    vocabulary: [Word]!
  }

  type Word {
    id: ID!
    language: Language!
    text: String!
    partOfSpeech: String
    definition: String
  }

  enum Language {
    CHINESE
    ENGLISH
    SPANISH
  }
`;

module.exports = typeDefs;
