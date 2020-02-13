import React from "react";
import Word from "../components/Reader/Word";
import "semantic-ui-css/semantic.min.css";

export default {
  title: "Word",
  component: Word
};

export const English = () => (
  <Word
    language="ENGLISH"
    text="Hello"
    partOfSpeech="Noun"
    lemma="Hello"
    definitions={["A greeting", "Something to greet others with"]}
  />
);

export const Chinese = () => (
  <Word
    language="CHINESE"
    text="你好"
    partOfSpeech="Noun"
    lemma="你好"
    definitions={["欢迎观临", "您好"]}
    hsk={5}
    pinyin={["ni3 hao3"]}
  />
);
