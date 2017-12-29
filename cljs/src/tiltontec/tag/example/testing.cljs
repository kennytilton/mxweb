(ns tiltontec.tag.example.testing
  (:require [clojure.string :as str]
            [tiltontec.util.core :refer [now]]
            [tiltontec.cell.core :refer-macros [c? c?once] :refer [c-in]]
            [tiltontec.model.core
             :refer-macros [with-par]
             :refer [matrix mx-par md-get md-reset! mxi-find mxu-find-name] :as md]
            [tiltontec.tag.gen :refer [evt-tag target-value] :refer-macros [h1 input div]]))

(defn matrix-build! []
  (md/make ::startwatch
           :mx-dom (c?once (md/with-par me
                                        [(h1 {:style "color:red;background-color:#00f"} {} "Himom?")
                                         (h1 {:hidden true} {} "Himom!")]))))