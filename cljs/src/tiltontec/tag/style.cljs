(ns tiltontec.tag.style
  (:require
    [tiltontec.util.core :refer [pln]]
    [tiltontec.cell.base :refer [md-ref? ia-type unbound]]
    [tiltontec.cell.observer :refer [observe observe-by-type]]
    [tiltontec.cell.evaluate :refer [not-to-be not-to-be-self]]
    [tiltontec.model.core
     :refer-macros [the-kids mdv!]
     :refer [md-get fasc fm! make md-reset! backdoor-reset!]
     :as md]
    [goog.dom.classlist :as classlist]
    [goog.style :as gstyle]
    [goog.dom :as dom]
    [cljs.pprint :as pp]
    [clojure.string :as str]))

;; todo move to utill or put all this in tag

(defn tag-dom [me]
  ;; This will return nil when 'me' is being awakened and rules
  ;; are firing for the first time, because 'me' has not yet
  ;; been installed in the actual DOM, so call this only
  ;; from event handlers and the like.
  (let [id (md-get me :id)]
    (assert id)
    (or (md-get me :dom-cache)
        (if-let [dom (dom/getElement (str id))]
          (backdoor-reset! me :dom-cache dom)
          (println :no-element id :found)))))

(defn make-css-inline [tag & stylings]
  (apply make
         :type :tiltontec.tag.css/css
         ;;:tag-id (when tag (:id @tag))
         :tag tag
         :css-keys (for [[k _] (partition 2 stylings)] k)
         stylings))

(defn style-string [s]
  (cond
    (string? s) s

    (map? s)
    (do
      ;;(println :ss-map-keys (keys s))
      (str/join ";"
                (for [[k v] s]
                  (pp/cl-format nil "~a:~a" (name k) v))))

    (= :tiltontec.tag.css/css (ia-type s))
    (style-string (select-keys @s (:css-keys @s)))

    :default
    (do
      (println :ss-unknown (type s)  ))))


#_(case slot
    :style (set! (.-style dom) newv)
    :hidden (set! (.-hidden dom) newv)
    :class (classlist/set dom newv)
    :checked (set! (.-checked dom) newv)
    )

#_(case slot
    :display (set! (.-display (.-style dom)) newv))

(defmethod observe-by-type [:tiltontec.tag.css/css] [slot me newv oldv _]
  (when (not= oldv unbound)
    (pln :obs-css-by-type slot newv me)
    (let [dom (tag-dom (:tag @me))]

      (gstyle/setStyle dom (name slot) newv))))

#_@(make-css-inline nil :a (c-in 1) :b (c? (* (md-get me :a))))