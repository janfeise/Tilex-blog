/**
 * 观察单个dom元素：当元素进入或者退出指定视眼范围时，所执行的操作
 */
const observeDom = function (
  dom,
  callback,
  option = { root: null, threshold: 0, rootMargin: 0 }
) {
  // 定义观察函数
  const observeFunc = function (entries, observer) {
    const [entry] = entries;
    console.log(entry);
    console.log(option);
    const isInView = entry.isIntersecting;

    // 回调函数：根据 isEntry 判断什么时候执行回调函数
    callback(entry.target, isInView);
  };

  // 创建观察器
  const domObserver = new IntersectionObserver(observeFunc, option);

  // 开始观察
  domObserver.observe(dom);

  // 返回一个停止观察的方法
  return () => domObserver.unobserve(dom);
};

/**
 * 观察多个dom元素：当元素进入或者退出指定视眼范围时，所执行的操作
 */
const observeDoms = function (
  doms,
  callback,
  option = { root: null, threshold: 0, rootMargin: "0px" }
) {
  const domObserver = new IntersectionObserver(callback, option);
  doms.forEach((dom) => {
    domObserver.observe(dom);
  });
};

export { observeDom, observeDoms };
