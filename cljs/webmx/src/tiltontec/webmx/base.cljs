(ns tiltontec.webmx.base
  (:require [tiltontec.util.base :refer [type-cljc]]))

(defn tag? [me]
  (= (type-cljc me) :tiltontec.webmx.base/tag))
