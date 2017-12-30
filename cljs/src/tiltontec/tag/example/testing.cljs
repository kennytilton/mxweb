(ns tiltontec.tag.example.testing
  (:require [clojure.string :as str]
            [tiltontec.util.core :refer [now]]
            [tiltontec.cell.core :refer-macros [c? c?once] :refer [c-in]]
            [tiltontec.model.core
             :refer-macros [with-par]
             :refer [matrix mx-par md-get md-reset! mxi-find mxu-find-name] :as md]
            [tiltontec.tag.gen :refer [evt-tag target-value]
             :refer-macros [h1 h2 h3 h4 h5 section label header footer p span a img ul li input div button]]
            [tiltontec.tag.style
             :refer [make-css-inline]
             :as css]
            [goog.dom :as dom]
            [goog.dom.classlist :as classlist]
            [goog.editor.focus :as focus]
            [goog.dom.selection :as selection]
            [goog.events.Event :as event]
            [goog.dom.forms :as form]))

(defn test-page-3 []
  [(div {:id      "xx"
         :onclick #(let [me (evt-tag %)]
                     (println :click!! % (:id @me) (:clicks @me))
                     (md-reset! me :clicks (inc (:clicks @me))))}

     {:clicks (c-in 0)}

     "Maybe?"

     (span {:style (c? (make-css-inline me
                         :color "blue"
                         :background-color "red"
                         :padding (c? (let [c (md-get (mx-par (:tag @me)) :clicks)]
                                        (str (* c 6) "px")))))
            :hidden (c? (odd? (md-get (mx-par me) :clicks)))}
           {:content (c? (str "Himom style ?! " (md-get (mx-par me) :clicks)))})

     (span {:style "color:red;background-color:#eee;padding:10px"}
           {:content (c? (str "Himom style string! " (md-get (mx-par me) :clicks)))})

     (span {:style (c? (let [c (md-get (mx-par me) :clicks)]
                         {:color            "blue"
                          :background-color "yellow"
                          :padding          (str (* c 6) "px")}))}
           {:content (c? (str "Himom style ?! " (md-get (mx-par me) :clicks)))}))
   ;(h1 {:hidden true} "Himom hidden!!")
   #_(div {:class "color-input" :style "margin-top:24px"}
       "Time color: "
       (input {:name     :timecolor
               :tag/type "text"
               :value    (c-in "#0ff")}))])

(defn matrix-build! []
  (md/make ::startwatch
    :mx-dom (c?once (md/with-par me (test-page-3)))))

