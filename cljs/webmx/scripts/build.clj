(require '[cljs.build.api :as b])

(println "Building ...")

(let [start (System/nanoTime)]
  (b/build "src"
    {:main 'tiltontec.webmx.core
     :output-to "out/webmx.js"
     :output-dir "out"
     :verbose false})
  (println "... done. Elapsed" (/ (- (System/nanoTime) start) 1e9) "seconds"))


