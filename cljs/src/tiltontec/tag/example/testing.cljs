(ns tiltontec.tag.example.testing
  (:require [clojure.string :as str]
            [tiltontec.util.core :refer [now]]
            [tiltontec.cell.core :refer-macros [c? c?once] :refer [c-in]]
            [tiltontec.model.core
             :refer-macros [with-par]
             :refer [matrix mx-par md-get md-reset! mxi-find mxu-find-name] :as md]
            [tiltontec.tag.gen :refer [evt-tag target-value]
             :refer-macros [h1 h2 h3 h4 h5 section label header footer p span a img ul li input div button]]
            [goog.dom :as dom]
            [goog.dom.classlist :as classlist]
            [goog.editor.focus :as focus]
            [goog.dom.selection :as selection]
            [goog.events.Event :as event]
            [goog.dom.forms :as form]))

(defn test-page-3 []
  [(h1 {:style "color:red;background-color:#000;padding:10px"} "Himom!")
   (h1 {:hidden true} "Himom!!")
   (div {:class "color-input"}
     "Time color: "
     (input {:name     :timecolor
             :tag/type "text"
             :value    (c-in "#0ff")
             :onclick #(println :click!! %)}))])

(defn matrix-build! []
  (md/make ::startwatch
    :mx-dom (c?once (md/with-par me (test-page-3)))))

