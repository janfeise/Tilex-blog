// utils/markdown.js
import { marked } from "../../node_modules/marked/lib/marked.esm";
import hljs from "highlight.js";
import bash from "highlight.js/lib/languages/bash"; // 按需注册语言
hljs.registerLanguage("bash", bash);

// 配置 marked
marked.setOptions({
  gfm: true, // 开启 GitHub 风格表格
  breaks: true, // 可选，自动换行
  headerIds: true, // 可选，给标题生成 id
  highlight: function (code, lang) {
    if (lang && hljs.getLanguage(lang)) {
      return hljs.highlight(code, { language: lang }).value;
    }
    return hljs.highlightAuto(code).value;
  },
  langPrefix: "hljs language-",
});

export default marked;
