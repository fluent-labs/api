import React from "react";
import ReactDOM from "react-dom";
import "./index.css";
import "semantic-ui-css/semantic.min.css";

import Reader from "./components/Reader/Reader";
import introspectionQueryResultData from "./fragmentTypes.json";

import { ApolloClient } from "apollo-client";
import { ApolloProvider } from "@apollo/react-hooks";
import {
  InMemoryCache,
  IntrospectionFragmentMatcher
} from "apollo-cache-inmemory";
import { HttpLink } from "apollo-link-http";

// Environment detection
let apiUrl = "https://api.foreignlanguagereader.com/api/";
if (window.location.hostname === "localhost") {
  apiUrl = "http://localhost:4000/api/";
}

const fragmentMatcher = new IntrospectionFragmentMatcher({
  introspectionQueryResultData
});
const cache = new InMemoryCache({ fragmentMatcher });
const link = new HttpLink({
  uri: apiUrl
});

const client = new ApolloClient({
  cache,
  link
});

ReactDOM.render(
  <ApolloProvider client={client}>
    <Reader />
  </ApolloProvider>,
  document.getElementById("root")
);
