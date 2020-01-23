const { ApolloServer } = require("apollo-server");
const typeDefs = require("./schema");
const resolvers = require("./resolvers");
const Database = require("./datasources/database");
const GoogleNaturalLanguageAPI = require("./datasources/google");

// This is only for running locally
const knexConfig = {
  client: 'mysql',
  connection: {
    host : 'localhost',
    user : 'root',
    password : 'my-secret-pw',
    database : 'foreign-language-reader'
  }
};

const server = new ApolloServer({
  typeDefs,
  resolvers,
  dataSources: () => ({
    database: new Database(knexConfig),
    googleAPI: new GoogleNaturalLanguageAPI()
  })
});

server.listen().then(({ url }) => {
  console.log(`🚀 Server ready at ${url}`);
});
