(require '[cljs.build.api :as b])

(b/watch "src"
  {:main 'tiltontec.webmx.core
   :output-to "out/webmx.js"
   :output-dir "out"
   :verbose false})
